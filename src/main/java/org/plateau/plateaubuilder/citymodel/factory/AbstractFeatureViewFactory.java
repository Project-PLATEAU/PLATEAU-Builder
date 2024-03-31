package org.plateau.plateaubuilder.citymodel.factory;

import org.plateau.plateaubuilder.citymodel.CityModelGroup;
import org.plateau.plateaubuilder.citymodel.CityModelView;

public abstract class AbstractFeatureViewFactory extends CityGMLFactory {
    private final CityModelGroup group;

    protected AbstractFeatureViewFactory(CityModelGroup group, CityModelView target) {
        super(target);

        this.group = group;
    }

    public CityModelGroup getGroup() {
        return group;
    }
}
