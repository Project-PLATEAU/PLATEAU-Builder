package org.plateau.citygmleditor.control.surfacetype;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.citygml4j.model.citygml.CityGMLClass;
import org.plateau.citygmleditor.control.OutLine;
import org.plateau.citygmleditor.control.PolygonSection;

public class PolygonTypeEditor extends PerModeSurfaceEditor {
    private final ObjectProperty<PolygonSection> selectedSection = new SimpleObjectProperty<>();

    public PolygonSection getSelectedSection() {
        return selectedSection.get();
    }

    public ObjectProperty<PolygonSection> selectedSectionProperty() {
        return selectedSection;
    }

    @Override
    public void handleClick(BuildingSurfaceTypeView clickedView, PolygonSection clickedSection) {
        selectedSection.set(clickedSection);
        targetViewProperty().set(clickedView);
    }

    public void refreshOutLine(OutLine outLine) {
        if (targetViewProperty().get() == null || selectedSection.get() == null)
            return;

        targetViewProperty().get().updateSelectionOutLine(selectedSection.get().getFaceBuffer(), outLine);
    }

    public CityGMLClass getSurfaceTypeOfSelectedSection() {
        if (selectedSection.get() == null)
            return null;

        return selectedSection.get().getCityGMLClass();
    }

    public void setSurfaceTypeOfSelectedSection(CityGMLClass clazz) {
        if (targetViewProperty().get() == null || selectedSection.get() == null)
            return;

        if (selectedSection.get().isOpening())
            getManipulator().moveSurfaceIntoNewOpening(selectedSection.get().getOpening(), clazz, selectedSection.get().getSurface());
        else
            getManipulator().moveSurfaceIntoNewBoundedBy(selectedSection.get().getBoundedBy(), clazz, selectedSection.get().getSurface());


        targetViewProperty().get().updateView(getManipulator().getPropertyMap());
    }

    public void reset() {
        selectedSection.set(null);
        targetViewProperty().set(null);
    }
}
