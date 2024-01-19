package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class T03ThematicAccuaracyValidator implements IValidator {
    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        // get buildings from gml file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = CityGmlUtil.getXmlDocumentFrom(cityModelView);

        // get all object have reference
        Set<String> allXHref = this.getAllAttribute(doc, TagName.X_HREF);
        // get all attribute gml:id
        Set<String> allGmlID = this.getAllAttribute(doc, TagName.GML_ID);
        List<String> xHrefInvalid = new ArrayList<>();
        for (String gmlID : allXHref) {
            if (!allGmlID.contains(gmlID)) {
                xHrefInvalid.add(gmlID);
            }
        }
        List<ValidationResultMessage> messages = new ArrayList<>();
        xHrefInvalid.forEach(x -> messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_T03_001, x))));
        return messages;
    }

    /**
     * get all content attribute by name attribute
     *
     * @param: doc file gml
     * @param: attribute name of attribute
     */
    private Set<String> getAllAttribute(Document doc, String attribute) {
        Set<String> allAttribute = new HashSet<>();
        XmlUtil.recursiveFindAttributeContent(doc, allAttribute, attribute);
        return allAttribute;
    }
}
