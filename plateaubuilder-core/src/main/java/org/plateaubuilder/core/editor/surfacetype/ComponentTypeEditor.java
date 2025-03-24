package org.plateaubuilder.core.editor.surfacetype;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.citygml4j.model.citygml.CityGMLClass;
import org.plateaubuilder.core.editor.Outline;
import org.plateaubuilder.core.utils3d.polygonmesh.FaceBuffer;

import java.util.Collections;
import java.util.List;

public class ComponentTypeEditor extends PerModeSurfaceEditor {
    private final ObjectProperty<List<PolygonSection>> selectedSections = new SimpleObjectProperty<>();

    public List<PolygonSection> getSelectedSections() {
        return selectedSections.get();
    }

    public ObjectProperty<List<PolygonSection>> selectedSectionsProperty() {
        return selectedSections;
    }

    @Override
    public void handleClick(BuildingSurfaceTypeView clickedView, PolygonSection clickedSection) {
        targetViewProperty().set(clickedView);
        selectedSections.set(clickedView.getComponentSection(clickedSection));
    }

    public void refreshOutLine(Outline outLine) {
        if (targetViewProperty().get() == null)
            return;

        FaceBuffer selectedFaces = new FaceBuffer();
        for (PolygonSection section : selectedSections.get()) {
            selectedFaces.addFaces(section.getFaceBuffer().getBuffer());
        }
        targetViewProperty().get().updateSelectionOutLine(selectedFaces, outLine);
    }

    public void reset() {
        selectedSections.set(Collections.emptyList());
    }

    public void setSurfaceTypeOfSelectedSections(CityGMLClass clazz) {
        if (targetViewProperty().get() == null || selectedSections.get() == null || selectedSections.get().isEmpty())
            return;

        var section = selectedSections.get().get(0);

        if (section.getFeature().getCityGMLClass() == clazz)
            return;

        if (section.isOpening())
            getManipulator().changeCityGMLClass(section.getOpening(), clazz);
        else
            getManipulator().changeCityGMLClass(section.getBoundedBy(), clazz);

        targetViewProperty().get().updateView(getManipulator().getPropertyMap());
    }
}
