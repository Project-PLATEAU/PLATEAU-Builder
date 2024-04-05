package org.plateaubuilder.core.citymodel.factory;

import org.plateaubuilder.core.citymodel.CityModelView;

public abstract class CityGMLFactory {
    private CityModelView target;

    protected CityGMLFactory(CityModelView target) {
        this.target = target;
    }

    public CityModelView getTarget() {
        return target;
    }

    public void setTarget(CityModelView target) {
        this.target = target;
    }
}
