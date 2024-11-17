package org.plateaubuilder.core.editor.filters;

import org.plateaubuilder.core.citymodel.IFeatureView;

public class AndFilter extends LogicalFilter {
    public AndFilter() {
    }

    public AndFilter(IFeatureFilter... filters) {
        super(filters);
    }

    @Override
    public boolean evaluate(IFeatureView feature) {
        var filters = getFilters();
        if (filters.isEmpty()) {
            throw new IllegalStateException("AndFilter must have at least one filter");
        }

        for (IFeatureFilter filter : filters) {
            if (!filter.evaluate(feature)) {
                return false;
            }
        }
        return true;
    }

    public static IFeatureFilter create(IFeatureFilter... filters) {
        return new AndFilter(filters);
    }
}
