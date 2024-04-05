package org.plateaubuilder.core.citymodel.factory;

import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;

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
