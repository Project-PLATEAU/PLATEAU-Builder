package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface IValidator {
    List<ValidationResultMessage> validate(CityModel cityModel) throws ParserConfigurationException, IOException, SAXException;
}
