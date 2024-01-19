package org.plateau.citygmleditor.citymodel.geometry;

import javafx.scene.Parent;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citygmleditor.TransformManipulator;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LOD3SolidView extends Parent implements ILODSolidView {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurfaceView> boundaries;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);
    private final List<MeshView> meshViews = new ArrayList<>();

    public LOD3SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
    }

    public AbstractSolid getGmlObject() {
        return gmlObject;
    }

    public void setGmlObject(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<BoundarySurfaceView> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(ArrayList<BoundarySurfaceView> boundaries) {
        this.boundaries = boundaries;
    }

    public HashMap<SurfaceDataView, ArrayList<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, ArrayList<PolygonView>>();

        for (var boundary : boundaries) {
            if (boundary.getPolygons() != null)
            {
                for (var polygon : boundary.getPolygons()) {
                    map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                    map.get(polygon.getSurfaceData()).add(polygon);
                }   
            }
            if (boundary.getOpeningPolygons() != null) {
                for (var polygon : boundary.getOpeningPolygons()) {
                    map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                    map.get(polygon.getSurfaceData()).add(polygon);
                }
            }
        }

        return map;
    }

    public void addMeshView(MeshView meshView) {
        getChildren().add(meshView);
        meshViews.add(meshView);
    }

    @Override
    public ArrayList<PolygonView> getPolygons() {
        var polygons = new ArrayList<PolygonView>();

        for (var boundary : boundaries) {
            if (boundary.getPolygons() != null) {
                polygons.addAll(boundary.getPolygons());
            }
            if (boundary.getOpeningPolygons() != null) {
                polygons.addAll(boundary.getOpeningPolygons());
            }
        }

        return polygons;
    }

    @Override
    public VertexBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public TexCoordBuffer getTexCoordBuffer() {
        return this.texCoordBuffer;
    }

    @Override
    public AbstractSolid getAbstractSolid() {
        return gmlObject;
    }

    @Override
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    @Override
    public Mesh getTotalMesh() {
        if (meshViews.isEmpty())
            return null;
        var mesh = (TriangleMesh)meshViews.get(0).getMesh();
        var outMesh = new TriangleMesh();

        outMesh.getNormals().addAll(mesh.getNormals());
        outMesh.getPoints().addAll(mesh.getPoints());
        outMesh.getTexCoords().addAll(mesh.getTexCoords());

        for (var polygon : getPolygons()) {
            outMesh.getFaces().addAll(polygon.getFaceBuffer().getBufferAsArray());
        }

        return outMesh;
    }

    @Override
    public void refrectGML() {
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();// .getOriginal().getPosList().toList3d();
            polygon.getExteriorRing().getOriginal().getPosList()
                    .setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();// .getOriginal().getPosList().toList3d();
                polygon.getExteriorRing().getOriginal().getPosList().setValue(
                        transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertexBuffer = new VertexBuffer();
        var vertices =
                transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }
}
