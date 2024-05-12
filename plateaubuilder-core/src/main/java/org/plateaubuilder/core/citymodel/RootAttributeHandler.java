package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.building.AbstractBuilding;

public class RootAttributeHandler extends AttributeHandler {
    private AbstractBuilding building;
    private String name = "root";

    public RootAttributeHandler(AbstractBuilding building) {
        this.building = building;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public void setValue(String value) {
    }

    @Override
    public String getUom() {
        return "";
    }

    @Override
    public void setUom(String uom) {
    }

    @Override
    public String getCodeSpace() {
        return "";
    }

    @Override
    public void setCodeSpace(String codeSpace) {
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public AbstractBuilding getContent() {
        return building;
    }
}