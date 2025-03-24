
package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;

import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * Clazz属性の追加・削除などの操作処理の実体を持つクラス
 */

public class ClazzWrapper extends AbstractAttributeWrapper {
    public ClazzWrapper(ModelType modelType) {
        initialize(modelType, "class");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getClazzMethod = obj.getClass().getMethod("getClazz");
            Code clazz = (Code) getClazzMethod.invoke(obj);
            return clazz != null ? clazz.getValue() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            Method getClazzMethod = obj.getClass().getMethod("getClazz");
            Code clazz = (Code) getClazzMethod.invoke(obj);
            if (clazz != null) {
                clazz.setValue(value);
            } else {
                Method setClazzMethod = obj.getClass().getMethod("setClazz", Code.class);
                setClazzMethod.invoke(obj, new Code(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCodeSpace(Object obj) {
        try {
            Method getClazzMethod = obj.getClass().getMethod("getClazz");
            Code clazz = (Code) getClazzMethod.invoke(obj);
            return clazz != null ? clazz.getCodeSpace() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setCodeSpace(Object obj, String value) {
        try {
            Method getClazzMethod = obj.getClass().getMethod("getClazz");
            Code clazz = (Code) getClazzMethod.invoke(obj);
            if (clazz != null) {
                clazz.setCodeSpace(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method unsetClazzMethod = obj.getClass().getMethod("unsetClazz");
            unsetClazzMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        setValue(obj, value);
    }
}