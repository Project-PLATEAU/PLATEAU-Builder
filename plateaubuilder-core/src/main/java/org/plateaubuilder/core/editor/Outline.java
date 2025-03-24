package org.plateaubuilder.core.editor;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;

public class Outline extends MeshView {
    public Outline() {
        initialize(Color.web("#ff330033"));
    }

    public Outline(Color color) {
        initialize(color);
    }

    private void initialize(Color color) {
        var material = new PhongMaterial();
        material.setDiffuseColor(new Color(1, 1, 0, 0.3));
        WritableImage image = new WritableImage(1, 1);
        PixelWriter writer = image.getPixelWriter();
        writer.setColor(0, 0, color);

        material.setSelfIlluminationMap(image);
        setMaterial(material);
        setDrawMode(DrawMode.FILL);
        setOpacity(0.3);
        setViewOrder(-1);
        setMouseTransparent(true);
    }

    public void clear() {
        setMesh(null);
    }
}
