package org.plateau.plateaubuilder.validation;

import org.apache.commons.lang3.StringUtils;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.validation.constant.MessageError;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.plateau.plateaubuilder.validation.constant.TagName.srsNameURI;

public class L05LogicalConsistencyValidator implements IValidator{
    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) {
        List<ValidationResultMessage> messages = new ArrayList<>();

        try {
            String uri = cityModelView.getGML().getBoundedBy().getEnvelope().getSrsName();
            if (StringUtils.isBlank(uri)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "L05: Url is blank"));
                return messages;
            }

            if (!uri.equals(srsNameURI)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_L05_001, uri)));
            }

            return messages;
        } catch (Exception e) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "L05: gml:srsName field is not exists"));
            return messages;
        }

    }
}
