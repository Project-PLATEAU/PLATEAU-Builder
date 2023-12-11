package org.plateau.citygmleditor.validation;

import org.apache.commons.lang3.StringUtils;
import org.citygml4j.model.citygml.core.CityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class L05CompletenessValidator implements IValidator{
    @Override
    public List<ValidationResultMessage> validate(CityModel cityModel) {
        List<ValidationResultMessage> messages = new ArrayList<>();

        if (Objects.isNull(cityModel)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    "The model Null"));
            return messages;
        }

        try {
            String uri = cityModel.getBoundedBy().getEnvelope().getSrsName();
            if (StringUtils.isBlank(uri)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "Url is blank"));
                return messages;
            }

            if (uri.contains("6697") || uri.contains("6668")) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                        "The spatial reference system specified by srsName is neither 6697 nor 6668"));
                return messages;
            }

            /*
            - Regex check standard Uri
            - Start by "http://", "https://" or "ftp://"
            - No 1 character is blank, "/", "$", ".", "?", or "#"
             */
            Pattern p = Pattern.compile("^(https?|ftp):\\/\\/[^\\s\\/$.?#].[^\\s]*$");
            Matcher m = p.matcher(uri);

            if (!m.matches()) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "Url error!"));
            }

            return messages;
        } catch (Exception e) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "gml:srsName field is not exists"));
            return messages;
        }

    }
}
