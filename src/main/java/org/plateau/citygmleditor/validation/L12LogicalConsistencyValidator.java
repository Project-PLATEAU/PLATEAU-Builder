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

public class L12LogicalConsistencyValidator implements IValidator {
    private final String LOD2OR3 = ".*[lLoOdD][23].*";

    private final String L12 = "L12";

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();
        // get buildings from gml file
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<L11LogicalConsistencyValidator.BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            List<Node> tagLOD23s = XmlUtil.getTagsByRegex(LOD2OR3, tagBuilding);

            L11LogicalConsistencyValidator l11Validate = new L11LogicalConsistencyValidator();
            List<L11LogicalConsistencyValidator.LODInvalid> lodInvalids = l11Validate.getInvalidLOD(tagLOD23s, L12, messages);

            if (CollectionUtil.isEmpty(lodInvalids)) continue;
            L11LogicalConsistencyValidator.BuildingInvalid buildingInvalid = new L11LogicalConsistencyValidator.BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setLodInvalids(lodInvalids);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return new ArrayList<>();
        for (L11LogicalConsistencyValidator.BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    MessageFormat.format(MessageError.ERR_L12_001, invalid)));
        }
        return messages;
    }
}
