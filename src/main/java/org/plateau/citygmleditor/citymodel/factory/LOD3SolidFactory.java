package org.plateau.citygmleditor.citymodel.factory;

import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.LOD3SolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LOD3SolidFactory extends GeometryFactory {
    protected LOD3SolidFactory(CityModelView target) {
        super(target);
    }

    public LOD3SolidView createLOD3Solid(AbstractBuilding gmlObject) {
        if (gmlObject.getLod3Solid() == null)
            return null;

        var solid = new LOD3SolidView(gmlObject.getLod3Solid().getObject(), vertexBuffer, texCoordBuffer);

        var boundaries = new ArrayList<BoundarySurfaceView>();

        // <bldg:boundedBy>
        for (var boundedBySurface : gmlObject.getBoundedBySurface()) {
            
            if ((boundedBySurface.getBoundarySurface().getLod3MultiSurface() == null) &&
                (boundedBySurface.getBoundarySurface().getOpening() == null))
                continue;

            var boundary = createBoundary(boundedBySurface);
            boundaries.add(boundary);
        }
        solid.setBoundaries(boundaries);

        var polygonsMap = solid.getSurfaceDataPolygonsMap();
        // 1メッシュにつき1マテリアルしか登録できないため、マテリアルごとに別メッシュとして生成
        for (Map.Entry<SurfaceDataView, ArrayList<PolygonView>> entry : polygonsMap.entrySet()) {
            var meshView = new MeshView();
            meshView.setMesh(createTriangleMesh(entry.getValue()));
            if (entry.getKey() == null) {
                meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());
            } else {
                meshView.setMaterial(entry.getKey().getMaterial());
            }
            solid.addMeshView(meshView);
        }

        return solid;
    }
    
    private BoundarySurfaceView createBoundary(BoundarySurfaceProperty boundedBySurface) {
        var boundary = new BoundarySurfaceView(boundedBySurface.getBoundarySurface());

        // <bldg:lod3MultiSurface>
        if (boundedBySurface.getBoundarySurface().getLod3MultiSurface() != null) {
            var boundaryPolygons = new ArrayList<PolygonView>();
            for (var surfaceMember : boundedBySurface.getBoundarySurface().getLod3MultiSurface().getMultiSurface().getSurfaceMember()) {
                var polygon = (Polygon) surfaceMember.getSurface();
                if (polygon == null)
                    continue;
                var polygonObject = createPolygon(polygon);
                boundaryPolygons.add(polygonObject);
            }
            boundary.setPolygons(boundaryPolygons);
        }
        // <bldg:opening>
        if (boundedBySurface.getBoundarySurface().getOpening() != null) {
            var boundaryPolygons = new ArrayList<PolygonView>();
            for (var opening : boundedBySurface.getBoundarySurface().getOpening()) {
                for (var surfaceMember : opening.getOpening().getLod3MultiSurface().getMultiSurface().getSurfaceMember()) {
                    var polygon = (Polygon) surfaceMember.getSurface();
                    if (polygon == null)
                        continue;
                    var polygonObject = createPolygon(polygon);
                    boundaryPolygons.add(polygonObject);
                }
            }
            boundary.setOpeningPolygons(boundaryPolygons);
        }
        return boundary;
    }
}
