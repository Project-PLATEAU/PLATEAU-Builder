package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.PlantCoverViewFactory;

public class ReplacePlantCoverCommand extends AbstractCityGMLFeatureUndoableCommand<PlantCover> {
    public ReplacePlantCoverCommand(CityModel cityModel, PlantCover oldFeature, PlantCover newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<PlantCover> createView(CityModelGroup group, CityModelView cityModelView, PlantCover feature) {
        return new PlantCoverViewFactory(group, cityModelView).create(feature);
    }
}
