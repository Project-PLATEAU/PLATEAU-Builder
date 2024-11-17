package org.plateaubuilder.core.editor.filters.expressions;

public class LikeExpression extends AbstractAttributeExpression {
    public LikeExpression(String attributeName, String value) {
        super(attributeName, value);
    }

    @Override
    boolean evaluate(String attributeValue, String value) {
        return attributeValue.contains(value);
    }
}
