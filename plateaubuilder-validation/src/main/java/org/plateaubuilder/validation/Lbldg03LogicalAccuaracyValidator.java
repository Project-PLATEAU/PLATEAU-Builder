package org.plateaubuilder.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Geometry;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.validation.constant.TagName;
import org.plateaubuilder.validation.invalid.Lbldg03BuildingError;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Lbldg03LogicalAccuaracyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(Lbldg03LogicalAccuaracyValidator.class.getName());

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<Lbldg03BuildingError> buildingErrors = new ArrayList<>();
        List<GmlElementError> elementErrors = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModel).getElementsByTagName(TagName.BLDG_BUILDING);
        try {
            for (int i = 0; i < buildings.getLength(); i++) {
                Element building = (Element) buildings.item(i);
                this.validdateBuilding(building, buildingErrors, elementErrors);
            }
        } catch (Exception e) {
            logger.severe("L bldg 03 catch exception: " + e);
        }
        if (buildingErrors.isEmpty()) return List.of();

        // add buildings error to show
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (int i = 0; i < buildingErrors.size(); i++) {
            Lbldg03BuildingError invalid = buildingErrors.get(i);
            String buildingStr = invalid.toString();
            if (buildingStr.isBlank()) continue;
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingStr, List.of(elementErrors.get(i))));
        }
        return messages;
    }

    private void validdateBuilding(Element building, List<Lbldg03BuildingError> buildingErrors, List<GmlElementError> elementErrors) {
        String buildingID = building.getAttribute(TagName.GML_ID);
        NodeList lod3MultiSurface = building.getElementsByTagName(TagName.BLDG_WALLSURFACE);
        List<String> totalDoors = new ArrayList<>();
        List<String> totalWindows = new ArrayList<>();
        List<String> totalPolygons = new ArrayList<>();
        for (int j = 0; j < lod3MultiSurface.getLength(); j++) {
            Element lod3 = (Element) lod3MultiSurface.item(j);
            // create doors, windows, walls from lod3Multisurface
            Map<String, List<Node>> wallAndOpenings = this.createWallAndOpening(lod3);
            if (wallAndOpenings == null) continue;
            List<Node> walls = wallAndOpenings.get("WALLS");
            List<Node> doors = wallAndOpenings.get("DOORS");
            List<Node> windows = wallAndOpenings.get("WINDOWS");
            // validate openings
            List<String> invalidDoors = this.getInvalidOpenings(walls, doors);
            List<String> invalidWindows = this.getInvalidOpenings(walls, windows);
            // validate format polygon
            List<String> invalidPolygons = this.getWrongFormatPolygon(lod3);
            if (invalidPolygons.isEmpty() && invalidDoors.isEmpty() && invalidWindows.isEmpty()) continue;
            addInvalid(invalidDoors, totalDoors);
            addInvalid(invalidWindows, totalWindows);
            addInvalid(invalidPolygons, totalPolygons);
        }
        if (totalDoors.isEmpty() && totalWindows.isEmpty() && totalPolygons.isEmpty()) return;
        // set building error
        setBuildingError(buildingErrors, elementErrors, buildingID, totalDoors, totalWindows, totalPolygons);
    }

    private static void addInvalid(List<String> invalidDoors, List<String> totalDoors) {
        if (!invalidDoors.isEmpty()) {
            totalDoors.addAll(invalidDoors);
        }
    }

    private static void setBuildingError(List<Lbldg03BuildingError> buildingErrors, List<GmlElementError> elementErrors,
                                         String buildingID, List<String> invalidDoors, List<String> invalidWindows,
                                         List<String> invalidPolygons) {
        Lbldg03BuildingError buildingInvalid = new Lbldg03BuildingError();
        buildingInvalid.setBuildingID(buildingID);
        buildingInvalid.setDoors(invalidDoors);
        buildingInvalid.setWindows(invalidWindows);
        buildingInvalid.setPolygons(invalidPolygons);
        buildingErrors.add(buildingInvalid);
        elementErrors.add(new GmlElementError(
                buildingID,
                null,
                invalidPolygons.toString(),
                null,
                null, 0));
    }

    public List<String> getWrongFormatPolygon(Element element) {
        List<String> result = new ArrayList<>();
        NodeList posLists = element.getElementsByTagName(TagName.GML_POSLIST);
        for (int j = 0; j < posLists.getLength(); j++) {
            Node posList = posLists.item(j);
            String[] posString = posList.getTextContent().trim().split(" ");
            if (posString.length % 3 != 0 || !this.isClosed(posString)) {
                Element parent = (Element) XmlUtil.findNearestParentByName(posList, TagName.GML_POLYGON);
                assert parent != null;
                String polygonID = parent.getAttribute(TagName.GML_ID);
                result.add(polygonID.isBlank() ? "[]" : polygonID);
            }
        }
        return result;
    }

    private boolean isClosed(String[] polygon) {
        int length = polygon.length;
        return Objects.equals(polygon[0], polygon[length - 3]) && Objects.equals(polygon[1], polygon[length - 2]) && Objects.equals(polygon[2], polygon[length - 1]);
    }

    private List<String> getInvalidOpenings(List<Node> walls, List<Node> openings) {
        List<String> openingIDs = new ArrayList<>();
        for (Node opening : openings) {
            boolean isValid = this.validateOpening(opening, walls);
            if (!isValid) {
                Element openingE = (Element) opening;
                openingIDs.add(openingE.getAttribute(TagName.GML_ID));
            }
        }
        return openingIDs;
    }

    private boolean validateOpening(Node opening, List<Node> walls) {
        List<Point3D> pointOfOpening = getPoint3Ds((Element) opening);
        // check window or door is closed
        boolean openingIsClose = pointOfOpening.get(0).equals(pointOfOpening.get(pointOfOpening.size() - 1));
        if (!openingIsClose) return false;

        for (Node wall : walls) {
            List<Point3D> pointOfWall = getPoint3Ds((Element) wall);
            int size = pointOfWall.size();
            // check wall is closed
            boolean wallIsClose = pointOfWall.get(0).equals(pointOfWall.get(size - 1));
            if (!wallIsClose) continue;

            double[] wallPlane = SolveEquationUtil.findPlaneEquation(pointOfWall.get(0), pointOfWall.get(1), pointOfWall.get(2));
            // if wallPlane is null mean 3 points have 2 points are same line, need to re-find wall plane by 3 others points
            if (wallPlane == null) wallPlane = this.reFindWallPlane(pointOfWall);
            if (wallPlane == null || (wallPlane[0] == 0.0 && wallPlane[1] == 0.0 && wallPlane[2] == 0.0))
                return false;

            if (this.containYOZ(pointOfWall, pointOfOpening, wallPlane) || this.containXOZ(pointOfWall, pointOfOpening, wallPlane)) {
                return true;
            }
        }
        return false;
    }

    private boolean containYOZ(List<Point3D> wall, List<Point3D> opening, double[] wallPlane) {
        List<Point3D> projects = this.getProjectPoint3D(opening, wallPlane);
        List<Point3D> projectsYOZ = this.projectYOZ(projects);
        List<Point3D> wallYOZ = this.projectYOZ(wall);
        Geometry geometry1 = ThreeDUtil.createPolygon(wallYOZ);
        try {
            Geometry geometry2 = ThreeDUtil.createPolygon(projectsYOZ);
            if (geometry1.covers(geometry2)) {
                return true;
            }
        } catch (Exception e) {
            logger.severe("error: " + e);
            return false;
        }
        return false;
    }

    private boolean containXOZ(List<Point3D> wall, List<Point3D> opening, double[] wallPlane) {
        List<Point3D> projects = this.getProjectPoint3D(opening, wallPlane);
        List<Point3D> projectsXOZ = this.projectXOZ(projects);
        List<Point3D> wallXOZ = this.projectXOZ(wall);
        Geometry geometry1 = ThreeDUtil.createPolygon(wallXOZ);
        try {
            Geometry geometry2 = ThreeDUtil.createPolygon(projectsXOZ);
            if (geometry1.covers(geometry2)) {
                return true;
            }
        } catch (Exception e) {
            logger.severe("error: " + e);
            return false;
        }
        return false;
    }

    private List<Point3D> projectYOZ(List<Point3D> input) {
        return input.stream().map(p -> new Point3D(p.getY(), p.getZ(), p.getX())).collect(Collectors.toList());
    }

    private List<Point3D> projectXOZ(List<Point3D> input) {
        return input.stream().map(p -> new Point3D(p.getX(), p.getZ(), p.getY())).collect(Collectors.toList());
    }

    private double[] reFindWallPlane(List<Point3D> pointOfWall) {
        double[] wallPlane = new double[4];
        int size = pointOfWall.size();
        for (int i = 1; i < size; i++) {
            Point3D point1 = pointOfWall.get(i);
            for (int j = i + 1; j < size; j++) {
                if (j + 1 > size) break;
                Point3D point2 = pointOfWall.get(j);
                wallPlane = SolveEquationUtil.findPlaneEquation(point1, point2, pointOfWall.get(j + 1));
                if (wallPlane != null) return wallPlane;
            }
        }
        return wallPlane;
    }

    private List<Point3D> getPoint3Ds(Element element) {
        Element exterior = (Element) element.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
        Element posList = (Element) exterior.getElementsByTagName(TagName.GML_POSLIST).item(0);
        String[] posString = posList.getTextContent().trim().split(" ");
        return ThreeDUtil.createListPoint(posString);
    }

    private List<Point3D> getProjectPoint3D(List<Point3D> point3DS, double[] plane) {
        return point3DS.stream().map(p -> SolveEquationUtil.projectOntoPlane(plane, p)).collect(Collectors.toList());
    }

    private Map<String, List<Node>> createWallAndOpening(Element wallSurface) {
        NodeList lod3MultiSurfaces = wallSurface.getElementsByTagName(TagName.BLDG_LOD3_MULTISURFACE);
        List<Node> walls = this.createWalls(lod3MultiSurfaces);
        NodeList doorNodes = wallSurface.getElementsByTagName(TagName.BLDG_DOOR);
        List<Node> doors = this.createOpenings(doorNodes);
        NodeList windowNodes = wallSurface.getElementsByTagName(TagName.BLDG_WINDOW);
        List<Node> windows = this.createOpenings(windowNodes);

        if (doors.isEmpty() && windows.isEmpty()) return null;
        Map<String, List<Node>> result = new HashMap<>();
        result.put("WALLS", walls);
        result.put("WINDOWS", windows);
        result.put("DOORS", doors);
        return result;
    }

    private List<Node> createOpenings(NodeList doorNodes) {
        List<Node> openings = new ArrayList<>();
        for (int i = 0; i < doorNodes.getLength(); i++) {
            openings.add(doorNodes.item(i));
        }
        return openings;
    }

    private List<Node> createWalls(NodeList lod3MultiSurfaces) {
        List<Node> walls = new ArrayList<>();
        for (int i = 0; i < lod3MultiSurfaces.getLength(); i++) {
            Node lod3 = lod3MultiSurfaces.item(i);
            Node parent = lod3.getParentNode();
            if (Objects.equals(parent.getNodeName(), TagName.BLDG_WALLSURFACE)) {
                walls.add(lod3);
            }
        }
        return walls;
    }
}