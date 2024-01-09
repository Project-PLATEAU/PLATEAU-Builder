package org.plateau.citygmleditor.citymodel.geometry;

import java.util.ArrayList;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.Solid;

import javafx.scene.shape.MeshView;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;

public class LOD1SolidView extends MeshView implements ILODSolidView {
    private Solid gmlObject;
    private ArrayList<PolygonView> polygons;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();

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
}
