package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.WaterBodyViewFactory;

public class ReplaceWaterBodyCommand extends AbstractCityGMLFeatureUndoableCommand<WaterBody> {

    public ReplaceWaterBodyCommand(CityModel cityModel, WaterBody oldFeature, WaterBody newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<WaterBody> createView(CityModelGroup group, CityModelView cityModelView, WaterBody feature) {
        return new WaterBodyViewFactory(group, cityModelView).create(feature);
    }
}
