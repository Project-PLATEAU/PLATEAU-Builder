package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;
import java.util.List;

import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

import java.util.List;
import java.util.ArrayList;

/**
 * Function属性の追加・削除などの操作処理の実体を持つクラス
 */
public class FunctionWrapper extends AbstractAttributeWrapper {
    public FunctionWrapper(ModelType modelType) {
        initialize(modelType, "function");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getFunctionMethod = obj.getClass().getMethod("getFunction");
            List<Code> functions = (List<Code>) getFunctionMethod.invoke(obj);
            return functions != null && !functions.isEmpty() ? functions.get(0).getValue() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            Method setFunctionMethod = obj.getClass().getMethod("setFunction", List.class);
            List<Code> functions = new ArrayList<>();
            functions.add(new Code(value));
            setFunctionMethod.invoke(obj, functions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCodeSpace(Object obj) {
        try {
            Method getFunctionMethod = obj.getClass().getMethod("getFunction");
            List<Code> functions = (List<Code>) getFunctionMethod.invoke(obj);
            return functions != null && !functions.isEmpty() ? functions.get(0).getCodeSpace() : "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setCodeSpace(Object obj, String value) {
        try {
            Method getFunctionMethod = obj.getClass().getMethod("getFunction");
            List<Code> functions = (List<Code>) getFunctionMethod.invoke(obj);
            if (functions != null && !functions.isEmpty()) {
                functions.get(0).setCodeSpace(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method unsetFunctionMethod = obj.getClass().getMethod("unsetFunction");
            unsetFunctionMethod.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        try {
            Method setFunctionMethod = obj.getClass().getMethod("setFunction", List.class);
            List<Code> functions = new ArrayList<>();
            functions.add(new Code(value));
            setFunctionMethod.invoke(obj, functions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}