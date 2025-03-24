package org.plateaubuilder.core.citymodel.attribute.wrapper;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

/**
 * CreationDate属性の追加・削除などの操作処理の実体を持つクラス
 */
public class CreationDateWrapper extends AbstractAttributeWrapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public CreationDateWrapper(ModelType modelType) {
        initialize(modelType, "creationDate");
    }

    @Override
    public String getValue(Object obj) {
        try {
            Method getMethod = obj.getClass().getMethod("getCreationDate");
            Object date = getMethod.invoke(obj);

            if (date instanceof LocalDate) {
                return ((LocalDate) date).format(FORMATTER);
            } else if (date instanceof ZonedDateTime) {
                return ((ZonedDateTime) date).format(FORMATTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void setValue(Object obj, String value) {
        try {
            LocalDate localDate = LocalDate.parse(value, FORMATTER);
            Method method = obj.getClass().getMethod("setCreationDate", LocalDate.class);
            method.invoke(obj, localDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(Object obj) {
        try {
            Method method = obj.getClass().getMethod("unsetCreationDate");
            method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Object obj, String value) {
        setValue(obj, value);
    }
}
