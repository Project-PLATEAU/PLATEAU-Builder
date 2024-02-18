package org.plateau.citygmleditor.control.surfacetype;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.shape.TriangleMesh;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.control.OutLine;
import org.plateau.citygmleditor.control.PolygonSection;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;

public abstract class PerModeSurfaceEditor {
    private final ObjectProperty<BuildingSurfaceTypeView> targetView = new SimpleObjectProperty<>();

    private BuildingModuleComponentManipulator manipulator;
    {
        targetView.addListener((observable, oldValue, newValue) -> {
            if (newValue != null)
                manipulator = new BuildingModuleComponentManipulator(newValue.getTargetBuilding(), newValue.getTargetLod());
        });
    }

    public PerModeSurfaceEditor() {
    }

    public void onClick(MouseEvent event) {
        var view = getSurfaceView(event.getPickResult());

        if (view == null)
            return;

        updateActiveFeatureSelection(event.getPickResult());

        var section = view.getPolygonSection(event.getPickResult());

        if (section == null)
            return;

        handleClick(view, section);
    }

    protected abstract void handleClick(BuildingSurfaceTypeView clickedView, PolygonSection clickedSection);

    public abstract void refreshOutLine(OutLine outLine);

    public abstract void reset();

    protected BuildingSurfaceTypeView getSurfaceView(PickResult pickResult) {
        var node = pickResult.getIntersectedNode();
        while (node != null && !(node instanceof BuildingView)) {
            node = node.getParent();
        }

        if (node == null)
            return null;

        var lodMode = CityGMLEditorApp.getCityModelViewMode().getLOD();

        return ((BuildingView) node).getSolid(lodMode).getSurfaceTypeView();
    }

    protected BuildingModuleComponentManipulator getManipulator() {
        return manipulator;
    }

    protected ObjectProperty<BuildingSurfaceTypeView> targetViewProperty() {
        return targetView;
    }

    protected void updateActiveFeatureSelection(PickResult pickResult) {
        var view = getSurfaceView(pickResult);
        var section = view.getPolygonSection(pickResult);
        if (section == null)
            return;

        if (!(section.getFeature() instanceof AbstractCityObject))
            return;

        CityGMLEditorApp.getFeatureSellection().activeCityObjectProperty().set((AbstractCityObject) section.getFeature());

        var building = getBuilding(view);

        if (building == null)
            return;

        CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty().set(building);
    }

    private BuildingView getBuilding(Node node) {
        while (node != null && !(node instanceof BuildingView)) {
            node = node.getParent();
        }
        return (BuildingView)node;
    }
}
