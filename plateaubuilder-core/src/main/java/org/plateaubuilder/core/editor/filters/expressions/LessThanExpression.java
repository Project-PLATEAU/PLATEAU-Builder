package org.plateaubuilder.core.editor.filters.expressions;

public class LessThanExpression extends AbstractNumberAttributeExpression {
    public LessThanExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(double attributeValue, double doubleValue) {
        return attributeValue < doubleValue;
    }
}
