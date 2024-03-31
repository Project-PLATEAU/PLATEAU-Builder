package org.plateau.plateaubuilder.validation;

import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.validation.constant.MessageError;
import org.plateau.plateaubuilder.validation.constant.TagName;
import org.plateau.plateaubuilder.utils.CityGmlUtil;
import org.plateau.plateaubuilder.utils.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

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

        if (xHrefInvalid.isEmpty()) return Collections.EMPTY_LIST;
        String hrefInvalidStr = xHrefInvalid.stream().map(x -> "[" + x + "]").collect(Collectors.joining("\n"));
        List<ValidationResultMessage> messages = new ArrayList<>();
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_T03_002_1, hrefInvalidStr)));
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
