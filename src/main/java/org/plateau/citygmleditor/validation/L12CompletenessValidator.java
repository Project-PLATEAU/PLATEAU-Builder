package org.plateau.citygmleditor.validation;

import org.citygml4j.model.citygml.core.CityModel;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class L12CompletenessValidator implements IValidator {
    private final String LOD1 = ".*[lLoOdD]1.*";

    private final String L12 = "L12";

    @Override
    public List<ValidationResultMessage> validate(CityModel cityModel) throws ParserConfigurationException, IOException, SAXException {
        File gmlFile = new File(World.getActiveInstance().getCityModel().getGmlPath());
        // get buildings from gml file
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(gmlFile, TagName.BUILDING);
        List<L11CompletenessValidator.BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            List<Node> tagLOD1s = XmlUtil.getTagsByRegex(LOD1, tagBuilding);

            L11CompletenessValidator l11Validate = new L11CompletenessValidator();
            List<L11CompletenessValidator.LOD1Invalid> lod1Invalids = l11Validate.getInvalidLOD1(tagLOD1s, L12);

            if (CollectionUtil.collectionEmpty(lod1Invalids)) continue;
            L11CompletenessValidator.BuildingInvalid buildingInvalid = new L11CompletenessValidator.BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setLod1Invalids(lod1Invalids);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.collectionEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, String.format("L12: %sは重複して使用されています。\n", buildingInvalids)));
        return messages;
    }
}
