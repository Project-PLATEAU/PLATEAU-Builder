package org.plateaubuilder.core.editor.filters;

import org.plateaubuilder.core.citymodel.IFeatureView;

public class OrFilter extends LogicalFilter {
    public OrFilter() {
    }

    public OrFilter(IFeatureFilter... filters) {
        super(filters);
    }

    @Override
    public boolean evaluate(IFeatureView feature) {
        var filters = getFilters();
        if (filters.isEmpty()) {
            throw new IllegalStateException("OrFilter must have at least one filter");
        }

        for (IFeatureFilter filter : filters) {
            if (filter.evaluate(feature)) {
                return true;
            }
        }
        return false;
    }

    public static IFeatureFilter create(IFeatureFilter... filters) {
        return new OrFilter(filters);
    }
}
