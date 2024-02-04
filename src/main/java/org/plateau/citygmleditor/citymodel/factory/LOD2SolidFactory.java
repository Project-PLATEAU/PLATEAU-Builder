package org.plateau.citygmleditor.citymodel.factory;

import java.util.HashMap;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.MeshViewUserData;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;
import java.util.Map;

public class LOD2SolidFactory extends GeometryFactory {
    protected LOD2SolidFactory(CityModelView target) {
        super(target);
    }

    public LOD2SolidView createLOD2Solid(AbstractBuilding gmlObject) {
        if (gmlObject.getLod2Solid() == null)
            return null;

        var solid = new LOD2SolidView(gmlObject.getLod2Solid().getObject(), vertexBuffer, texCoordBuffer);

        var boundaries = new ArrayList<BoundarySurfaceView>();

        for (var boundedBySurface : gmlObject.getBoundedBySurface()) {
            if (boundedBySurface.getBoundarySurface().getLod2MultiSurface() == null)
                continue;

            var boundary = createBoundary(boundedBySurface);
            boundaries.add(boundary);
        }
        solid.setBoundaries(boundaries);

        boundaries.forEach(
            boundary -> boundary.getPolygons().forEach(
                polygon -> {
                  var meshView = new MeshView();
                  meshView.setUserData(new MeshViewUserData(polygon.getGMLID(), boundary.getGMLID()));
                  meshView.setMesh(createTriangleMesh(List.of(polygon)));
                  meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());
                  solid.addMeshView(meshView);
                }
            ));

        return solid;
    }

    private BoundarySurfaceView createBoundary(BoundarySurfaceProperty boundedBySurface) {
        var boundary = new BoundarySurfaceView(boundedBySurface.getBoundarySurface());

        var boundaryPolygons = new ArrayList<PolygonView>();
        for (var surfaceMember : boundedBySurface.getBoundarySurface().getLod2MultiSurface().getMultiSurface().getSurfaceMember()) {
            var polygon = (Polygon) surfaceMember.getSurface();
            if (polygon == null)
                continue;

            var polygonObject = createPolygon(polygon);
            boundaryPolygons.add(polygonObject);
        }

        boundary.setPolygons(boundaryPolygons);
        return boundary;
    }
}
