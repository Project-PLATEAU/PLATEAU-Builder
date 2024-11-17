package org.plateaubuilder.core.editor.filters;

import org.plateaubuilder.core.citymodel.IFeatureView;

public class FeatureTypeFilter implements IFeatureTypeFilter {
    private final String type;

    public FeatureTypeFilter(String type) {
        this.type = type;
    }

    public String getFeatureType() {
        return type;
    }

    @Override
    public boolean evaluate(IFeatureView feature) {
        return feature.getFeatureType().equals(this.type);
    }
}
