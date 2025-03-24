package org.plateaubuilder.core.editor.filters;

import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.editor.filters.expressions.Expression;

public class FeatureFilter implements IFeatureFilter {
    private final FeatureTypeFilter gmlTypeFilter;
    private final Expression expression;

    public FeatureFilter(FeatureTypeFilter gmlTypeFilter, Expression expression) {
        if (gmlTypeFilter == null) {
            throw new IllegalArgumentException("GML type filter is not set.");
        }
        this.gmlTypeFilter = gmlTypeFilter;
        this.expression = expression;
    }

    public FeatureTypeFilter getGmlTypeFilter() {
        return gmlTypeFilter;
    }

    public Expression getExpression() {
        return expression;
    }

    @Override
    public boolean evaluate(IFeatureView feature) {
        return gmlTypeFilter.evaluate(feature) && (expression != null ? expression.evaluate(feature) : true);
    }
}
