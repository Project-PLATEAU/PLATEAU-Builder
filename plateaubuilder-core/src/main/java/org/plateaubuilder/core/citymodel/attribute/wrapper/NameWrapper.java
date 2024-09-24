package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;

/**
 * Name属性の追加・削除などの操作処理の実体を持つクラス
 */
public class NameWrapper extends AbstractAttributeWrapper {
    public NameWrapper() {
        initialize("name");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getName().get(0).getValue());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getName().get(0).setValue(value);
    }

    public String getCodeSpace(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return building.getName().get(0).getCodeSpace();
    }

    public void setCodeSpace(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getName().get(0).setCodeSpace(value);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetName();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setClazz(new Code(value));
    }
}