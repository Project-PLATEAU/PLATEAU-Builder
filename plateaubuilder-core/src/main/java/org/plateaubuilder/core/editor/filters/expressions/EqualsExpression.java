package org.plateaubuilder.core.editor.filters.expressions;

public class EqualsExpression extends AbstractAttributeExpression {
    public EqualsExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(String attributeValue, String value) {
        return attributeValue.equals(value);
    }
}
