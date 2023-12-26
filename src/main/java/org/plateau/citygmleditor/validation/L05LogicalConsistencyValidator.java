package org.plateau.citygmleditor.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import org.plateau.citygmleditor.citymodel.CityModelView;

import static org.plateau.citygmleditor.constant.TagName.srsNameURI;

public class L05LogicalConsistencyValidator implements IValidator{
    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) {
        List<ValidationResultMessage> messages = new ArrayList<>();

        try {
            String uri = cityModelView.getGmlObject().getBoundedBy().getEnvelope().getSrsName();
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
