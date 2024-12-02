package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.citygml.core.CityModel;
import org.plateaubuilder.core.citymodel.BuildingInstallationView;
import org.plateaubuilder.core.citymodel.BuildingView;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.factory.BuildingInstallationViewFactory;
import org.plateaubuilder.core.world.World;

public class ReplaceBuildingInstallationCommand extends AbstractCityGMLFeatureUndoableCommand<BuildingInstallation> {

    public ReplaceBuildingInstallationCommand(CityModel cityModel, BuildingInstallation oldFeature, BuildingInstallation newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<BuildingInstallation> createView(CityModelGroup group, CityModelView cityModelView, BuildingInstallation feature) {
        return new BuildingInstallationViewFactory(group, cityModelView).create(feature);
    }

    @Override
    protected void replace(CityModel cityModel, BuildingInstallation oldFeature, BuildingInstallation newFeature) {
        AbstractBuilding parent = null;
        for (var member : cityModel.getCityObjectMember()) {
            var cityObject = member.getCityObject();
            if (cityObject instanceof AbstractBuilding) {
                var building = (AbstractBuilding) cityObject;
                for (var buildingInstallationProperty : building.getOuterBuildingInstallation()) {
                    var buildingInstallation = buildingInstallationProperty.getBuildingInstallation();
                    if (buildingInstallation.getId().equals(oldFeature.getId())) {
                        parent = building;
                        break;
                    }
                }
            }
        }
        if (parent == null) {
            throw new RuntimeException();
        }

        var group = World.getActiveInstance().getCityModelGroup();
        var cityModelView = (CityModelView) group.findView(cityModel);
        var buildingView = (BuildingView) group.findView(parent);
        cityModelView.removeFeature(group, buildingView);
        buildingView.removeBuildingInstallationView(oldFeature.getId());

        var newView = (BuildingInstallationView) createView(group, cityModelView, newFeature);
        buildingView.addBuildingInstallationView(newView);
        cityModelView.addFeature(group, buildingView);
    }
}
