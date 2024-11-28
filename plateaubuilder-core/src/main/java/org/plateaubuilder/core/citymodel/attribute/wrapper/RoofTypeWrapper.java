package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * RoofType属性の追加・削除などの操作処理の実体を持つクラス
 */
public class RoofTypeWrapper extends AbstractAttributeWrapper {
    public RoofTypeWrapper(ModelType modelType) {
        initialize(modelType, "roofType");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return String.valueOf(building.getRoofType().getValue());
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getRoofType().setValue(value);
    }

    public String getCodeSpace(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return building.getRoofType().getCodeSpace();
    }

    public void setCodeSpace(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.getRoofType().setCodeSpace(value);
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetRoofType();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.setRoofType(new Code(value));
    }
}