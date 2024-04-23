package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;

public class FeatureViewFactoryBuilder {
    private CityModelGroup cityModelGroup;

    private CityModelView cityModelView;

    private AbstractCityObject cityObject;

    public FeatureViewFactoryBuilder cityModelGroup(CityModelGroup cityModelGroup) {
        this.cityModelGroup = cityModelGroup;
        return this;
    }

    public FeatureViewFactoryBuilder cityModelView(CityModelView cityModelView) {
        this.cityModelView = cityModelView;
        return this;
    }

    public FeatureViewFactoryBuilder cityObject(AbstractCityObject cityObject) {
        this.cityObject = cityObject;
        return this;
    }

    public AbstractFeatureViewFactory build() {
        if (this.cityModelGroup == null) {
            throw new IllegalStateException("City model group is not set.");
        }
        if (this.cityModelView == null) {
            throw new IllegalStateException("City model view is not set.");
        }
        if (this.cityObject == null) {
            throw new IllegalStateException("City object is not set.");
        }

        if (this.cityObject.getCityGMLClass() == CityGMLClass.BUILDING) {
            return new BuildingViewFactory(this.cityModelGroup, this.cityModelView);
        } else if (this.cityObject.getCityGMLClass() == CityGMLClass.ROAD) {
            return new RoadViewFactory(this.cityModelGroup, this.cityModelView);
        } else {
            throw new IllegalArgumentException(
                    String.format("Unsupported CityGML class: %s", this.cityObject.getCityGMLClass()));
        }
    }
}
