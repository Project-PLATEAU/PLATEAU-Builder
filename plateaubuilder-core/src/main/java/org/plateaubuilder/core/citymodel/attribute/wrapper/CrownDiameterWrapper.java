package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * CrownDiameterWrapper属性の追加・削除などの操作処理の実体を持つクラス
 */
public class CrownDiameterWrapper extends AbstractAttributeWrapper {
    public CrownDiameterWrapper(ModelType modelType) {
        initialize(modelType, "crownDiameter");
    }

    @Override
    public String getValue(Object obj) {
        String value = "";
        value = String.valueOf(((SolitaryVegetationObject) obj).getCrownDiameter().getValue());
        return value;
    }

    @Override
    public void setValue(Object obj, String value) {
        ((SolitaryVegetationObject) obj).setCrownDiameter(new Length(Double.parseDouble(value)));
    }

    @Override
    public void remove(Object obj) {
        ((SolitaryVegetationObject) obj).unsetCrownDiameter();
    }

    @Override
    public void add(Object obj, String value) {
        ((SolitaryVegetationObject) obj).setCrownDiameter(new Length(Double.parseDouble(value)));
    }
}