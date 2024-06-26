package org.plateau.plateaubuilder.control.commands;

import javafx.scene.Node;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.citymodel.factory.CityObjectMemberFactory;
import org.plateau.plateaubuilder.world.World;

public class ReplaceBuildingCommand extends AbstractCityGMLUndoableCommand {
    private final CityModel cityModel;
    private final AbstractBuilding oldBuilding;
    private final AbstractBuilding newBuilding;

    public ReplaceBuildingCommand(CityModel cityModel, AbstractBuilding oldBuilding, AbstractBuilding newBuilding) {
        this.cityModel = cityModel;
        this.oldBuilding = oldBuilding;
        this.newBuilding = newBuilding;
    }

    public void redo() {
        replaceBuilding(cityModel, oldBuilding, newBuilding);
    }

    public void undo() {
        replaceBuilding(cityModel, newBuilding, oldBuilding);
    }

    public Node getUndoFocusTarget() {
        var group = World.getActiveInstance().getCityModelGroup();
        return group.findView(oldBuilding);
    }

    public Node getRedoFocusTarget() {
        var group = World.getActiveInstance().getCityModelGroup();
        return group.findView(newBuilding);
    }

    private static void replaceBuilding(CityModel cityModel, AbstractBuilding oldBuilding, AbstractBuilding newBuilding) {
        CityObjectMember cityObjectMember = null;
        for (var member : cityModel.getCityObjectMember()) {
            if (member.getCityObject() == oldBuilding)
                cityObjectMember = member;
        }
        if (cityObjectMember == null)
            throw new RuntimeException();

        var group = World.getActiveInstance().getCityModelGroup();
        var cityModelView = (CityModelView) group.findView(cityModel);
        var oldView = (BuildingView) group.findView(oldBuilding);
        cityModelView.removeFeature(group, oldView);

        cityObjectMember.setCityObject(newBuilding);
        var newView = new CityObjectMemberFactory(group, cityModelView).createBuilding(newBuilding);
        cityModelView.addFeature(group, newView);
    }
}
