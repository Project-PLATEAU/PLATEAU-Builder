package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Array;
import java.util.List;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * Height属性の追加・削除などの操作処理の実体を持つクラス
 */
public class HeightWrapper extends AbstractAttributeWrapper {
    public HeightWrapper(ModelType modelType) {
        initialize(modelType, "height");
    }

    @Override
    public String getValue(Object obj) {
        String value = "";
        value = String.valueOf(((SolitaryVegetationObject) obj).getHeight().getValue());
        return value;
    }

    @Override
    public void setValue(Object obj, String value) {
        ((SolitaryVegetationObject) obj).setHeight(new Length(Double.parseDouble(value)));
    }

    @Override
    public void remove(Object obj) {
        ((SolitaryVegetationObject) obj).unsetHeight();
    }

    @Override
    public void add(Object obj, String value) {
        ((SolitaryVegetationObject) obj).setHeight(new Length(Double.parseDouble(value)));
    }
}