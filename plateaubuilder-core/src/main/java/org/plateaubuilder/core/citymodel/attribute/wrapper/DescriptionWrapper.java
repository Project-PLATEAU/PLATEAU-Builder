package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;

import org.citygml4j.model.gml.base.StringOrRef;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * Description属性の追加・削除などの操作処理の実体を持つクラス
 */
public class DescriptionWrapper extends AbstractAttributeWrapper {
    public DescriptionWrapper(ModelType modelType) {
        initialize(modelType, "description");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getDescriptionMethod = obj.getClass().getMethod("getDescription");
            StringOrRef description = (StringOrRef) getDescriptionMethod.invoke(obj);
            return description != null ? description.getValue() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            Method setDescriptionMethod = obj.getClass().getMethod("setDescription", StringOrRef.class);
            setDescriptionMethod.invoke(obj, new StringOrRef(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method unsetDescriptionMethod = obj.getClass().getMethod("unsetDescription");
            unsetDescriptionMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        setValue(obj, value);
    }
}