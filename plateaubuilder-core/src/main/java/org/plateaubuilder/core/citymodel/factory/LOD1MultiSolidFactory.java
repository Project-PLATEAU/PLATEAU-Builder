package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD1SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

import java.util.ArrayList;
import java.util.List;

public class LOD1MultiSolidFactory extends GeometryFactory {

    public LOD1MultiSolidFactory(CityModelView target) {
        super(target);
    }

    public LOD1MultiSolidView createLOD1MultiSolid(PlantCover gmlObject) {
        if (gmlObject.getLod1MultiSolid() == null) {
            return null;
        }

        var multiSolid = gmlObject.getLod1MultiSolid();
        return multiSolid != null ? createLOD1MultiSolid(multiSolid.getObject()) : null;
    }

    private LOD1MultiSolidView createLOD1MultiSolid(MultiSolid multiSolid) {
        List<SolidProperty> solidMember = multiSolid.getSolidMember();

        var polygons = new ArrayList<PolygonView>();
        for (SolidProperty solidMemberElement : solidMember) {
            var solid = (Solid) solidMemberElement.getSolid();
            var exterior = solid.getExterior();
            var compositeSurface = (CompositeSurface) exterior.getObject();

            List<SurfaceProperty> surfaceMember = compositeSurface.getSurfaceMember();
            for (SurfaceProperty surfaceMemberElement : surfaceMember) {
                var polygon = (Polygon) surfaceMemberElement.getSurface();
                if (polygon == null) {
                    continue;
                }

                var polygonObject = createPolygon(polygon);
                polygons.add(polygonObject);
            }
        }

        var mesh = createTriangleMesh(polygons);
        var meshView = new MeshView();
        meshView.setMesh(mesh);
        meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());

        var multiSolidView = new LOD1MultiSolidView(multiSolid, vertexBuffer, texCoordBuffer);
        multiSolidView.setPolygons(polygons);
        multiSolidView.addMeshView(meshView);

        return multiSolidView;
    }
}
