package org.plateaubuilder.core.io.mesh.exporters;

import javafx.scene.paint.Color;

public class MaterialModel {
    private String name;
    private String fileName;
    private String materialUrl;
    private Color diffuseColor;
    private Color specularColor;
    private Color emissiveColor;

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

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Color diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Color specularColor) {
        this.specularColor = specularColor;
    }

    public Color getEmissiveColor() {
        return emissiveColor;
    }

    public void setEmissiveColor(Color emissiveColor) {
        this.emissiveColor = emissiveColor;
    }

    public boolean hasFileName() {
        return fileName != null;
    }
}
