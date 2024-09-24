
package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;

/**
 * Clazz属性の追加・削除などの操作処理の実体を持つクラス
 */
public class ClazzWrapper extends AbstractAttributeWrapper {
    public ClazzWrapper() {
        initialize("class");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getClazz().getValue());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getClazz().setValue(value);
    }

    public String getCodeSpace(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return building.getClazz().getCodeSpace();
    }

    public void setCodeSpace(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getClazz().setCodeSpace(value);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetClazz();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setClazz(new Code(value));
    }
}