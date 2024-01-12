package org.plateau.citygmleditor.world;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.util.Duration;
import org.plateau.citygmleditor.citygmleditor.AutoScalingGroup;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;

public class AxisGizmo {
    private Box xAxis, yAxis, zAxis;
    private Sphere xSphere, ySphere, zSphere;
    private AutoScalingGroup autoScalingGroup;

    private SimpleBooleanProperty showAxis = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (xAxis == null) {
                    createAxis();
                }
                autoScalingGroup.getChildren().addAll(xAxis, yAxis, zAxis);
                autoScalingGroup.getChildren().addAll(xSphere, ySphere, zSphere);
            } else if (xAxis != null) {
                autoScalingGroup.getChildren().removeAll(xAxis, yAxis, zAxis);
                autoScalingGroup.getChildren().removeAll(xSphere, ySphere, zSphere);
            }
        }
    };

    public boolean getShowAxis() {
        return showAxis.get();
    }

    public SimpleBooleanProperty showAxisProperty() {
        return showAxis;
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis.set(showAxis);
    }

    private void createAxis() {
        double length = 200.0;
        double width = 1.0;
        double radius = 2.0;
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        xSphere = new Sphere(radius);
        ySphere = new Sphere(radius);
        zSphere = new Sphere(radius);
        xSphere.setMaterial(redMaterial);
        ySphere.setMaterial(greenMaterial);
        zSphere.setMaterial(blueMaterial);

        xSphere.setTranslateX(100.0);
        ySphere.setTranslateY(100.0);
        zSphere.setTranslateZ(100.0);

        xAxis = new Box(length, width, width);
        yAxis = new Box(width, length, width);
        zAxis = new Box(width, width, length);
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
    }

    public AxisGizmo() {
        this.autoScalingGroup = CityGMLEditorApp.getAutoScalingGroup();
    }
}
