package org.plateaubuilder.validation;

import org.plateaubuilder.core.citymodel.CityModelView;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface IValidator {
    List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException;
}
