package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.basicTypes.Code;

/**
 * Description属性の追加・削除などの操作処理の実体を持つクラス
 */
public class DescriptionWrapper extends AbstractAttributeWrapper {
    public DescriptionWrapper() {
        initialize("description");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getMeasuredHeight().getValue());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getMeasuredHeight().setValue(Double.parseDouble(value));
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetDescription();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setDescription(new StringOrRef(value));
    }
}