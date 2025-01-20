package org.plateaubuilder.core.editor.filters.expressions;

import java.util.ArrayList;
import java.util.List;

import org.plateaubuilder.core.editor.filters.FeatureFilterBuilder;

public class FeatureFilterSetting {
    private boolean isAndLogic;

    private List<FeatureFilterBuilder> bulders = new ArrayList<>();

    public FeatureFilterSetting() {
    }

    public FeatureFilterSetting(boolean isAndLogic) {
        this.isAndLogic = isAndLogic;
    }

    public void addFilter(FeatureFilterBuilder builder) {
        bulders.add(builder);
    }

    // for serialization
    public boolean isAndLogic() {
        return isAndLogic;
    }

    public void setAndLogic(boolean isAndLogic) {
        this.isAndLogic = isAndLogic;
    }

    public List<FeatureFilterBuilder> getBulders() {
        return bulders;
    }

    public void setBulders(List<FeatureFilterBuilder> bulders) {
        this.bulders = bulders;
    }
}
