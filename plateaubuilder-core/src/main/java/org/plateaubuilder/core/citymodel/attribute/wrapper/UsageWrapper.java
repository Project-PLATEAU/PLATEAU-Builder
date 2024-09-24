package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;

/**
 * Usage属性の追加・削除などの操作処理の実体を持つクラス
 */
public class UsageWrapper extends AbstractAttributeWrapper {
    public UsageWrapper() {
        initialize("usage");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getUsage().get(0).getValue());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getUsage().get(0).setValue(value);
    }

    @Override
    public String getCodeSpace(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return building.getUsage().get(0).getCodeSpace();
    }

    @Override
    public void setCodeSpace(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getUsage().get(0).setCodeSpace(value);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetUsage();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.addUsage(new Code(value));
    }
}