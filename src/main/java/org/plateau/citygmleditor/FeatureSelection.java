package org.plateau.citygmleditor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.DepthTest;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.DrawMode;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.world.World;

import org.plateau.citygmleditor.citymodel.BuildingView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.shape.MeshView;

public class FeatureSelection {
    private ObjectProperty<BuildingView> active = new SimpleObjectProperty<>();
    private MeshView outLine;

    public BuildingView getActive() {
        return active.get();
    }

    public ObjectProperty<BuildingView> getActiveFeatureProperty() {
        return active;
    }

    public void registerClickEvent(Scene scene) {
        outLine = new MeshView();

        var material = new PhongMaterial();
        material.setDiffuseColor(Color.ORANGE);
        outLine.setMaterial(material);
        // outLine.setCullFace(CullFace.BACK);
        outLine.setDrawMode(DrawMode.LINE);
        outLine.setDepthTest(DepthTest.DISABLE);

        var node = (Group) World.getRoot3D();
        node.getChildren().add(outLine);

        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            PickResult pickResult = event.getPickResult();
            var newSelectedMesh = pickResult.getIntersectedNode();

            var feature = newSelectedMesh;
            while (feature != null && !(feature instanceof BuildingView)) {
                feature = feature.getParent();
            }

            if (feature == null)
                return;

            if (newSelectedMesh instanceof MeshView) {
                var meshView = (MeshView) newSelectedMesh;

                outLine.setMesh(meshView.getMesh());
            }

            this.active.set((BuildingView) feature);
        });
    }

    public MeshView getOutLine() {
        return outLine;
    }
}
