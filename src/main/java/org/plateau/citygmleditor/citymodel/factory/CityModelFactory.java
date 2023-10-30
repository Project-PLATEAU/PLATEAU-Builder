package org.plateau.citygmleditor.citymodel.factory;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.xml.schema.SchemaHandler;
import org.plateau.citygmleditor.citymodel.CityModel;

public class CityModelFactory {
    public CityModel createCityModel(org.citygml4j.model.citygml.core.CityModel gmlObject, String gmlPath, SchemaHandler schemaHandler) {
        var cityModel = new CityModel(gmlObject, schemaHandler);
        cityModel.setGmlPath(gmlPath);

        for (var appearanceMember : gmlObject.getAppearanceMember()) {
            // TODO: rgbTexture以外のAppearanceがある場合の対応

            var appearanceFactory = new AppearanceFactory(cityModel);
            var appearance = appearanceFactory.createAppearance((AppearanceMember) appearanceMember);
            cityModel.setRGBTextureAppearance(appearance);
        }

        for (CityObjectMember cityObjectMember : gmlObject.getCityObjectMember()) {
            AbstractCityObject cityObject = cityObjectMember.getCityObject();
            if (cityObject.getCityGMLClass() == CityGMLClass.BUILDING) {
                var cityObjectMemberFactory = new CityObjectMemberFactory(cityModel);
                var building = cityObjectMemberFactory.createBuilding((AbstractBuilding) cityObject);
                cityModel.addCityObjectMember(building);
            }
        }

        return cityModel;
    }
}
