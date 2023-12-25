package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class C04CompletenessValidator implements IValidator {

    private int length;

    static class BuildingInvalid {
        private String ID;
        private List<String> uroBuildingID;

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public List<String> getUroBuildingID() {
            return uroBuildingID;
        }

        public void setUroBuildingID(List<String> uroBuildingID) {
            this.uroBuildingID = uroBuildingID;
        }

        public String toString() {
            return "BuildingID: " + ID + " (uroBuildingID: " + uroBuildingID.toString() + ")";
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModel cityModelView) throws ParserConfigurationException, IOException, SAXException {
        NodeList buildings = CityGmlUtil.getAllTagFromCityModel(cityModelView, TagName.BLDG_BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);
            // get tag <uro:BuildingID> duplicate
            List<Node> uroBuildingIDDuplicate = this.getUroBuildingIDDuplicate(building);
            // get tag <uro:BuildingID> invalid
            List<String> uroBuildingIDInvalids = this.getUroBuildingIDInvalid(uroBuildingIDDuplicate);

            if (CollectionUtil.isEmpty(uroBuildingIDInvalids)) continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setID(buildingID);
            buildingInvalid.setUroBuildingID(uroBuildingIDInvalids);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, String.format("%sは重複して使用されています。\n", "C04: " + buildingInvalids)));
        return messages;
    }

    private List<Node> getUroBuildingIDDuplicate(Element building) {
        List<Node> duplicate = new ArrayList<>();
        NodeList tagUroBuildingIDs = building.getElementsByTagName(TagName.URO_BULDING_ID);

        length = tagUroBuildingIDs.getLength();
        if (length == 0) return List.of();
        List<String> conditions = new ArrayList<>();
        Node uroBuildingIDNode = tagUroBuildingIDs.item(0);
        String uroBuildingID = uroBuildingIDNode.getTextContent().trim();
        conditions.add(uroBuildingID);

        for (int i = 1; i < length; i++) {
            Node node = tagUroBuildingIDs.item(i);
            String id = node.getTextContent().trim();

            if (conditions.contains(id)) {
                duplicate.add(node);
            }
            conditions.add(id);
        }
        return duplicate;
    }

    private List<String> getUroBuildingIDInvalid(List<Node> duplicate) {
        List<String> uroBuildingIDInvalids = new ArrayList<>();
        for (Node uroBuilding : duplicate) {
            Element eUroBuilding = (Element) uroBuilding;
            NodeList uroBrandID = eUroBuilding.getElementsByTagName(TagName.URO_BRANCH_ID);
            NodeList uroPartID = eUroBuilding.getElementsByTagName(TagName.URO_PART_ID);

            if (uroBrandID.getLength() == 0 || uroPartID.getLength() == 0) {
                String uroBuildingIDInvalid = eUroBuilding.getTextContent();
                uroBuildingIDInvalids.add(uroBuildingIDInvalid);
            }
        }
        return uroBuildingIDInvalids;
    }
}
