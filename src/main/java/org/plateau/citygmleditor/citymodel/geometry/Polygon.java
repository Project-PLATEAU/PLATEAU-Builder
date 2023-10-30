package org.plateau.citygmleditor.citymodel.geometry;

import org.plateau.citygmleditor.citymodel.GMLObject;
import org.plateau.citygmleditor.citymodel.SurfaceData;

public class Polygon extends GMLObject<org.citygml4j.model.gml.geometry.primitives.Polygon> {
    private int[] faces;

    private LinearRing exteriorRing;

    // TODO: InteriorRing対応
    //private ArrayList<LinearRing> interiorRings = new ArrayList<>();

    public Polygon(org.citygml4j.model.gml.geometry.primitives.Polygon original) {
        super(original);
    }

    public int[] getFaces() {
        return faces;
    }

    public void setFaces(int[] faces) {
        this.faces = faces;
    }

    public float[] getAllUVs() {
        var uvs = new float[getAllUVsSize()];
        var index = 0;
        for (var uv : exteriorRing.getUVs()) {
            uvs[index++] = uv;
        }

        return uvs;
    }

    public int getAllUVsSize() {
        var uvSize = 0;
        if (exteriorRing == null)
            return uvSize;

        uvSize += exteriorRing.getUVs().length;

        return uvSize;
    }

    public float[] getAllVertices() {
        var vertices = new float[getAllVerticesSize()];
        var index = 0;
        for (var vertex : exteriorRing.getVertices()) {
            vertices[index++] = vertex;
        }

        return vertices;
    }

    public int getAllVerticesSize() {
        var vertexSize = 0;
        if (exteriorRing == null)
            return vertexSize;

        vertexSize += exteriorRing.getVertices().length;

        return vertexSize;
    }

    public LinearRing getExteriorRing() {
        return this.exteriorRing;

    }
    public void setExteriorRing(LinearRing exteriorRing) {
        this.exteriorRing = exteriorRing;
    }

    public SurfaceData getSurfaceData() {
        // Polygon内のAppearanceはすべて同じであること前提

        if (this.exteriorRing == null)
            return null;

        return this.exteriorRing.getSurfaceData();
    }
}
