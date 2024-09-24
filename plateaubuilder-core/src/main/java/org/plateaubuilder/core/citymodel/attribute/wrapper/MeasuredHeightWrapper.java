package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.base.StringOrRef;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;

import javafx.scene.control.TreeItem;

/**
 * MeasuredHeight属性の追加・削除などの操作処理の実体を持つクラス
 */
public class MeasuredHeightWrapper extends AbstractAttributeWrapper {
    public MeasuredHeightWrapper() {
        initialize("measuredHeight");
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

    public String getUom(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return building.getMeasuredHeight().getUom();
    }

    public void setUom(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getMeasuredHeight().setUom(value);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetMeasuredHeight();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setMeasuredHeight(new Length(Double.parseDouble((value))));
    }
}
