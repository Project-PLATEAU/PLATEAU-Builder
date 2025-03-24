package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.transportation.Road;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.RoadViewFactory;

public class ReplaceRoadCommand extends AbstractCityGMLFeatureUndoableCommand<Road> {

    public ReplaceRoadCommand(CityModel cityModel, Road oldFeature, Road newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<Road> createView(CityModelGroup group, CityModelView cityModelView, Road feature) {
        return new RoadViewFactory(group, cityModelView).create(feature);
    }
}
