package org.plateaubuilder.core.editor.filters;

import org.plateaubuilder.core.editor.filters.expressions.ExpressionBuilder;
import org.plateaubuilder.core.editor.filters.expressions.Operator;

public class FeatureFilterBuilder {
    private String featureType;
    private ExpressionBuilder expressionBuilder = new ExpressionBuilder();

    public FeatureFilterBuilder() {
    }

    public FeatureFilterBuilder featureType(String featureType) {
        this.featureType = featureType;
        return this;
    }

    public FeatureFilterBuilder attributeName(String attributeName) {
        this.expressionBuilder.attributeName(attributeName);
        return this;
    }

    public FeatureFilterBuilder operator(Operator operator) {
        this.expressionBuilder.operator(operator);
        return this;
    }

    public FeatureFilterBuilder value(String value) {
        this.expressionBuilder.value(value);
        return this;
    }

    public IFeatureFilter build() {
        return new FeatureFilter(new FeatureTypeFilter(featureType), expressionBuilder.build());
    }

    // for serialization
    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public ExpressionBuilder getExpressionBuilder() {
        return expressionBuilder;
    }

    public void setExpressionBuilder(ExpressionBuilder expressionBuilder) {
        this.expressionBuilder = expressionBuilder;
    }
}
