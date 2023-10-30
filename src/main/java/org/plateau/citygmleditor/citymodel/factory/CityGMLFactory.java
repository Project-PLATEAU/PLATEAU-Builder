package org.plateau.citygmleditor.citymodel.factory;

import org.plateau.citygmleditor.citymodel.CityModel;

public abstract class CityGMLFactory {
    private CityModel target;

    protected CityGMLFactory(CityModel target) {
        this.target = target;
    }

    public CityModel getTarget() {
        return target;
    }

    public void setTarget(CityModel target) {
        this.target = target;
    }
}
