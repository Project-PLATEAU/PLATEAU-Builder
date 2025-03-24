package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.SolitaryVegetationObjectViewFactory;

public class ReplaceSolitaryVegetationObjectCommand extends AbstractCityGMLFeatureUndoableCommand<SolitaryVegetationObject> {
    public ReplaceSolitaryVegetationObjectCommand(CityModel cityModel, SolitaryVegetationObject oldFeature, SolitaryVegetationObject newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<SolitaryVegetationObject> createView(CityModelGroup group, CityModelView cityModelView, SolitaryVegetationObject feature) {
        return new SolitaryVegetationObjectViewFactory(group, cityModelView).create(feature);
    }
}
