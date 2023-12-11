package org.plateau.citygmleditor.validation;

import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GMLIDCompletenessValidator implements IValidator {
    public List<ValidationResultMessage> validate(CityModel cityModel) {
        Set<String> gmlIDs = new HashSet<>();
        List<ValidationResultMessage> messages = new ArrayList<>();

        messages.add(new ValidationResultMessage(
                ValidationResultMessageType.Info,
                "gml:idの完全性を検証中..."
        ));

        for (CityObjectMember cityObjectMember : cityModel.getCityObjectMember()) {
            AbstractCityObject cityObject = cityObjectMember.getCityObject();
            if (cityObject.getCityGMLClass() != CityGMLClass.BUILDING)
                continue;

            var building = (Building)cityObject;

            if (building.getId() == null || building.getId().isEmpty()) {
                messages.add(new ValidationResultMessage(
                        ValidationResultMessageType.Error,
                        String.format("Exists GmlId null\n")));
            }

            var id = building.getId();
            if (gmlIDs.contains(id) && id != "") {
                messages.add(new ValidationResultMessage(
                    ValidationResultMessageType.Error,
                    String.format("%sは重複して使用されています。\n", id)));
            }
            gmlIDs.add(id);
        }

        return messages;
    }
}
