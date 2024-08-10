package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.world.World;

import javafx.scene.Node;

public abstract class AbstractCityGMLFeatureUndoableCommand<T extends AbstractCityObject> extends AbstractCityGMLUndoableCommand {
    private final CityModel cityModel;
    private final T oldFeature;
    private final T newFeature;

    public AbstractCityGMLFeatureUndoableCommand(CityModel cityModel, T oldFeature, T newFeature) {
        this.cityModel = cityModel;
        this.oldFeature = oldFeature;
        this.newFeature = newFeature;
    }

    public void redo() {
        replace(cityModel, oldFeature, newFeature);
    }

    public void undo() {
        replace(cityModel, newFeature, oldFeature);
    }

    public Node getUndoFocusTarget() {
        var group = World.getActiveInstance().getCityModelGroup();
        return group.findView(oldFeature);
    }

    public Node getRedoFocusTarget() {
        var group = World.getActiveInstance().getCityModelGroup();
        return group.findView(newFeature);
    }

    protected void replace(CityModel cityModel, T oldFeature, T newFeature) {
        CityObjectMember cityObjectMember = null;
        for (var member : cityModel.getCityObjectMember()) {
            if (member.getCityObject() == oldFeature) {
                cityObjectMember = member;
                break;
            }
        }
        if (cityObjectMember == null) {
            throw new RuntimeException();
        }

        var group = World.getActiveInstance().getCityModelGroup();
        var cityModelView = (CityModelView) group.findView(cityModel);
        var oldView = (ManagedGMLView<T>) group.findView(oldFeature);
        cityModelView.removeFeature(group, oldView);

        cityObjectMember.setCityObject(newFeature);
        var newView = createView(group, cityModelView, newFeature);
        cityModelView.addFeature(group, newView);
    }

    abstract protected ManagedGMLView<T> createView(CityModelGroup group, CityModelView cityModelView, T feature);
}
