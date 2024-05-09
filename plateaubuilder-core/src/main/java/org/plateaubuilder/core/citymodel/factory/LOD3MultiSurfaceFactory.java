package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.citymodel.geometry.TrafficAreaView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD3MultiSurfaceFactory extends GeometryFactory {

    public LOD3MultiSurfaceFactory(CityModelView target) {
        super(target);
    }

    public LOD3MultiSurfaceView createLOD3MultiSurface(Road gmlObject) {
        if (gmlObject.getLod3MultiSurface() == null) {
            return null;
        }

        var multiSurfaceView = new LOD3MultiSurfaceView(gmlObject.getLod3MultiSurface().getObject(), vertexBuffer, texCoordBuffer);

        var trafficAreaViews = new ArrayList<TrafficAreaView>();

        for (var trafficArea : gmlObject.getTrafficArea()) {
            if (trafficArea.getTrafficArea().getLod3MultiSurface() == null) {
                continue;
            }

            var trafficAreaView = createTrafficArea(trafficArea);
            trafficAreaViews.add(trafficAreaView);
        }
        multiSurfaceView.setTrafficAreaViews(trafficAreaViews);

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

        multiSurfaceView.addSurfaceTypeView(gmlObject);

        return multiSurfaceView;
    }

    private TrafficAreaView createTrafficArea(TrafficAreaProperty trafficArea) {
        var trafficAreaView = new TrafficAreaView(trafficArea.getTrafficArea());
        var multiSurface = trafficArea.getTrafficArea().getLod3MultiSurface().getMultiSurface();
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

        trafficAreaView.setPolygons(polygons);
        return trafficAreaView;
    }
}
