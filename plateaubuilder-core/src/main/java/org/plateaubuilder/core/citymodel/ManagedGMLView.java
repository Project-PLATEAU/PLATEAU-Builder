package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.gml.base.AbstractGML;

public abstract class ManagedGMLView<T extends AbstractGML> extends GMLView<T> {
    private boolean filtered = false;

    public ManagedGMLView(T gml) {
        super(gml);
    }

    public <T2 extends AbstractGML> void addFeature(CityModelGroup group, ManagedGMLView<T2> view) {
        getChildren().add(view);
        group.registerView(view);
        group.fireChangeEvent();
    }

    public <T2 extends AbstractGML> void removeFeature(CityModelGroup group, ManagedGMLView<T2> view) {
        getChildren().remove(view);
        group.removeView(view);
        group.fireChangeEvent();
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
}
