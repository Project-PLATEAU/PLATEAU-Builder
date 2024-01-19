package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.Relationship;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.PythonUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Lbldg02LogicalConsistencyValidator implements IValidator {
    static class BuildingInvalid {
        private String ID;
        private List<BuildingPart> buildingPart;

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setBuidlingPart(List<BuildingPart> buidlingPart) {
            this.buildingPart = buidlingPart;
        }

        public String toString() {
            return "bldg:Building gml:id=" + this.ID + "bldg:BuildingPart =" + this.buildingPart;
        }
    }

    static class BuildingPart {
        private String ID;

        private List<String> solid;

        public void setID(String ID) {
            this.ID = ID;
        }

        public List<String> getSolid() {
            return solid;
        }

        public void setSolid(List<String> solid) {
            this.solid = solid;
        }

        @Override
        public String toString() {
            return "bldg:BuildingPart = " + this.ID + " solid = " + solid;
        }
    }

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getAllTagFromCityModel(cityModelView, TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid building part tags
            NodeList tagBuildingParts = building.getElementsByTagName(TagName.BLGD_BUILDING_PART);
            List<BuildingPart> buildingPartInvalid = this.getBuildingPartInvalid(tagBuildingParts);
            if (buildingPartInvalid.isEmpty()) continue;

            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setID(buildingID);
            buildingInvalid.setBuidlingPart(buildingPartInvalid);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_LBLDG_02_001, invalid)));
        }
        return messages;
    }

    private List<BuildingPart> getBuildingPartInvalid(NodeList buildingParts) throws IOException {
        List<BuildingPart> result = new ArrayList<>();

        for (int i = 0; i < buildingParts.getLength(); i++) {
            Element buildingPart = (Element) buildingParts.item(i);
            NodeList solids = buildingPart.getElementsByTagName(TagName.GML_SOLID);
            // building part have only one solid always valid
            if (solids.getLength() <= 1) return List.of();

            BuildingPart invalidBP = new BuildingPart();
            invalidBP.setID(buildingPart.getAttribute(TagName.GML_ID));
            invalidBP.setSolid(this.getInvalidSolid(solids));
            result.add(invalidBP);
        }
        return result;
    }

    private List<String> getInvalidSolid(NodeList solids) throws IOException {
        List<String> result = new ArrayList<>();

        outterLoop:
        for (int i = 0; i < solids.getLength(); i++) {
            Element solid1 = (Element) solids.item(i);
            for (int j = i + 1; j < solids.getLength(); j++) {
                Element solid2 = (Element) solids.item(j);
                if (this.touch(solid1, solid2)) continue outterLoop;
                result.add(solid1.getAttribute(TagName.GML_ID));
            }
        }
        return result;
    }

    /**
     * Check 2 solid touch
     *
     * @param solid1 solid1 input
     * @param solid2 solid2 input
     * @return true if polygon touch 1 polygon of solid and not intersect the others polygon of solid
     */
    private boolean touch(Element solid1, Element solid2) throws IOException {
        NodeList polygons = solid1.getElementsByTagName(TagName.GML_POLYGON);
        for (int i = 0; i < polygons.getLength(); i++) {
            Element polygon = (Element) polygons.item(i);
            if (this.isTouch(polygon, solid2)) return true;
        }
        return false;
    }

    /**
     * Check polygon and solid touch
     *
     * @param polygon a polygon of solid
     * @param solid   a solid which need to check touch
     * @return true if polygon touch 1 polygon of solid and not intersect the others polygon of solid
     */
    private boolean isTouch(Element polygon, Element solid) throws IOException {
        Element exterior1 = (Element) polygon.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
        Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
        String[] posString1 = posList1.getTextContent().trim().split(" ");

        int count = 0;
        NodeList polygonSolids = solid.getElementsByTagName(TagName.GML_POLYGON);
        int totalPolygonOfSolid = polygonSolids.getLength();
        for (int i = 0; i < totalPolygonOfSolid; i++) {
            Element polygonOfSolid = (Element) polygonSolids.item(i);
            Element exterior2 = (Element) polygonOfSolid.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString2 = posList2.getTextContent().trim().split(" ");

            Map<String, String> runCmdResult = PythonUtil.checkIntersecWithPyCmd(AppConst.PATH_PYTHON, posString1, posString2);
            if (!runCmdResult.get("ERROR").isBlank()) return false;
            String output = runCmdResult.get("OUTPUT").trim();
            if (Objects.equals(output, Relationship.INTERSECT)) return false;
            if (Objects.equals(output, Relationship.TOUCH) || Objects.equals(output, Relationship.FLAT_INTERSECT)) continue;
            if (Objects.equals(output, Relationship.NOT_INTERSECT)) {
                count++;
            }
        }
        return count == totalPolygonOfSolid;
    }
}