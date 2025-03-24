package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.LandUseViewFactory;

public class ReplaceLandUseCommand extends AbstractCityGMLFeatureUndoableCommand<LandUse> {

    public ReplaceLandUseCommand(CityModel cityModel, LandUse oldFeature, LandUse newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<LandUse> createView(CityModelGroup group, CityModelView cityModelView, LandUse feature) {
        return new LandUseViewFactory(group, cityModelView).create(feature);
    }
}
