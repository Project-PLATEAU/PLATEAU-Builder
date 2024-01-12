package org.plateau.citygmleditor.citymodel.factory;

import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurfaceView;
import org.plateau.citygmleditor.citymodel.geometry.LOD3SolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;
import java.util.List;

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
            var boundarySurface = boundedBySurface.getBoundarySurface();

            // <bldg:lod3MultiSurface>
            if (boundarySurface.getLod3MultiSurface() != null) {
                var boundary = new BoundarySurfaceView(boundarySurface);
                boundaries.add(boundary);

                var boundaryPolygons = new ArrayList<PolygonView>();
                for (var surfaceMember : boundarySurface.getLod3MultiSurface().getMultiSurface().getSurfaceMember()) {
                    var polygon = (Polygon) surfaceMember.getSurface();
                    if (polygon == null)
                        continue;

                    var polygonObject = createPolygon(polygon);
                    boundaryPolygons.add(polygonObject);

                    // ノードの最小単位＝ポリゴン
                    var material = polygonObject.getSurfaceData() != null ? polygonObject.getSurfaceData().getMaterial() : null;
                    var polygonMesh = new ArrayList<PolygonView>(List.of(polygonObject));
                    var meshView = new MeshView();
                    meshView.setMesh(createTriangleMesh(polygonMesh));
                    meshView.setMaterial(material != null ? material : World.getActiveInstance().getDefaultMaterial());
                    meshView.setId(surfaceMember.getGeometry().getId());
                    solid.addMeshView(boundarySurface.getId(), meshView);
                }
                boundary.setPolygons(boundaryPolygons);
            }
            // <bldg:opening>
            if (boundarySurface.getOpening() != null) {
                var boundary = new BoundarySurfaceView(boundarySurface);
                boundaries.add(boundary);

                var boundaryPolygons = new ArrayList<PolygonView>();
                for (var opening : boundarySurface.getOpening()) {
                    for (var surfaceMember : opening.getOpening().getLod3MultiSurface().getMultiSurface().getSurfaceMember()) {
                        var polygon = (org.citygml4j.model.gml.geometry.primitives.Polygon) surfaceMember.getSurface();
                        if (polygon == null)
                            continue;

                        var polygonObject = createPolygon(polygon);
                        boundaryPolygons.add(polygonObject);

                        // ノードの最小単位＝ポリゴン
                        var material = polygonObject.getSurfaceData() != null ? polygonObject.getSurfaceData().getMaterial() : null;
                        var polygonMesh = new ArrayList<PolygonView>(List.of(polygonObject));
                        var meshView = new MeshView();
                        meshView.setMesh(createTriangleMesh(polygonMesh));
                        meshView.setMaterial(material != null ? material : World.getActiveInstance().getDefaultMaterial());
                        meshView.setId(surfaceMember.getGeometry().getId());
                        // TODO どの壁に属するのかわからなくなる？
                        //solid.addMeshView(boundarySurface.getId(), meshView);
                        solid.addMeshView(opening.getOpening().getId(), meshView);
                    }
                }
                boundary.setPolygons(boundaryPolygons);
            }
        }
        solid.setBoundaries(boundaries);

        return solid;
    }
}
