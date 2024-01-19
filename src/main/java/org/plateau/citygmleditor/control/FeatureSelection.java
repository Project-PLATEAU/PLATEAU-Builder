package org.plateau.citygmleditor.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.world.World;

import org.plateau.citygmleditor.citymodel.BuildingView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

public class FeatureSelection {
    private final ObjectProperty<BuildingView> active = new SimpleObjectProperty<>();
    private final MeshView outLine = new MeshView();

    private final ObjectProperty<SurfacePolygonSection> activeSection = new SimpleObjectProperty<>();

    public FeatureSelection() {
        var material = new PhongMaterial();
        material.setDiffuseColor(new Color(1, 1, 0, 0.3));
        WritableImage image = new WritableImage(1, 1);
        PixelWriter writer = image.getPixelWriter();
        writer.setColor(0, 0, Color.web("#ff330033"));

        material.setSelfIlluminationMap(image);
        outLine.setMaterial(material);
        outLine.setDrawMode(DrawMode.FILL);
        outLine.setOpacity(0.3);
        outLine.setViewOrder(-1);

        var node = (Group) World.getRoot3D();
        node.getChildren().add(outLine);

        // モード切替時に選択をリセット
        var viewMode = CityGMLEditorApp.getCityModelViewMode();
        viewMode.isSurfaceViewModeProperty().addListener((observable) -> refreshOutLine());
        viewMode.lodProperty().addListener((observable) -> refreshOutLine());
    }

    public BuildingView getActive() {
        return active.get();
    }

    public ObjectProperty<BuildingView> getActiveFeatureProperty() {
        return active;
    }

    public ObjectProperty<SurfacePolygonSection> getSurfacePolygonSectionProperty() {
        return activeSection;
    }

    public void registerClickEvent(Scene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            PickResult pickResult = event.getPickResult();
            var newSelectedMesh = pickResult.getIntersectedNode();
            var feature = getBuilding(newSelectedMesh);

            if (feature == null)
                return;

            clear();

            var viewMode = CityGMLEditorApp.getCityModelViewMode();

            var solid = feature.getSolid(viewMode.getLOD());
            if (solid == null)
                return;

            active.set(feature);

            if (viewMode.isSurfaceViewModeProperty().get()) {
                activeSection.set(feature.getLOD2Solid().getSurfaceTypeView().getSection(pickResult));
                feature.getLOD2Solid().getSurfaceTypeView().updateSelectionOutLine(activeSection.get().polygon, outLine);
            } else {
                outLine.setMesh(solid.getTotalMesh());
            }

        });
    }

    public void clear() {
        active.set(null);
        activeSection.set(null);
        outLine.setMesh(null);
    }

    public void refreshOutLine() {
        outLine.setMesh(null);

        if (active.get() == null)
            return;
        
        var viewMode = CityGMLEditorApp.getCityModelViewMode();
        if (viewMode.isSurfaceViewModeProperty().get()) {
            var solid = active.get().getLOD2Solid();

            if (solid == null || solid.getSurfaceTypeView() == null || activeSection.get() == null)
                return;

            solid.getSurfaceTypeView().updateSelectionOutLine(activeSection.get().polygon, outLine);
            return;
        }

        var solid = active.get().getSolid(viewMode.getLOD());
        if (solid == null)
            return;

        outLine.setMesh(solid.getTotalMesh());
    }

    public MeshView getOutLine() {
        return outLine;
    }

    private BuildingView getBuilding(Node node) {
        while (node != null && !(node instanceof BuildingView)) {
            node = node.getParent();
        }
        return (BuildingView)node;
    }
}
