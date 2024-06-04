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
import org.plateaubuilder.core.citymodel.geometry.LOD2RoadMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.citymodel.geometry.TrafficAreaView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD2RoadMultiSurfaceFactory extends GeometryFactory {

    public LOD2RoadMultiSurfaceFactory(CityModelView target) {
        super(target);
    }

    public LOD2RoadMultiSurfaceView createLOD2MultiSurface(Road gmlObject) {
        if (gmlObject.getLod2MultiSurface() == null) {
            return null;
        }

        var roadMultiSurfaceView = new LOD2RoadMultiSurfaceView(gmlObject.getLod2MultiSurface().getObject(), vertexBuffer, texCoordBuffer);
        {
            if (gmlObject.getLod2MultiSurface() != null) {
                var multiSurface = gmlObject.getLod2MultiSurface().getMultiSurface();
                roadMultiSurfaceView.setLOD2MultiSurfaceView(new LOD2MultiSurfaceFactory(getTarget()).createLOD2MultiSurface(multiSurface));
                roadMultiSurfaceView.setPolygons(createPolygonViews(multiSurface));
            }
        }

        // 表示用とエクスポート用で2回Polygonを作成している
        // バッファはFactoryが管理しているため、表示用はLOD2RoadMultiSurfaceFactory、エクスポート用は新しく生成したLOD2MultiSurfaceFactoryで行っている
        var trafficAreaViews = new ArrayList<TrafficAreaView>();
        for (var trafficAreaProperty : gmlObject.getTrafficArea()) {
            var trafficArea = trafficAreaProperty.getTrafficArea();
            if (trafficArea.getLod2MultiSurface() == null) {
                continue;
            }

            var multiSurface = trafficArea.getLod2MultiSurface().getMultiSurface();
            var lod2MultiSurface = new LOD2MultiSurfaceFactory(getTarget()).createLOD2MultiSurface(multiSurface);
            var trafficAreaView = new TrafficAreaView(trafficArea);
            trafficAreaView.setPolygons(lod2MultiSurface.getPolygons());
            trafficAreaView.setMultiSurfaceMeshView(lod2MultiSurface);
            trafficAreaViews.add(trafficAreaView);
            roadMultiSurfaceView.addPolygons(createPolygonViews(multiSurface));
        }
        roadMultiSurfaceView.setTrafficAreas(trafficAreaViews);

        var auxiliaryTrafficAreaViews = new ArrayList<AuxiliaryTrafficAreaView>();
        for (var auxiliaryTrafficAreaProperty : gmlObject.getAuxiliaryTrafficArea()) {
            var auxiliaryTrafficArea = auxiliaryTrafficAreaProperty.getAuxiliaryTrafficArea();
            if (auxiliaryTrafficArea.getLod2MultiSurface() == null) {
                continue;
            }

            var multiSurface = auxiliaryTrafficArea.getLod2MultiSurface().getMultiSurface();
            var lod2MultiSurface = new LOD2MultiSurfaceFactory(getTarget()).createLOD2MultiSurface(multiSurface);
            var auxiliaryTrafficAreaView = new AuxiliaryTrafficAreaView(auxiliaryTrafficArea);
            auxiliaryTrafficAreaView.setPolygons(lod2MultiSurface.getPolygons());
            auxiliaryTrafficAreaView.setMultiSurfaceMeshView(lod2MultiSurface);
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
