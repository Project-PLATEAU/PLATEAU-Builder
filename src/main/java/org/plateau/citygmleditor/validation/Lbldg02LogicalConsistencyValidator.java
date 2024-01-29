package org.plateau.citygmleditor.validation;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.PolygonRelationship;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.PythonUtil;
import org.plateau.citygmleditor.validation.exception.GeometryPyException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class Lbldg02LogicalConsistencyValidator implements IValidator {
    static class BuildingInvalid {
        private String ID;
        private List<BuildingPart> buildingParts;
        private List<String> polygons;

        public void setPolygons(List<String> polygons) {
            this.polygons = polygons;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setBuidlingPart(List<BuildingPart> buidlingPart) {
            this.buildingParts = buidlingPart;
        }

        public String toString() {
            if (CollectionUtil.isEmpty(polygons) && CollectionUtil.isEmpty(buildingParts)) return "";
            String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n" + this.polygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>").collect(Collectors.joining(":\n"));
            String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
            if (CollectionUtil.isEmpty(buildingParts)) {
                return String.format(MessageError.ERR_L_BLDG_02_001, ID, polygonStr, "");
            }
            if (CollectionUtil.isEmpty(polygons)) {
                return String.format(MessageError.ERR_L_BLDG_02_001, ID, bpStr, "");
            }
            return String.format(MessageError.ERR_L_BLDG_02_001, ID, polygonStr, "\n" + bpStr);
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
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid building part tags
            NodeList tagBuildingParts = building.getElementsByTagName(TagName.BLGD_BUILDING_PART);
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setID(buildingID);
            this.setFieldError(tagBuildingParts, buildingInvalid);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            String buildingStr = invalid.toString();
            if (buildingStr.isBlank()) continue;
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingStr));
        }
        return messages;
    }

    private void setFieldError(NodeList buildingParts, BuildingInvalid building) throws IOException {
        List<BuildingPart> invalidBPs = new ArrayList<>();

        for (int i = 0; i < buildingParts.getLength(); i++) {
            Element buildingPart = (Element) buildingParts.item(i);
            NodeList solids = buildingPart.getElementsByTagName(TagName.GML_SOLID);
            // building part have only one solid always valid
            if (solids.getLength() <= 1) {
                building.setBuidlingPart(Collections.EMPTY_LIST);
                return;
            }
            List<String> invalidSolid = this.validateSolidAndSetPolygon(solids, building);
            if (invalidSolid.isEmpty()) continue;

            BuildingPart invalidBP = new BuildingPart();
            invalidBP.setID(buildingPart.getAttribute(TagName.GML_ID));
            invalidBP.setSolid(invalidSolid);
            invalidBPs.add(invalidBP);
        }
        if (!invalidBPs.isEmpty()) {
            building.setBuidlingPart(invalidBPs);
        }
    }

    private List<String> validateSolidAndSetPolygon(NodeList solids, BuildingInvalid buildingInvalid) throws IOException {
        List<String> result = new ArrayList<>();

        outterLoop:
        for (int i = 0; i < solids.getLength(); i++) {
            Element solid1 = (Element) solids.item(i);
            for (int j = i + 1; j < solids.getLength(); j++) {
                Element solid2 = (Element) solids.item(j);
                if (this.checkTouchAndSetInvalidPolygon(solid1, solid2, buildingInvalid)) continue outterLoop;
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
    private boolean checkTouchAndSetInvalidPolygon(Element solid1, Element solid2, BuildingInvalid buildingInvalid) throws IOException {
        List<String> invalidPolygons = new ArrayList<>();
        NodeList polygons = solid1.getElementsByTagName(TagName.GML_POLYGON);
        for (int i = 0; i < polygons.getLength(); i++) {
            Element polygon = (Element) polygons.item(i);
            Element exterior1 = (Element) polygon.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");
            int length = posString1.length;
            // check posString closed or miss x,y,z
            boolean isClosed = Objects.equals(posString1[0], posString1[length - 3]) && Objects.equals(posString1[1], posString1[length - 2]) && Objects.equals(posString1[2], posString1[length - 1]);
            if (length % 3 != 0 || isClosed) {
                invalidPolygons.add(polygon.getAttribute(TagName.GML_ID));
            }
            if (this.isTouch(posString1, solid2)) return true;
        }
        if (!invalidPolygons.isEmpty()) {
            buildingInvalid.setPolygons(invalidPolygons);
        }
        return false;
    }

    /**
     * Check polygon and solid touch
     *
     * @param posString1 a list coornidate of polygon
     * @param solid      a solid which need to check touch
     * @return true if polygon touch 1 polygon of solid and not intersect the others polygon of solid
     */
    private boolean isTouch(String[] posString1, Element solid) throws IOException {
        NodeList polygonSolids = solid.getElementsByTagName(TagName.GML_POLYGON);
        int totalPolygonOfSolid = polygonSolids.getLength();
        for (int i = 0; i < totalPolygonOfSolid; i++) {
            Element polygonOfSolid = (Element) polygonSolids.item(i);
            Element exterior2 = (Element) polygonOfSolid.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString2 = posList2.getTextContent().trim().split(" ");

            try {
                PolygonRelationship relationship = PythonUtil.checkPolygonRelationship(AppConst.PATH_PYTHON, posString1, posString2);
                // polyyon intersect in 3D 1 face of solid is invalid
                if (relationship == PolygonRelationship.INTERSECT_3D) return false;
                // polygon touch or intersect in flat of 1 face of solid is valid
                if (relationship == PolygonRelationship.TOUCH || relationship == PolygonRelationship.FLAT_INTERSECT) {
                    return true;
                }
            } catch (GeometryPyException geometryPyException) {
                return false;
            }
        }
        return false;
    }
}