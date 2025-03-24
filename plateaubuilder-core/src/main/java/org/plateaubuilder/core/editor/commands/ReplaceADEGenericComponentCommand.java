package org.plateaubuilder.core.editor.commands;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.citymodel.factory.ADEGenericComponentViewFactory;
import org.plateaubuilder.core.world.World;

public class ReplaceADEGenericComponentCommand extends AbstractCityGMLFeatureUndoableCommand<ADEGenericComponent> {

    public ReplaceADEGenericComponentCommand(CityModel cityModel, ADEGenericComponent oldFeature, ADEGenericComponent newFeature) {
        super(cityModel, oldFeature, newFeature);
    }

    @Override
    protected ManagedGMLView<ADEGenericComponent> createView(CityModelGroup group, CityModelView cityModelView, ADEGenericComponent feature) {
        return new ADEGenericComponentViewFactory(group, cityModelView).create(feature);
    }

    @Override
    protected void replace(CityModel cityModel, ADEGenericComponent oldFeature, ADEGenericComponent newFeature) {
        CityObjectMember cityObjectMember = null;
        var oldId = oldFeature.getId();
        for (var member : cityModel.getCityObjectMember()) {
            if (member.isSetGenericADEElement()) {
                var component = new ADEGenericComponent(member.getGenericADEElement());
                if (component.getId().equals(oldId)) {
                    cityObjectMember = member;
                    break;
                }
            }
        }
        if (cityObjectMember == null) {
            throw new RuntimeException();
        }

        var group = World.getActiveInstance().getCityModelGroup();
        var cityModelView = (CityModelView) group.findView(cityModel);
        var oldView = (ManagedGMLView<ADEGenericComponent>) group.findView(oldFeature);
        cityModelView.removeFeature(group, oldView);
        cityObjectMember.unsetCityObject();
        cityObjectMember.setGenericADEElement(newFeature.getGenericADEElement().get(0));
        var newView = createView(group, cityModelView, newFeature);
        cityModelView.addFeature(group, newView);
    }
}
