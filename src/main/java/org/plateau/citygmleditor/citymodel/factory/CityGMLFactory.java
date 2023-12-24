package org.plateau.citygmleditor.citymodel.factory;

import org.plateau.citygmleditor.citymodel.CityModelView;

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
