package org.plateaubuilder.core.editor.filters.expressions;

public class NotLikeExpression extends AbstractAttributeExpression {

    public NotLikeExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(String attributeValue, String value) {
        return !attributeValue.contains(value);
    }

}
