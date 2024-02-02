package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
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
    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<String> invalidBuildings = new ArrayList<>();
        List<GmlElementError> elementErrors = new ArrayList<>();
        for (int i = 0; i < buildings.getLength(); i++) {
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
            invalidBuildings.add(String.format("gml:id = (%s) [%s]", buildingID, invalidCodeSpaces));
        }

        List<ValidationResultMessage> messages = new ArrayList<>();
        StringBuilder errorMessage = new StringBuilder(MessageError.ERR_L04_002_1);
        for (String invalid : invalidBuildings) {
            errorMessage.append(MessageFormat.format(MessageError.ERR_L04_002_2, invalid));
        }

        if (!errorMessage.toString().equals(MessageError.ERR_L04_002_1)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, errorMessage.toString(), elementErrors));
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
        File fileImport = new File(World.getActiveInstance().getCityModel().getGmlPath()).getParentFile();
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
