package org.plateaubuilder.core.editor.filters.expressions;

public enum Operator {

    NONE("-"), EQUALS("一致する"), GREATER_THAN("次より大きい"), GREATER_THAN_OR_EQUAL("次以上"), LESS_THAN("次より小さい"), LESS_THAN_OR_EQUAL("次以下");

    private final String operator;

    Operator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public static Operator fromString(String operator) {
        for (Operator o : Operator.values()) {
            if (o.getOperator().equalsIgnoreCase(operator)) {
                return o;
            }
        }
        return null;
    }
}
