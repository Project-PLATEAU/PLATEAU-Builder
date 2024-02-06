package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Geometry;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Lbldg02LogicalConsistencyValidator implements IValidator {
    static class BuildingInvalid {
        private String ID;
        private List<String> buildingParts;
        private List<String> polygons;

        public void setPolygons(List<String> polygons) {
            this.polygons = polygons;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setBuidlingPart(List<String> buidlingPart) {
            this.buildingParts = buidlingPart;
        }

        public String toString() {
            if (CollectionUtil.isEmpty(polygons) && CollectionUtil.isEmpty(buildingParts)) return "";
            if (CollectionUtil.isEmpty(buildingParts)) {
                String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n"
                        + this.polygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>")
                        .collect(Collectors.joining(":\n"));
                return String.format(MessageError.ERR_L_BLDG_02_001, ID, polygonStr, "");
            }
            if (CollectionUtil.isEmpty(polygons)) {
                String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
                return String.format(MessageError.ERR_L_BLDG_02_001, ID, bpStr, "");
            }
            String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
            String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n" + this.polygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>").collect(Collectors.joining(":\n"));
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

        public String getID() {
            return ID;
        }

        @Override
        public String toString() {
            return "bldg:BuildingPart = " + this.ID + " solid = " + solid;
        }
    }

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<GmlElementError> elementErrors = new ArrayList<>();
        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            NodeList tagBuildingParts = building.getElementsByTagName(TagName.BLGD_BUILDING_PART);
            if (tagBuildingParts.getLength() == 0) continue;
            List<Node> lod1Solid = createLod1Solid(tagBuildingParts);
            if (lod1Solid.isEmpty()) continue;
            // validate building parts
            List<String> invalidBP = this.getInvalidLod1Solids(lod1Solid);
            // validate format points
            List<String> invalidPolygon = this.getWrongFormatePolygon(tagBuildingParts);
            if (invalidPolygon.isEmpty() && invalidBP.isEmpty()) continue;
            this.setBuildingError(buildingID, invalidPolygon, invalidBP, buildingInvalids, elementErrors);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            String buildingStr = invalid.toString();
            if (buildingStr.isBlank()) continue;
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingStr, elementErrors));
        }
        return messages;
    }

    private List<Node> createLod1Solid(NodeList tagBuildingParts) {
        List<Node> result = new ArrayList<>();
        for (int j = 0; j < tagBuildingParts.getLength(); j++) {
            Element buildingPart = (Element) tagBuildingParts.item(j);
            Node lod1Solid = buildingPart.getElementsByTagName(TagName.BLDG_LOD_1_SOLID).item(0);
            if (lod1Solid == null) continue;
            result.add(lod1Solid);
        }
        return result;
    }

    private void setBuildingError(String buildingID, List<String> invalidPolygon, List<String> invalidBP, List<BuildingInvalid> buildingInvalids, List<GmlElementError> elementErrors) {
        BuildingInvalid buildingInvalid = new BuildingInvalid();
        buildingInvalid.setID(buildingID);
        buildingInvalid.setPolygons(invalidPolygon);
        buildingInvalid.setBuidlingPart(invalidBP);
        buildingInvalids.add(buildingInvalid);
        elementErrors.add(new GmlElementError(
                buildingID,
                invalidBP.toString(),
                invalidPolygon.toString(),
                null,
                null,
                0));
    }

    public List<String> getWrongFormatePolygon(NodeList nodeList) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            NodeList posLists = element.getElementsByTagName(TagName.GML_POSLIST);
            for (int j = 0; j < posLists.getLength(); j++) {
                Node posList = posLists.item(j);
                String[] posString = posList.getTextContent().trim().split(" ");
                if (posString.length % 3 != 0) {
                    Element parent = (Element) XmlUtil.findNearestParentByName(posList, TagName.GML_POLYGON);
                    assert parent != null;
                    String polygonID = parent.getAttribute(TagName.GML_ID);
                    result.add(polygonID.isBlank() ? "[]" : polygonID);
                }
            }
        }
        return result;
    }

    private List<Node> getPolygonByAttrbute(Node buildingPart, String attrValue) {
        List<Node> result = new ArrayList<>();
        XmlUtil.recursiveGetNodeByTagNameAndAttr(buildingPart, result, TagName.GML_POLYGON, TagName.ATTR_XLINK_HREF, attrValue);
        return result;
    }

    private List<String> getInvalidLod1Solids(List<Node> lod1Solid) {
        List<String> invalidSolids = new ArrayList<>();
        for (int i = 0; i < lod1Solid.size(); i++) {
            Node solid = lod1Solid.get(i);
            if (!this.checkTouch(lod1Solid, i)) {
                invalidSolids.add(((Element) solid).getAttribute(TagName.GML_ID));
            }
        }
//        if (!invalidSolids.isEmpty()) {
//            String blgPartID = buildingPart.getAttribute(TagName.GML_ID);
//            invalidSolids.add(blgPartID.isBlank() ? "[]" : blgPartID);
//        }

        return invalidSolids;
    }

    private boolean checkTouch(List<Node> solids, int index) {
        for (int i = 0; i < solids.size(); i++) {
            Element solid1 = (Element) solids.get(i);
            for (int j = 0; j < solids.size(); j++) {
                Element solid2 = (Element) solids.get(j);
                if (j != index && this.touch(solid1, solid2)) return true;
            }
        }
        return false;
    }

    private boolean checkTouch(NodeList solids, int index) {
        for (int i = 0; i < solids.getLength(); i++) {
            Element solid1 = (Element) solids.item(i);
            for (int j = 0; j < solids.getLength(); j++) {
                Element solid2 = (Element) solids.item(j);
                if (j != index && this.touch(solid1, solid2)) return true;
            }
        }
        return false;
    }

    /**
     * one face of solid touch any face of solid2 => solid 1 touch solid 2
     */
    private boolean touch(Element solid1, Element solid2) {
        NodeList polygon1s = solid1.getElementsByTagName(TagName.GML_POLYGON);
        NodeList polygon2s = solid2.getElementsByTagName(TagName.GML_POLYGON);
        for (int i = 0; i < polygon1s.getLength(); i++) {
            Element polygon1 = (Element) polygon1s.item(i);
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");
            List<Point3D> point3D1s = ThreeDUtil.createListPoint(posString1);
            for (int j = 0; j < polygon2s.getLength(); j++) {
                Element polygon2 = (Element) polygon2s.item(i);
                Element exterior2 = (Element) polygon2.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
                String[] posString2 = posList2.getTextContent().trim().split(" ");
                List<Point3D> point3D2s = ThreeDUtil.createListPoint(posString2);
                // check 2 polygons touch
                if (this.touch(point3D1s, point3D2s)) return true;
            }
        }
        return false;
    }

    /**
     * 2 polyon is touch in 3D when they're in a same plane and intersect
     */
    private boolean touch(List<Point3D> polygon1, List<Point3D> polygon2) {
        var plane1 = SolveEquationUtil.findPlaneEquation(polygon1.get(0), polygon1.get(1), polygon1.get(2));
        var plane2 = SolveEquationUtil.findPlaneEquation(polygon2.get(0), polygon2.get(1), polygon2.get(2));
        if (!SolveEquationUtil.onPlane(plane1, plane2)) return false;

        Geometry geometry1 = ThreeDUtil.createPolygon(polygon1);
        Geometry geometry2 = ThreeDUtil.createPolygon(polygon2);
        return geometry1.intersects(geometry2);
    }
}