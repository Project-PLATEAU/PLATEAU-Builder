package org.plateaubuilder.core.editor.surfacetype;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.citygml4j.model.citygml.building.BuildingModuleComponent;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.plateaubuilder.core.editor.Outline;
import org.plateaubuilder.core.utils3d.polygonmesh.FaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class ComponentPolygonEditor extends PerModeSurfaceEditor {
    public void reset() {
        selectedSections.set(new ArrayList<>());
        targetProperty = null;
        mode.set(EditorMode.SELECT_COMPONENT);
    }

    public enum EditorMode {
        SELECT_COMPONENT,
        EDIT_POLYGONS
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

    private ObjectProperty<List<PolygonSection>> selectedSections = new SimpleObjectProperty<>();

    public List<PolygonSection> getSelectedSections() {
        return selectedSections.get();
    }

    public ObjectProperty<List<PolygonSection>> selectedSectionsProperty() {
        return selectedSections;
    }

    private BuildingModuleComponent targetProperty;

    @Override
    public void handleClick(BuildingSurfaceTypeView clickedView, PolygonSection clickedSection) {
        switch (mode.get()) {
            case SELECT_COMPONENT:
                targetViewProperty().set(clickedView);
                selectedSections.set(clickedView.getComponentSection(clickedSection));
                targetProperty = clickedSection.getComponent();
                break;
            case EDIT_POLYGONS:
                if (targetViewProperty().get() != clickedView)
                    break;

                if (selectedSections.get().contains(clickedSection))
                    selectedSections.get().remove(clickedSection);
                else
                    selectedSections.get().add(clickedSection);
                break;
        }
    }

    public void refreshOutLine(Outline outLine) {
        FaceBuffer selectedFaces = new FaceBuffer();
        for (PolygonSection section : selectedSections.get()) {
            selectedFaces.addFaces(section.getFaceBuffer().getBuffer());
        }
        targetViewProperty().get().updateSelectionOutLine(selectedFaces, outLine);
    }

    public void applyEdits() {
        if (targetViewProperty().get() == null || selectedSections.get() == null || selectedSections.get().isEmpty())
            return;

        var surfaces = new ArrayList<AbstractSurface>();
        var srcProperties = new ArrayList<BuildingModuleComponent>();

        for (var section : selectedSections.get()) {
            surfaces.add(section.getSurface());
            srcProperties.add(section.getComponent());
        }

        getManipulator().reconstructWithSurfaces(surfaces, srcProperties, targetProperty);

        targetViewProperty().get().updateView(getManipulator().getPropertyMap());
    }
}
