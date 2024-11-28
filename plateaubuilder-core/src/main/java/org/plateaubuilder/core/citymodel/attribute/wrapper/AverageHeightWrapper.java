package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;
import java.util.List;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

import java.util.List;
import java.util.ArrayList;

/**
 * AverageHeight属性の追加・削除などの操作処理の実体を持つクラス
 */
public class AverageHeightWrapper extends AbstractAttributeWrapper {
    public AverageHeightWrapper(ModelType modelType) {
        initialize(modelType, "averageHeight");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getFunctionMethod = obj.getClass().getMethod("getAverageHeight");
            Length length = (Length) getFunctionMethod.invoke(obj);
            return length != null ? String.valueOf(length.getValue()) : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            Method setFunctionMethod = obj.getClass().getMethod("setAverageHeight", List.class);
            Length length = new Length(Double.parseDouble(value));
            setFunctionMethod.invoke(obj, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method unsetFunctionMethod = obj.getClass().getMethod("unsetAverageHeight");
            unsetFunctionMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUom(Object obj) {
        try {
            Method getFunctionMethod = obj.getClass().getMethod("getAverageHeight");
            Length length = (Length) getFunctionMethod.invoke(obj);
            return length.getUom() != null ? length.getUom() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setUom(Object obj, String value) {
        try {
            Method getFunctionMethod = obj.getClass().getMethod("getAverageHeight");
            Length length = (Length) getFunctionMethod.invoke(obj);
            length.setUom(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        try {
            Method setFunctionMethod = obj.getClass().getMethod("setAverageHeight", List.class);
            Length length = new Length(Double.parseDouble(value));
            setFunctionMethod.invoke(obj, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}