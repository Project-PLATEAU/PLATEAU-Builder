package org.plateaubuilder.core.editor.filters.expressions;

abstract public class AbstractNumberAttributeExpression extends AbstractAttributeExpression {
    protected final double doubleValue;

    public AbstractNumberAttributeExpression(String attributeName, String value) {
        super(attributeName, value);
        doubleValue = Double.parseDouble(value);
    }

    @Override
    boolean evaluate(String attributeValue, String value) {
        try {
            var doubleAttributeValue = Double.parseDouble(attributeValue);
            return evaluate(doubleAttributeValue, doubleValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    abstract boolean evaluate(double attributeValue, double doubleValue);
}
