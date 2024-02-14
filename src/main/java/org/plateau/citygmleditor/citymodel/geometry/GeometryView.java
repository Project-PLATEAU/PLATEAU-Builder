package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citygmleditor.TransformManipulator;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import javafx.scene.shape.MeshView;
import java.util.ArrayList;

public class GeometryView extends MeshView implements ILODSolidView {
    private AbstractGeometry gmlObject;
    private ArrayList<PolygonView> polygons;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);

    public GeometryView(AbstractGeometry gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
    }

    public ArrayList<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }

    @Override
    public AbstractSolid getAbstractSolid() {
        return null;
    }

    @Override
    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public MeshView getMeshView() {
        return this;
    }

    @Override
    public TexCoordBuffer getTexCoordBuffer() {
        return texCoordBuffer;
    }

    @Override
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    @Override
    public void refrectGML() {
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();//.getOriginal().getPosList().toList3d();
            polygon.getExteriorRing().getOriginal().getPosList().setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();//.getOriginal().getPosList().toList3d();
                polygon.getExteriorRing().getOriginal().getPosList().setValue(transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertexBuffer = new VertexBuffer();
        var vertices = transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }
}
