package org.plateau.plateaubuilder.control.surfacetype;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.BuildingModuleComponent;
import org.plateau.plateaubuilder.control.Outline;
import org.plateau.plateaubuilder.utils3d.polygonmesh.FaceBuffer;
import org.plateau.plateaubuilder.world.World;

import java.util.ArrayList;
import java.util.List;

public class ComponentOpeningEditor extends PerModeSurfaceEditor {
    public void reset() {
        targetComponent.set(null);
        selectedComponents.clear();
        openingOutLine.clear();
        mode.set(EditorMode.SELECT_COMPONENT);
    }

    public enum EditorMode {
        SELECT_COMPONENT,
        EDIT_OPENINGS
    }

    private final ObjectProperty<EditorMode> mode = new SimpleObjectProperty<>();
    {
        mode.set(EditorMode.SELECT_COMPONENT);
    }

    public EditorMode getMode() {
        return mode.get();
    }

    public ObjectProperty<EditorMode> modeProperty() {
        return mode;
    }

    private final List<BuildingModuleComponent> selectedComponents = new ArrayList<>();
    private final ObjectProperty<BoundarySurfaceProperty> targetComponent = new SimpleObjectProperty<>();

    private final BooleanProperty selectedTargetProperty = new SimpleBooleanProperty(false);
    {
        targetComponent.addListener(((observable, oldValue, newValue) -> {
            var isSelected = newValue != null;
            if (selectedTargetProperty.get() != isSelected) {
                selectedTargetProperty.set(isSelected);
            }
        }));
    }

    public BooleanProperty selectedTargetProperty() {
        return selectedTargetProperty;
    }

    private final Outline openingOutLine = new Outline(Color.web("#ff0000aa"));

    public ComponentOpeningEditor() {
        var node = (Group) World.getRoot3D();
        node.getChildren().add(openingOutLine);
    }

    @Override
    public void handleClick(BuildingSurfaceTypeView clickedView, PolygonSection clickedSection) {
        switch (mode.get()) {
            case SELECT_COMPONENT:
                if (clickedSection.isOpening() || clickedView.getTargetLod() < 3)
                    break;

                targetViewProperty().set(clickedView);
                targetComponent.set(clickedSection.getBoundedBy());
                selectedComponents.clear();
                selectedComponents.addAll(targetComponent.get().getBoundarySurface().getOpening());

                break;
            case EDIT_OPENINGS:
                if (clickedView != targetViewProperty().get() || clickedSection.getFeature() == targetComponent.get().getBoundarySurface())
                    break;

                var selectedComponent = clickedSection.getComponent();
                if (selectedComponents.contains(selectedComponent))
                    selectedComponents.remove(selectedComponent);
                else
                    selectedComponents.add(selectedComponent);
                break;
        }
    }

    public void refreshOutLine(Outline outLine) {
        if (targetComponent.get() == null) {
            outLine.clear();
            openingOutLine.clear();
            return;
        }

        FaceBuffer selectedFaces = new FaceBuffer();
        for (var section : targetViewProperty().get().getComponentSection(targetComponent.get())) {
            selectedFaces.addFaces(section.getFaceBuffer().getBuffer());
        }
        targetViewProperty().get().updateSelectionOutLine(selectedFaces, outLine);

        FaceBuffer selectedOpeningFaces = new FaceBuffer();
        for (var component : selectedComponents) {
            var sections = targetViewProperty().get().getComponentSection(component);
            for (var section : sections)
                selectedOpeningFaces.addFaces(section.getFaceBuffer().getBuffer());
        }
        targetViewProperty().get().updateSelectionOutLine(selectedOpeningFaces, openingOutLine);
    }

    public void applyEdits() {
        if (targetViewProperty().get() == null)
            return;

        getManipulator().moveComponentsIntoOpening(selectedComponents, targetComponent.get());

        targetViewProperty().get().updateView(getManipulator().getPropertyMap());
    }
}
