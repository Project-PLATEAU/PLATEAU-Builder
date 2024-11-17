package org.plateaubuilder.core.editor.filters.expressions;

public class NotEqualsExpression extends AbstractAttributeExpression {

    public NotEqualsExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(String attributeValue, String value) {
        return !attributeValue.equals(value);
    }

}
