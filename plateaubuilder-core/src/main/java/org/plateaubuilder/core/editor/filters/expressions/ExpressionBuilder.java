package org.plateaubuilder.core.editor.filters.expressions;

public class ExpressionBuilder {
    private String attributeName;
    private Operator operator = Operator.NONE;
    private String value;

    public ExpressionBuilder attributeName(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    public ExpressionBuilder operator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public ExpressionBuilder value(String value) {
        this.value = value;
        return this;
    }

    public Expression build() {
        switch (operator) {
        case NONE:
            return null;
        case EQUALS:
            return new EqualsExpression(attributeName, value);
        case GREATER_THAN:
            return new GreaterThanExpression(attributeName, value);
        case GREATER_THAN_OR_EQUAL:
            return new GreaterThanOrEqualExpression(attributeName, value);
        case LESS_THAN:
            return new LessThanExpression(attributeName, value);
        case LESS_THAN_OR_EQUAL:
            return new LessThanOrEqualExpression(attributeName, value);
        default:
            throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}
