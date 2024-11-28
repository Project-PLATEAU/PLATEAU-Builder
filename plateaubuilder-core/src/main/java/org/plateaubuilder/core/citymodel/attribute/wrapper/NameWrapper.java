package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;
import java.util.List;

import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * Name属性の追加・削除などの操作処理の実体を持つクラス
 */
public class NameWrapper extends AbstractAttributeWrapper {
    public NameWrapper(ModelType modelType) {
        initialize(modelType, "name");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getNameMethod = obj.getClass().getMethod("getName");
            List<Code> names = (List<Code>) getNameMethod.invoke(obj);
            return names != null && !names.isEmpty() ? names.get(0).getValue() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            Method getNameMethod = obj.getClass().getMethod("getName");
            List<Code> names = (List<Code>) getNameMethod.invoke(obj);
            if (names != null && !names.isEmpty()) {
                names.get(0).setValue(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCodeSpace(Object obj) {
        try {
            Method getNameMethod = obj.getClass().getMethod("getName");
            List<Code> names = (List<Code>) getNameMethod.invoke(obj);
            return names != null && !names.isEmpty() ? names.get(0).getCodeSpace() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setCodeSpace(Object obj, String value) {
        try {
            Method getNameMethod = obj.getClass().getMethod("getName");
            List<Code> names = (List<Code>) getNameMethod.invoke(obj);
            if (names != null && !names.isEmpty()) {
                names.get(0).setCodeSpace(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method unsetNameMethod = obj.getClass().getMethod("unsetName");
            unsetNameMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        try {
            Method addNameMethod = obj.getClass().getMethod("addName", Code.class);
            addNameMethod.invoke(obj, new Code(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}