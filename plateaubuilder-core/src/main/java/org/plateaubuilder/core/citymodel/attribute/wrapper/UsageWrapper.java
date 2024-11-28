package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;
import java.util.List;

import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * Usage属性の追加・削除などの操作処理の実体を持つクラス
 */
public class UsageWrapper extends AbstractAttributeWrapper {
    public UsageWrapper(ModelType modelType) {
        initialize(modelType, "usage");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getUsageMethod = obj.getClass().getMethod("getUsage");
            List<Code> usages = (List<Code>) getUsageMethod.invoke(obj);
            return usages != null && !usages.isEmpty() ? usages.get(0).getValue() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            Method getUsageMethod = obj.getClass().getMethod("getUsage");
            List<Code> usages = (List<Code>) getUsageMethod.invoke(obj);
            if (usages != null && !usages.isEmpty()) {
                usages.get(0).setValue(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getCodeSpace(Object obj) {
        try {
            Method getUsageMethod = obj.getClass().getMethod("getUsage");
            List<Code> usages = (List<Code>) getUsageMethod.invoke(obj);
            return usages != null && !usages.isEmpty() ? usages.get(0).getCodeSpace() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setCodeSpace(Object obj, String value) {
        try {
            Method getUsageMethod = obj.getClass().getMethod("getUsage");
            List<Code> usages = (List<Code>) getUsageMethod.invoke(obj);
            if (usages != null && !usages.isEmpty()) {
                usages.get(0).setCodeSpace(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method unsetUsageMethod = obj.getClass().getMethod("unsetUsage");
            unsetUsageMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        try {
            Method addUsageMethod = obj.getClass().getMethod("addUsage", Code.class);
            addUsageMethod.invoke(obj, new Code(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}