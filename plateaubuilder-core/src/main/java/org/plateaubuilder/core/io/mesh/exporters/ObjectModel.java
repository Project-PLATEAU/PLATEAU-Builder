package org.plateaubuilder.core.io.mesh.exporters;

public class ObjectModel {
    private String name;
    private MaterialModel materialModel;
    private int[] faces;
    private float[] vertices;
    private float[] uvs;

    public ObjectModel(String name, int[] faces, float[] vertices, float[] uvs, MaterialModel materialModel) {
        this.name = name;
        this.faces = faces;
        this.vertices = vertices;
        this.uvs = uvs;
        this.materialModel = materialModel;
    }

    public String getName() {
        return name;
    }

    public MaterialModel getMaterial() {
        return materialModel;
    }

    public int[] getFaces() {
        return faces;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getUVs() {
        return uvs;
    }
}
