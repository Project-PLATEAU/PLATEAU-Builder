package org.plateaubuilder.core.citymodel.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.geometry.AbstractMultiSurfaceMeshView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.world.World;

import javafx.scene.shape.MeshView;

public class LOD3MultiSurfaceFactory extends GeometryFactory {

    public LOD3MultiSurfaceFactory(CityModelView target) {
        super(target);
    }

    public LOD3MultiSurfaceView createLOD3MultiSurface(MultiSurface multiSurface) {
        var multiSurfaceView = new LOD3MultiSurfaceView(multiSurface, vertexBuffer, texCoordBuffer);
        multiSurfaceView.setPolygons(createPolygonViews(multiSurface));

        setMaterial(multiSurfaceView);

        return multiSurfaceView;
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
