package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.base.AbstractGML;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;

public abstract class AbstractFeatureViewFactory<T extends AbstractCityObject, T2 extends AbstractGML>
        extends CityGMLFactory {
    private final CityModelGroup group;

    protected AbstractFeatureViewFactory(CityModelGroup group, CityModelView target) {
        super(target);

        this.group = group;
    }

    public CityModelGroup getGroup() {
        return group;
    }

    abstract public ManagedGMLView<T2> create(T2 gmlObject);
}
