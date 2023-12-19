package org.plateau.citygmleditor.validation;

import org.citygml4j.model.citygml.core.CityModel;

import java.util.List;

public interface IValidator {
    List<ValidationResultMessage> validate(CityModel cityModel);
}
