package org.plateaubuilder.core.io.mesh.exporters;

public class MaterialModel {
    private String name;
    private String fileName;
    private String materialUrl;

    public MaterialModel(String name) {
        this(name, null, null);
    }

    public MaterialModel(String name, String fileName, String materialUrl) {
        this.name = name;
        this.fileName = fileName;
        this.materialUrl = materialUrl;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMaterialUrl() {
        return materialUrl;
    }

    public boolean hasFileName() {
        return fileName != null;
    }
}
