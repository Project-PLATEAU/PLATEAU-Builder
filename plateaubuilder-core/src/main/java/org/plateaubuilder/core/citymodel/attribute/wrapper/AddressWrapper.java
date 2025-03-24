package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.xal.AddressDetails;
import org.citygml4j.model.xal.CountryName;
import org.citygml4j.model.xal.LocalityName;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * Address属性の追加・削除などの操作処理の実体を持つクラス
 */
public class AddressWrapper extends AbstractAttributeWrapper {
    public AddressWrapper(ModelType modelType) {
        initialize(modelType, "address");
    }

    @Override
    public String getValue(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
    }

    @Override
    public void remove(Object obj) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.unsetAddress();
    }

    @Override
    public void add(Object obj, String value) {
        AbstractBuilding building = (AbstractBuilding) obj;
        building.addAddress(new AddressProperty());
    }
}