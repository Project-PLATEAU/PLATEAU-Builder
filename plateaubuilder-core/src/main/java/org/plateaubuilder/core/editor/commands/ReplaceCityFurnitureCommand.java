package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.CityModel;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.CityFurnitureViewFactory;

public class ReplaceCityFurnitureCommand extends AbstractCityGMLFeatureUndoableCommand<CityFurniture> {
    public ReplaceCityFurnitureCommand(CityModel cityModel, CityFurniture oldFeature, CityFurniture newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<CityFurniture> createView(CityModelGroup group, CityModelView cityModelView, CityFurniture feature) {
        return new CityFurnitureViewFactory(group, cityModelView).create(feature);
    }
}
