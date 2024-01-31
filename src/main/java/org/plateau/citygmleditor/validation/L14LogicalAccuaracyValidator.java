package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.PolygonRelationship;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.PythonUtil;
import org.plateau.citygmleditor.utils.SolveEquationUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.validation.exception.GeometryPyException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public class L14LogicalAccuaracyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(L14LogicalAccuaracyValidator.class.getName());

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<String> buildingErrors = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            Map<String, List<String>> invalidSolid = this.getInvalidSolid(building);
//            String selfIntersect = String.join("\n", invalidSolid.get("SELF_INTERSECT"));
            String isClosed = String.join("\n", invalidSolid.get("IS_NOT_CLOSED"));
            String notSameDirection = String.join("\n", invalidSolid.get("NOT_SAME_DIRECTION"));
            if (isClosed.isBlank() && notSameDirection.isBlank()) continue;
            String buildingError = MessageFormat.format(MessageError.ERR_L14_002_1, buildingID) + isClosed + "\n" + notSameDirection;
            buildingErrors.add(buildingError);
        }
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (String invalid : buildingErrors) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, invalid));
        }
        return messages;
    }

    private Map<String, List<String>> getInvalidSolid(Element building) throws IOException {
        NodeList solids = building.getElementsByTagName(TagName.GML_SOLID);
        Map<String, List<String>> result = new HashMap<>();
        List<String> polygonSelfIntersect = new ArrayList<>();
        List<String> polygonNotClosed = new ArrayList<>();
        List<String> polygonNoteSameDirection = new ArrayList<>();
        for (int i = 0; i < solids.getLength(); i++) {
            Element solid = (Element) solids.item(i);
            NodeList polygonNode = solid.getElementsByTagName(TagName.GML_POLYGON);
            // check polgyon closed
            String solidID = solid.getAttribute(TagName.GML_ID);
            if (!this.isPolygonClosed(polygonNode)) {
                polygonNotClosed.add("gml:Solid gml:id=" + (solidID.isBlank() ? "[]" : solidID) + "境界面が閉じていない。");
            }
            // check same derection
            if (!this.isValidDirection(polygonNode)) {
                polygonNoteSameDirection.add("gml:Solid gml:id=" + (solidID.isBlank() ? "[]" : solidID) + "全ての境界面の向きが外側を向いていない。");
            }
        }
        result.put("SELF_INTERSECT", polygonSelfIntersect);
        result.put("IS_NOT_CLOSED", polygonNotClosed);
        result.put("NOT_SAME_DIRECTION", polygonNoteSameDirection);
        return result;
    }

    private boolean isValidDirection(NodeList polgyonNode) {
        List<List<LineSegment3D>> polygons = this.createPolygons(polgyonNode);
        int size = polygons.size();
        for (int i = 0; i < size; i++) {
            if (!this.checkSameDrection(polygons, i)) return false;
        }
        return true;
    }

    /**
     * Check any direction's polygon of solid with others
     * return false if the polygon  have no same direction with any others
     */
    private boolean checkSameDrection(List<List<LineSegment3D>> listPolygons, int index) {
        List<LineSegment3D> polygonCheck = listPolygons.get(index);
        for (int i = 0; i < listPolygons.size(); i++) {
            // Don't need to check with itself
            if (i == index) continue;
            List<LineSegment3D> polygon = listPolygons.get(i);
            if (this.isSameDirection(polygonCheck, polygon)) return true;
        }
        return false;
    }

    private boolean isSameDirection(List<LineSegment3D> polygon1, List<LineSegment3D> polygon2) {
        for (LineSegment3D segment3D1 : polygon1) {
            for (LineSegment3D segment3D2 : polygon2) {
                if (segment3D1.getStart().equals(segment3D2.getEnd()) && segment3D1.getEnd().equals(segment3D2.getStart()))
                    return true;
            }
        }
        return false;
    }

    private boolean isPolygonClosed(NodeList polygonNodes) {
        List<List<LineSegment3D>> polygons = this.createPolygons(polygonNodes);
        // union polygon are on plane
        List<List<LineSegment3D>> polygonConvert = new ArrayList<>();
        List<Integer> indexFilter = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            List<LineSegment3D> polygon1 = polygons.get(i);
            double[] plane1 = SolveEquationUtil.findPlaneEquation(polygon1.get(0).getStart(), polygon1.get(0).getEnd(), polygon1.get(1).getEnd());
            for (int j = i + 1; j < polygons.size(); j++) {
                List<LineSegment3D> polygon2 = polygons.get(j);
                double[] plane2 = SolveEquationUtil.findPlaneEquation(polygon2.get(0).getStart(), polygon2.get(0).getEnd(), polygon2.get(1).getEnd());
                if (SolveEquationUtil.onPlane(plane1, plane2)) {
                    indexFilter.add(j);
                    polygon1 = this.union(polygon1, polygon2);
                }
            }
            if (indexFilter.isEmpty() || !indexFilter.contains(i)) {
                polygonConvert.add(polygon1);
            }
        }

        for (List<LineSegment3D> polygon1 : polygonConvert) {
            int count = 0;
            for (List<LineSegment3D> polygon2 : polygonConvert) {
                if (!checkDuplicateSegment(polygon1, polygon2)) count++;
            }
            // polygon have no duplicate segment with others polygon
            if (count == polygonConvert.size() - 1) return false;
        }
        return true;
    }

    private boolean checkDuplicateSegment(List<LineSegment3D> polygon1, List<LineSegment3D> polygon2) {
        for (LineSegment3D segment3D1 : polygon1) {
            for (LineSegment3D segment3D2 : polygon2) {
                if (segment3D1.equal(segment3D2)) return true;
            }
        }
        return false;
    }

    private List<List<LineSegment3D>> createPolygons(NodeList nodePolygon) {
        List<List<LineSegment3D>> polygons = new ArrayList<>();
        for (int i = 0; i < nodePolygon.getLength(); i++) {
            Element polygon1 = (Element) nodePolygon.item(i);
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");
            List<LineSegment3D> segment3DS = ThreeDUtil.getLineSegments(posString1);
            polygons.add(segment3DS);
        }
        return polygons;
    }

    /**
     * Get the edges with the condition of removing duplicate segments of the two polygons
     */
    private List<LineSegment3D> union(List<LineSegment3D> polygon1, List<LineSegment3D> polygon2) {
        List<LineSegment3D> result = new ArrayList<>(polygon1);

        for (LineSegment3D segment : polygon2) {
            Optional<LineSegment3D> flag = result.stream().filter(e -> e.equals(segment)).findFirst();
            if (flag.isPresent()) {
                int index = polygon2.indexOf(segment);
                result.remove(index);
            }
            if (flag.isEmpty()) {
                result.add(segment);
            }
        }
        return result;
    }

    private List<String> getSelfIntersectPolygon(NodeList polygons) throws IOException {
        List<String> result = new ArrayList<>();
        int totalPolygon = polygons.getLength();

        outterLoop:
        for (int i = 0; i < totalPolygon; i++) {
            Element polygon1 = (Element) polygons.item(i);
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");

            // check valid coornidate of polygon
            if (posString1.length % 3 != 0) {
                this.setPolygonError(polygon1, result);
            }
            int count = 0;
            for (int j = i + 1; j < totalPolygon; j++) {
                Element polygon2 = (Element) polygons.item(j);
                Element exterior2 = (Element) polygon2.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
                String[] posString2 = posList2.getTextContent().trim().split(" ");

                try {
                    PolygonRelationship relationship = PythonUtil.checkPolygonRelationship(AppConst.PATH_PYTHON, posString1, posString2);
                    // if 2 polygon intersect invalid
                    if (relationship == PolygonRelationship.INTERSECT_3D || relationship == PolygonRelationship.FLAT_INTERSECT) {
                        this.setPolygonError(polygon1, result);
                        continue outterLoop;
                    }
                    // 2 polygon touch valid compare other polygon of the solid
                    if (relationship == PolygonRelationship.TOUCH) {
                        continue;
                    }
                    // count the polygon not intersect to the others of solid
                    if (relationship == PolygonRelationship.NOT_INTERSECT) {
                        count++;
                    }
                } catch (GeometryPyException geometryPyException) {
                    this.setPolygonError(polygon1, result);
                }
            }
            if (count == totalPolygon - 1) {
                this.setPolygonError(polygon1, result);
            }
        }

        return result;
    }

    private void setPolygonError(Element polygon, List<String> invalidPolygon) {
        if (polygon.getAttribute(TagName.GML_ID).isBlank()) {
            invalidPolygon.add("Polygon=[] 境界面が自己交差している。");
        } else {
            invalidPolygon.add("Polygon=" + polygon.getAttribute(TagName.GML_ID) + "境界面が自己交差している。");
        }
    }
}
