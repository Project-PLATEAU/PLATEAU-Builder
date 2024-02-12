package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class C04CompletenessValidator implements IValidator {

    private int length;

    static class BuildingInvalid {
        private String buildingID;
        private List<String> uroBuildingIDs;

        public void setBuildingID(String buildingID) {
            this.buildingID = buildingID;
        }

        public void setUroBuildingID(List<String> uroBuildingID) {
            this.uroBuildingIDs = uroBuildingID;
        }

        public String toString() {
            String uro = uroBuildingIDs.stream().map(u -> "[" + u + "]").collect(Collectors.joining("\n"));
            return String.format(MessageError.ERR_C04_BLDG_1_001, buildingID, uro);
        }
    }

    static class UROBuildingAtrribute {
        String uroBuildingID;
        String brandID;
        String partID;

        public String getUroBuildingID() {
            return uroBuildingID;
        }

        public void setUroBuildingID(String uroBuildingID) {
            this.uroBuildingID = uroBuildingID;
        }

        public String getBrandID() {
            return brandID;
        }

        public void setBrandID(String brandID) {
            this.brandID = brandID;
        }

        public String getPartID() {
            return partID;
        }

        public void setPartID(String partID) {
            this.partID = partID;
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        List<ValidationResultMessage> messages = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            List<GmlElementError> elementErrors = new ArrayList<>();
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);
            // get tag <uro:BuildingID> duplicate
            List<UROBuildingAtrribute> uroBuildingIDDuplicate = this.getUroBuildingIDDuplicate(building);
            // get tag <uro:BuildingID> invalid
            List<String> uroBuildingIDInvalids = this.getUroBuildingIDInvalid(uroBuildingIDDuplicate);
            if (CollectionUtil.isEmpty(uroBuildingIDInvalids)) continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setUroBuildingID(uroBuildingIDInvalids);
            buildingInvalids.add(buildingInvalid);
            elementErrors.add(new GmlElementError(
                    buildingID,
                    null,
                    null,
                    uroBuildingIDInvalids.toString(),
                    TagName.URO_BUILDING_ID_ATTRIBUTE, 0));
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingInvalid.toString(), elementErrors));
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();

        return messages;
    }

    private List<UROBuildingAtrribute> getUroBuildingIDDuplicate(Element building) {
        NodeList uroBuildingAttr = building.getElementsByTagName(TagName.URO_BUILDING_ID_ATTRIBUTE);
        length = uroBuildingAttr.getLength();
        if (length == 0) return new ArrayList<>();

        List<UROBuildingAtrribute> duplicateID = new ArrayList<>();
        List<UROBuildingAtrribute> uroByNodelist = this.createUROBuildingAtrribute(uroBuildingAttr);

        // get all uroBuilding duplicate
        outerLoop:
        for (int i = 0; i < uroByNodelist.size(); i++) {
            UROBuildingAtrribute uroBuilding = uroByNodelist.get(i);
            String uroID1 = uroBuilding.getUroBuildingID();
            for (int j = 0; j < uroByNodelist.size(); j++) {
                // ignores the case compared to itself
                if (j == i) continue;

                String uroID2 = uroByNodelist.get(j).getUroBuildingID();
                if (Objects.equals(uroID1, uroID2)) {
                    duplicateID.add(uroBuilding);
                    continue outerLoop;
                }
            }
        }

        return duplicateID;
    }

    private List<UROBuildingAtrribute> createUROBuildingAtrribute(NodeList uroBuildingAttr) {
        List<UROBuildingAtrribute> results = new ArrayList<>();
        for (int i = 0; i < uroBuildingAttr.getLength(); i++) {
            Element uro = (Element) uroBuildingAttr.item(i);
            NodeList uroID = uro.getElementsByTagName(TagName.URO_BULDING_ID);
            if (uroID.getLength() == 0) continue;
            NodeList brandID = uro.getElementsByTagName(TagName.URO_BRANCH_ID);
            NodeList partID = uro.getElementsByTagName(TagName.URO_PART_ID);

            UROBuildingAtrribute uroBuildingAtrribute = new UROBuildingAtrribute();
            uroBuildingAtrribute.setUroBuildingID(uroID.item(0).getTextContent().trim());
            if (brandID.getLength() == 0) {
                uroBuildingAtrribute.setBrandID("");
            } else {
                uroBuildingAtrribute.setBrandID(brandID.item(0).getTextContent().trim());
            }
            if (partID.getLength() == 0) {
                uroBuildingAtrribute.setPartID("");
            } else {
                uroBuildingAtrribute.setPartID(partID.item(0).getTextContent().trim());
            }

            results.add(uroBuildingAtrribute);
        }
        return results;
    }

    private List<String> getUroBuildingIDInvalid(List<UROBuildingAtrribute> duplicate) {
        List<String> uroBuildingIDInvalids = new ArrayList<>();
        int count = 0;
        for (UROBuildingAtrribute uroBuilding : duplicate) {
            String uroBrandID = uroBuilding.getBrandID();
            String uroPartID = uroBuilding.getPartID();

            // only when both uroPartID and uroBrandID are missing should it be considered an error
            if (uroPartID.isEmpty() && uroBrandID.isEmpty()) {
                count++;
                if (count > 1) {
                    String uroBuildingIDInvalid = uroBuilding.getUroBuildingID();
                    uroBuildingIDInvalids.add(uroBuildingIDInvalid);
                }
            }
        }
        return uroBuildingIDInvalids.stream().distinct().collect(Collectors.toList());
    }
}
