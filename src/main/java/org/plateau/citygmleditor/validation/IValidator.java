package org.plateau.citygmleditor.validation;

import org.citygml4j.model.citygml.core.CityModel;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

public interface IValidator {
    List<ValidationResultMessage> validate(CityModel cityModel, String pathGmlFile) throws IOException, SAXException, ParserConfigurationException;
}
