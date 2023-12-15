package org.plateau.citygmleditor.validation;

import org.apache.commons.lang3.StringUtils;
import org.citygml4j.model.citygml.core.CityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.plateau.citygmleditor.constant.TagName.srsNameURI;

public class L05CompletenessValidator implements IValidator{
    @Override
    public List<ValidationResultMessage> validate(CityModel cityModel) {
        List<ValidationResultMessage> messages = new ArrayList<>();

        if (Objects.isNull(cityModel)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "The model Null"));
            return messages;
        }

        try {
            String uri = cityModel.getBoundedBy().getEnvelope().getSrsName();
            if (StringUtils.isBlank(uri)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "L05: Url is blank"));
                return messages;
            }

            if (!uri.equals(srsNameURI)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "L05: Url error!" + uri));
            }

            return messages;
        } catch (Exception e) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "L05: gml:srsName field is not exists"));
            return messages;
        }

    }
}
