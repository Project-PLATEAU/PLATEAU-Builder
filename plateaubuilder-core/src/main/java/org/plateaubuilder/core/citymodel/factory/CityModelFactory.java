package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.xml.schema.SchemaHandler;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;

import java.nio.file.Paths;

public class CityModelFactory {
    public CityModelView createCityModel(CityModelGroup group, CityModel gmlObject, String gmlPath, SchemaHandler schemaHandler) {
        var cityModel = new CityModelView(gmlObject, schemaHandler);
        cityModel.setGmlPath(gmlPath);
        cityModel.setId(Paths.get(gmlPath).getFileName().toString());

        for (var appearanceMember : gmlObject.getAppearanceMember()) {
            // TODO: rgbTexture以外のAppearanceがある場合の対応

            var appearanceFactory = new AppearanceFactory(cityModel);
            var appearance = appearanceFactory.createAppearance((AppearanceMember) appearanceMember);
            cityModel.setRGBTextureAppearance(appearance);
        }

        for (CityObjectMember cityObjectMember : gmlObject.getCityObjectMember()) {
            AbstractCityObject cityObject = cityObjectMember.getCityObject();
            if (cityObject.getCityGMLClass() == CityGMLClass.BUILDING) {
                var cityObjectMemberFactory = new CityObjectMemberFactory(group, cityModel);
                var building = cityObjectMemberFactory.createBuilding((AbstractBuilding) cityObject);
                cityModel.addFeature(group, building);
            }
        }

        return cityModel;
    }
}
