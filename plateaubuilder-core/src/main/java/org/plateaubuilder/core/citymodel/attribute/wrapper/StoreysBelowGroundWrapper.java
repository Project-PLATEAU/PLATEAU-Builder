package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;

/**
 * StoreysBelowGround属性の追加・削除などの操作処理の実体を持つクラス
 */
public class StoreysBelowGroundWrapper extends AbstractAttributeWrapper {
    public StoreysBelowGroundWrapper() {
        initialize("storeysBelowGround");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getStoreysBelowGround());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setStoreysBelowGround(Integer.parseInt(value));
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetStoreyHeightsBelowGround();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        MeasureOrNullList storeyHeights = new MeasureOrNullList();
        storeyHeights.addDoubleOrNull(new DoubleOrNull(Double.parseDouble(value)));
        building.setStoreyHeightsBelowGround(storeyHeights);
    }
}