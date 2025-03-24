package org.plateaubuilder.core.editor.filters.expressions;

import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.basicTypes.Measure;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.IFeatureView;

import net.opengis.gml.StringOrRefType;

abstract public class AbstractAttributeExpression implements Expression {
    private final String attributeName;
    private final String value;

    public AbstractAttributeExpression(String attributeName, String value) {
        if (attributeName == null || value == null) {
            throw new IllegalArgumentException("attributeName and value must not be null");
        }
        this.attributeName = attributeName;
        this.value = value;
    }

    String getAttributeName() {
        return attributeName;
    }

    String getValue() {
        return value;
    }

    private String getAttributeValue(IFeatureView feature) {
        var value = feature.getAttribute(this.attributeName);
        if (value instanceof Code) {
            return ((Code) value).getValue();
        } else if (value instanceof Length) {
            return String.valueOf(((Length) value).getValue());
        } else if (value instanceof Measure) {
            return String.valueOf(((Measure) value).getValue());
        } else if (value instanceof StringOrRefType) {
            return String.valueOf(((StringOrRefType) value).getValue());
        } else {
            return value != null ? value.toString() : null;
        }
    }

    @Override
    public boolean evaluate(IFeatureView feature) {
        try {
            var attributeValue = getAttributeValue(feature);
            return evaluate(attributeValue, value);
        } catch (Exception e) {
            // 指定した属性値が取得できなかったり、数値変換に失敗した場合はすべてfalseとみなす
            return false;
        }
    }

    abstract boolean evaluate(String attributeValue, String value);
}
