package org.plateaubuilder.core.editor.filters.expressions;

public class GreaterThanOrEqualExpression extends AbstractNumberAttributeExpression {
    public GreaterThanOrEqualExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(double attributeValue, double doubleValue) {
        return attributeValue >= doubleValue;
    }
}
