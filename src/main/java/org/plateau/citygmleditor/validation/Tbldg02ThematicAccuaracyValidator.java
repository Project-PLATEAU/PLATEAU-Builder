package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Tbldg02ThematicAccuaracyValidator implements IValidator {
    final String NOT_GML_MULTISURFACE_GML_SOLID = "^(?:(?!\\bgml:MultiSurface\\b|\\bgml:Solid\\b).)*$";
    final String BLDG_LOD2OR3_GEOMETRY = "^bldg:lod[23]Geometry$";

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        NodeList buildingInstallations = CityGmlUtil.getXmlDocumentFrom(cityModelView)
                .getElementsByTagName(TagName.BLDG_BUILDING_INSTALLATION);
        List<String> invalidInstallations = new ArrayList<>();
        List<GmlElementError> elementErrors = new ArrayList<>();

        for (int i = 0; i < buildingInstallations.getLength(); i++) {
            Node installation = buildingInstallations.item(i);
            // Get all tags that are not A and B
            List<Node> lodGeometry = XmlUtil.getTagsByRegex(BLDG_LOD2OR3_GEOMETRY, installation);
            List<String> invalidLodGeometry = this.getLodGemetryInvalids(lodGeometry);
            if (!CollectionUtil.isEmpty(invalidLodGeometry)) {
                Element eInstallation = (Element) installation;
                String invalidInstallation = "gml:id=" + eInstallation.getAttribute(TagName.GML_ID) + " [" + invalidLodGeometry + "]";
                invalidInstallations.add(invalidInstallation);
                elementErrors.add(new GmlElementError(null, null, null, invalidInstallation, TagName.BLDG_BUILDING_INSTALLATION, 0));
            }
        }
        List<ValidationResultMessage> messages = new ArrayList<>();
        StringBuilder errorMessage = new StringBuilder(MessageError.ERR_T_Bldg_02_002_1);
        for (String invalid : invalidInstallations) {
            errorMessage.append(MessageFormat.format(MessageError.ERR_T_Bldg_02_002_2, invalid));
        }

        if (!errorMessage.toString().equals(MessageError.ERR_T_Bldg_02_002_1)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, errorMessage.toString(), elementErrors));
        }
        return messages;
    }

    private List<String> getLodGemetryInvalids(List<Node> tags) {
        List<String> result = new ArrayList<>();
        for (Node geometry : tags) {
            boolean isGeometryValid = this.isTagValid(geometry);
            if (!isGeometryValid) {
                Element element = (Element) geometry;
                String lodGeometry = element.getTagName() + " " + element.getAttribute(TagName.GML_ID);
                result.add(lodGeometry);
            }
        }
        return result;
    }

    private boolean isTagValid(Node geometry) {
        NodeList childrenTag = geometry.getChildNodes();
        for (int i = 0; i < childrenTag.getLength(); i++) {
            Node child = childrenTag.item(i);
            // Check to see if the child tag contains any tags other than gml:MultiSurface and gml:solid
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().matches(NOT_GML_MULTISURFACE_GML_SOLID)) {
                return false;
            }
        }
        return true;
    }
}
