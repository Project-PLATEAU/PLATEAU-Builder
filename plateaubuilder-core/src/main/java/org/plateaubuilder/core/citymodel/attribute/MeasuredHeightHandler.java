package org.plateaubuilder.core.citymodel.attribute;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.measures.Length;

public class MeasuredHeightHandler extends AttributeHandler {
    private AbstractBuilding building;
    private String type = MeasuredHeightManager.getType();
    private String name = MeasuredHeightManager.getName();

    public MeasuredHeightHandler(AbstractBuilding building) {
        this.building = building;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return MeasuredHeightManager.getMeasuredHeightValue(building);
    }

    @Override
    public void setValue(String value) {
        MeasuredHeightManager.setMeasuredHeightValue(building, value);
    }

    @Override
    public String getUom() {
        return MeasuredHeightManager.getMeasuredHeightUom(building);
    }

    @Override
    public void setUom(String uom) {
        MeasuredHeightManager.setMeasuredHeightUom(building, uom);
    }

    @Override
    public String getCodeSpace() {
        return null;
    }

    @Override
    public void setCodeSpace(String codeSpace) {
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Length getContent() {
        return building.getMeasuredHeight();
    }
}