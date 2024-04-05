package org.plateaubuilder.core.editor;

import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import org.plateaubuilder.core.world.World;

public class SelectionOutline extends MeshView {
    public SelectionOutline() {
        var material = new PhongMaterial();
        material.setDiffuseColor(new Color(0, 0, 0, 1));
        WritableImage image = new WritableImage(1, 1);
        PixelWriter writer = image.getPixelWriter();
        writer.setColor(0, 0, Color.ORANGE);

        material.setSelfIlluminationMap(image);
        setMaterial(material);
        setDrawMode(DrawMode.LINE);
        setDepthTest(DepthTest.DISABLE);

        var node = (Group) World.getRoot3D();
        node.getChildren().add(this);
    }
}
