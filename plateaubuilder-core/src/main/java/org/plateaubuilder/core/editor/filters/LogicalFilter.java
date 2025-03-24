package org.plateaubuilder.core.editor.filters;

import java.util.ArrayList;
import java.util.List;

abstract public class LogicalFilter implements IFeatureFilter {
    // 評価の優先順位がなさそうなので、Listで保持することにした
    private final List<IFeatureFilter> filters = new ArrayList<>();

    public LogicalFilter() {
    }

    public LogicalFilter(IFeatureFilter... filters) {
        for (IFeatureFilter filter : filters) {
            addFilters(filter);
        }
    }

    public void addFilters(IFeatureFilter filter) {
        filters.add(filter);
    }

    public List<IFeatureFilter> getFilters() {
        return filters;
    }
}
