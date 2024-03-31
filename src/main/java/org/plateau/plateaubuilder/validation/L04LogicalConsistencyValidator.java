package org.plateau.plateaubuilder.validation;

import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.validation.constant.MessageError;
import org.plateau.plateaubuilder.validation.constant.TagName;
import org.plateau.plateaubuilder.utils.CityGmlUtil;
import org.plateau.plateaubuilder.utils.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class L04LogicalConsistencyValidator implements IValidator {
    private CityModelView targetCityModelView;

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        targetCityModelView = cityModelView;

        List<ValidationResultMessage> messages = new ArrayList<>();

        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            List<GmlElementError> elementErrors = new ArrayList<>();
            Node building = buildings.item(i);
            Element buildingE = (Element) building;
            String buildingID = buildingE.getAttribute(TagName.GML_ID);

            List<Node> tagHaveCodeSpaces = new ArrayList<>();
            XmlUtil.recursiveFindNodeByAttribute(building, tagHaveCodeSpaces, TagName.ATTRIBUTE_CODE_SPACE);
            List<String> invalidCodeSpaces = this.getInvalidCodeSpaces(tagHaveCodeSpaces);
            if (invalidCodeSpaces.isEmpty()) continue;
            elementErrors.add(new GmlElementError(
                    buildingID,
                    null,
                    null,
                    invalidCodeSpaces.toString(),
                    null,
                    0
            ));

            StringBuilder errorMessage = new StringBuilder(MessageError.ERR_L04_002_1);
            for (String invalid : invalidCodeSpaces) {
                errorMessage.append(MessageFormat.format(MessageError.ERR_L04_002_2, invalid));
            }

            if (!errorMessage.toString().equals(MessageError.ERR_L04_002_1)) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, errorMessage.toString(), elementErrors));
            }
        }

        return messages;
    }

    private List<String> getInvalidCodeSpaces(List<Node> tagHaveCodeSpaces) throws ParserConfigurationException, IOException, SAXException {
        List<String> result = new ArrayList<>();
        for (Node tag : tagHaveCodeSpaces) {
            Element element = (Element) tag;
            if (!this.checkTagValid(element)) {
                String linkCodeSpace = element.getAttribute(TagName.ATTRIBUTE_CODE_SPACE).trim();
                result.add(linkCodeSpace);
            }
        }

        return result;
    }

    private boolean checkTagValid(Element tagInput) throws ParserConfigurationException, IOException, SAXException {
        String linkCodeSpace = tagInput.getAttribute(TagName.ATTRIBUTE_CODE_SPACE).trim();
        String[] paths = linkCodeSpace.split("/");
        StringBuilder absolutePathCodeSpace = new StringBuilder();

        // create absolute file CodeSpace
        File fileImport = new File(targetCityModelView.getGmlPath()).getParentFile();
        for (String path : paths) {
            if (Objects.equals("..", path)) {
                fileImport = fileImport.getParentFile();
            } else {
                if (absolutePathCodeSpace.length() == 0) {
                    absolutePathCodeSpace = new StringBuilder(fileImport.toString() + "/" + path);
                } else {
                    absolutePathCodeSpace.append("/").append(path);
                }
            }
        }

        String contentInput = tagInput.getTextContent().trim();
        File fileCodeSpace = new File(absolutePathCodeSpace.toString());
        NodeList codeSpaces = XmlUtil.getAllTagFromXmlFile(fileCodeSpace, TagName.GML_NAME);
        for (int i = 0; i < codeSpaces.getLength(); i++) {
            Element codeSpace = (Element) codeSpaces.item(i);
            String codeContenSpace = codeSpace.getTextContent().trim();
            if (Objects.equals(codeContenSpace, contentInput)) return true;
        }
        return false;
    }
}
