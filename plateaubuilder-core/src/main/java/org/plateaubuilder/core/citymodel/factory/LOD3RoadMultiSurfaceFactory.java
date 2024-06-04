package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.geometry.AbstractMultiSurfaceMeshView;
import org.plateaubuilder.core.citymodel.geometry.AuxiliaryTrafficAreaView;
import org.plateaubuilder.core.citymodel.geometry.LOD3RoadMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.citymodel.geometry.TrafficAreaView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD3RoadMultiSurfaceFactory extends GeometryFactory {

    public LOD3RoadMultiSurfaceFactory(CityModelView target) {
        super(target);
    }

    public LOD3RoadMultiSurfaceView createLOD3MultiSurface(Road gmlObject) {
        if (gmlObject.getLod3MultiSurface() == null) {
            return null;
        }

        var roadMultiSurfaceView = new LOD3RoadMultiSurfaceView(gmlObject.getLod3MultiSurface().getObject(), vertexBuffer, texCoordBuffer);
        {
            if (gmlObject.getLod3MultiSurface() != null) {
                var multiSurface = gmlObject.getLod3MultiSurface().getMultiSurface();
                roadMultiSurfaceView.setLOD3MultiSurfaceView(new LOD3MultiSurfaceFactory(getTarget()).createLOD3MultiSurface(multiSurface));
                roadMultiSurfaceView.setPolygons(createPolygonViews(multiSurface));
            }
        }

        // 表示用とエクスポート用で2回Polygonを作成している
        // バッファはFactoryが管理しているため、表示用はLOD3RoadMultiSurfaceFactory、エクスポート用は新しく生成したLOD3MultiSurfaceFactoryで行っている
        var trafficAreaViews = new ArrayList<TrafficAreaView>();
        for (var trafficAreaProperty : gmlObject.getTrafficArea()) {
            var trafficArea = trafficAreaProperty.getTrafficArea();
            if (trafficArea.getLod3MultiSurface() == null) {
                continue;
            }

            var multiSurface = trafficArea.getLod3MultiSurface().getMultiSurface();
            var lod3MultiSurface = new LOD3MultiSurfaceFactory(getTarget()).createLOD3MultiSurface(multiSurface);
            var trafficAreaView = new TrafficAreaView(trafficArea);
            trafficAreaView.setPolygons(lod3MultiSurface.getPolygons());
            trafficAreaView.setMultiSurfaceMeshView(lod3MultiSurface);
            trafficAreaViews.add(trafficAreaView);
            roadMultiSurfaceView.addPolygons(createPolygonViews(multiSurface));
        }
        roadMultiSurfaceView.setTrafficAreas(trafficAreaViews);

        var auxiliaryTrafficAreaViews = new ArrayList<AuxiliaryTrafficAreaView>();
        for (var auxiliaryTrafficAreaProperty : gmlObject.getAuxiliaryTrafficArea()) {
            var auxiliaryTrafficArea = auxiliaryTrafficAreaProperty.getAuxiliaryTrafficArea();
            if (auxiliaryTrafficArea.getLod3MultiSurface() == null) {
                continue;
            }

            var multiSurface = auxiliaryTrafficArea.getLod3MultiSurface().getMultiSurface();
            var lod3MultiSurface = new LOD3MultiSurfaceFactory(getTarget()).createLOD3MultiSurface(multiSurface);
            var auxiliaryTrafficAreaView = new AuxiliaryTrafficAreaView(auxiliaryTrafficArea);
            auxiliaryTrafficAreaView.setPolygons(lod3MultiSurface.getPolygons());
            auxiliaryTrafficAreaView.setMultiSurfaceMeshView(lod3MultiSurface);
            auxiliaryTrafficAreaViews.add(auxiliaryTrafficAreaView);
            roadMultiSurfaceView.addPolygons(createPolygonViews(multiSurface));
        }
        roadMultiSurfaceView.setAuxiliaryTrafficAreas(auxiliaryTrafficAreaViews);

        setMaterial(roadMultiSurfaceView);

        return roadMultiSurfaceView;
    }

    private List<PolygonView> createPolygonViews(MultiSurface multiSurface) {
        List<SurfaceProperty> surfaceMember = multiSurface.getSurfaceMember();

        var polygons = new ArrayList<PolygonView>();
        for (SurfaceProperty surfaceMemberElement : surfaceMember) {
            var polygon = (Polygon) surfaceMemberElement.getSurface();
            if (polygon == null) {
                continue;
            }
            var polygonObject = createPolygon(polygon, polygon.getId());
            polygons.add(polygonObject);
        }

        return polygons;
    }

    private void setMaterial(AbstractMultiSurfaceMeshView multiSurfaceView) {
        var polygonsMap = multiSurfaceView.getSurfaceDataPolygonsMap();
        // 1メッシュにつき1マテリアルしか登録できないため、マテリアルごとに別メッシュとして生成
        for (Map.Entry<SurfaceDataView, List<PolygonView>> entry : polygonsMap.entrySet()) {
            var meshView = new MeshView();
            meshView.setMesh(createTriangleMesh(entry.getValue()));
            if (entry.getKey() == null) {
                meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());
            } else {
                meshView.setMaterial(entry.getKey().getMaterial());
            }
            multiSurfaceView.addMeshView(meshView);
        }
    }
}
