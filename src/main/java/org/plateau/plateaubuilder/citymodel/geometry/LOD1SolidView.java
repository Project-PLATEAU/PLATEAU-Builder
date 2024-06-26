package org.plateau.plateaubuilder.citymodel.geometry;

import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.plateau.plateaubuilder.control.transform.TransformManipulator;
import org.plateau.plateaubuilder.control.surfacetype.BuildingSurfaceTypeView;
import org.plateau.plateaubuilder.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.plateaubuilder.utils3d.polygonmesh.VertexBuffer;

import java.util.ArrayList;

public class LOD1SolidView extends MeshView implements ILODSolidView {
    private Solid gmlObject;
    private ArrayList<PolygonView> polygons;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);

    public LOD1SolidView(Solid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<PolygonView> getPolygons() {
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

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractSolid getAbstractSolid() {
        return gmlObject;
    }

    @Override
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    @Override
    public MeshView getMeshView() {
        return this;
    }

    @Override
    public Mesh getTotalMesh() {
        return getMesh();
    }

    @Override
    public BuildingSurfaceTypeView getSurfaceTypeView() {
        return null;
    }

    @Override
    public void reflectGML() {
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();//.getOriginal().getPosList().toList3d();
            polygon.getExteriorRing().getGML().getPosList().setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();//.getOriginal().getPosList().toList3d();
                interiorRing.getGML().getPosList().setValue(transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertices = transformManipulator.unprojectVertexTransforms(vertexBuffer.getVertices());
        var newVertexBuffer = new VertexBuffer();
        for (var vertex : vertices) {
            newVertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(newVertexBuffer);
    }
}
