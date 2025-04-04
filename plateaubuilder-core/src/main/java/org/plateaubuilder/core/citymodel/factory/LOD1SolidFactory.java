package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.geometry.LOD1SolidView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.List;

public class LOD1SolidFactory extends GeometryFactory {

    public LOD1SolidFactory(CityModelView target) {
        super(target);
    }

    public LOD1SolidView createLOD1Solid(AbstractBuilding gmlObject) {
        if (gmlObject.getLod1Solid() == null) {
            return null;
        }

        var solid = (Solid)gmlObject.getLod1Solid().getObject();
        var exterior = solid.getExterior();
        var compositeSurface = (CompositeSurface) exterior.getObject();

        List<SurfaceProperty> surfaceMember = compositeSurface.getSurfaceMember();

        var polygons = new ArrayList<PolygonView>();

        for (SurfaceProperty surfaceMemberElement : surfaceMember) {
            var polygon = (Polygon) surfaceMemberElement.getSurface();
            if (polygon == null) {
                continue;
            }

            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);
        }

        var mesh = createTriangleMesh(polygons);
        var meshView = new MeshView();
        meshView.setMesh(mesh);
        meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());

        var solidView = new LOD1SolidView(solid, vertexBuffer, texCoordBuffer);
        solidView.setPolygons(polygons);
        solidView.addMeshView(meshView);

        return solidView;
    }
}
