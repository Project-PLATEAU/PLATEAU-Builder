package org.plateaubuilder.core.io.mesh.exporters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectModel {
    private String name;
    private float[] vertices;
    private float[] uvs;
    private Map<String, MaterialModel> materialModelsMap = new HashMap<>();
    private Map<String, int[]> facesMap = new HashMap<>();

    public ObjectModel(String name, float[] vertices, float[] uvs) {
        this.name = name;
        this.vertices = vertices;
        this.uvs = uvs;
    }

    public String getName() {
        return name;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getUVs() {
        return uvs;
    }

    public int getVertexCount() {
        return vertices.length / 3;
    }

    public int getUVCount() {
        return uvs.length / 2;
    }

    public List<String> getMaterialNames() {
        return new ArrayList<String>(materialModelsMap.keySet());
    }

    public MaterialModel getMaterial(String name) {
        return materialModelsMap.get(name);
    }

    public int[] getFaces(String name) {
        return facesMap.get(name);
    }

    public void addMaterialFaces(String name, MaterialModel materialModel, int[] faces) {
        materialModelsMap.put(name, materialModel);
        facesMap.put(name, faces);
    }
}
