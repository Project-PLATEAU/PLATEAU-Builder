package org.plateaubuilder.core.editor.filters.expressions;

public class GreaterThanExpression extends AbstractNumberAttributeExpression {
    public GreaterThanExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(double attributeValue, double doubleValue) {
        return attributeValue > doubleValue;
    }
}
