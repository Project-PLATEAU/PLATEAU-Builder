package org.plateaubuilder.core.citymodel.factory;

import java.nio.file.Paths;

import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.xml.schema.SchemaHandler;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;

public class CityModelViewFactory {
    @SuppressWarnings("unchecked")
    public CityModelView createCityModel(CityModelGroup group, CityModel gmlObject, String gmlPath, SchemaHandler schemaHandler) {
        var cityModelView = new CityModelView(gmlObject, schemaHandler);
        cityModelView.setGmlPath(gmlPath);
        cityModelView.setId(Paths.get(gmlPath).getFileName().toString());

        for (var appearanceMember : gmlObject.getAppearanceMember()) {
            var appearanceFactory = new AppearanceFactory(cityModelView);
            var appearance = appearanceFactory.createAppearance((AppearanceMember) appearanceMember);
            cityModelView.setAppearance(appearance);
        }

        for (CityObjectMember cityObjectMember : gmlObject.getCityObjectMember()) {
            var cityObject = cityObjectMember.getCityObject();
            if (cityObject != null) {
                var featureViewFactory = new FeatureViewFactoryBuilder().cityModelGroup(group).cityModelView(cityModelView).cityObject(cityObject).build();
                var featureView = featureViewFactory.create(cityObject);
                cityModelView.addFeature(group, featureView);
            } else if (cityObjectMember.isSetGenericADEElement()) {
                var genericCityObjectViewFactory = new ADEGenericComponentViewFactory(group, cityModelView);
                var featureView = genericCityObjectViewFactory.create(new ADEGenericComponent(cityObjectMember.getGenericADEElement()));
                cityModelView.addFeature(group, featureView);
            }
        }

        return cityModelView;
    }
}
