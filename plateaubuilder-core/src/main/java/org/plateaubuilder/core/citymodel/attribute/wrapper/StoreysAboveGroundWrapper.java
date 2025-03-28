package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.DoubleOrNull;
import org.citygml4j.model.gml.basicTypes.MeasureOrNullList;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * StoreysAboveGround属性の追加・削除などの操作処理の実体を持つクラス
 */
public class StoreysAboveGroundWrapper extends AbstractAttributeWrapper {
    public StoreysAboveGroundWrapper(ModelType modelType) {
        initialize(modelType, "storeysAboveGround");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getStoreysAboveGround());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setStoreysAboveGround(Integer.parseInt(value));
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetStoreysAboveGround();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setStoreysAboveGround(Integer.parseInt(value));
    }
}