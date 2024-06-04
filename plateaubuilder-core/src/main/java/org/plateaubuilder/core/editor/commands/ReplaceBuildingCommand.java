package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.CityModel;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.BuildingViewFactory;

public class ReplaceBuildingCommand extends AbstractCityGMLFeatureUndoableCommand<AbstractBuilding> {

    public ReplaceBuildingCommand(CityModel cityModel, AbstractBuilding oldFeature, AbstractBuilding newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<AbstractBuilding> createView(CityModelGroup group, CityModelView cityModelView, AbstractBuilding feature) {
        return new BuildingViewFactory(group, cityModelView).create(feature);
    }
}
