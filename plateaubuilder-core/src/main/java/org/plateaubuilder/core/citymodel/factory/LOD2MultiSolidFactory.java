package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.geometry.AbstractMultiSolidMeshView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD2MultiSolidFactory extends GeometryFactory {

    public LOD2MultiSolidFactory(CityModelView target) {
        super(target);
    }

    public LOD2MultiSolidView createLOD2MultiSolid(PlantCover gmlObject) {
        if (gmlObject.getLod2MultiSolid() == null) {
            return null;
        }

        var multiSolid = gmlObject.getLod2MultiSolid();
        return multiSolid != null ? createLOD2MultiSolid(multiSolid.getObject()) : null;
    }

    public LOD2MultiSolidView createLOD2MultiSolid(MultiSolid multiSolid) {
        var multiSolidView = new LOD2MultiSolidView(multiSolid, vertexBuffer, texCoordBuffer);
        multiSolidView.setPolygons(createPolygonViews(multiSolid));

        setMaterial(multiSolidView);

        return multiSolidView;
    }

    private List<PolygonView> createPolygonViews(MultiSolid multiSolid) {
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

        return polygons;
    }

    private void setMaterial(AbstractMultiSolidMeshView multiSolidView) {
        var polygonsMap = multiSolidView.getSurfaceDataPolygonsMap();
        // 1メッシュにつき1マテリアルしか登録できないため、マテリアルごとに別メッシュとして生成
        for (Map.Entry<SurfaceDataView, List<PolygonView>> entry : polygonsMap.entrySet()) {
            var meshView = new MeshView();
            meshView.setMesh(createTriangleMesh(entry.getValue()));
            if (entry.getKey() == null) {
                meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());
            } else {
                meshView.setMaterial(entry.getKey().getMaterial());
            }
            multiSolidView.addMeshView(meshView);
        }
    }
}
