package org.plateaubuilder.core.editor.filters.expressions;

public class LessThanOrEqualExpression extends AbstractNumberAttributeExpression {
    public LessThanOrEqualExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(double attributeValue, double doubleValue) {
        return attributeValue <= doubleValue;
    }
}
