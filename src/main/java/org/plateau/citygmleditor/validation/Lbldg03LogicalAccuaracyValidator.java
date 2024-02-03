package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Geometry;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.SolveEquationUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class Lbldg03LogicalAccuaracyValidator implements IValidator {

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModel).getElementsByTagName(TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            NodeList lod3MultiSurface = building.getElementsByTagName(TagName.BLDG_WALLSURFACE);
            for (int j = 0; j < lod3MultiSurface.getLength(); j++) {
                Element lod3 = (Element) lod3MultiSurface.item(j);
                Map<String, List<Node>> wallAndOpenings = this.createWallAndOpening(lod3);

                if (wallAndOpenings == null) continue;
                List<Node> walls = wallAndOpenings.get("WALLS");
                List<Node> openings = wallAndOpenings.get("OPENINGS");
                this.getInvalidOpenings(walls, openings);
            }
        }

        return messages;
    }

    private List<String> getInvalidOpenings(List<Node> walls, List<Node> openings) {
        List<String> openingIDs = new ArrayList<>();
        for (Node wall : walls) {
            Element polygon1 = (Element) wall;
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");
            List<Point3D> point1s = ThreeDUtil.createListPoint(posString1);
            double[] plane1 = SolveEquationUtil.findPlaneEquation(point1s.get(0), point1s.get(1), point1s.get(2));
            Geometry geometry1 = ThreeDUtil.createPolygon(point1s);
            for (Node opening : openings) {
                Element polygon2 = (Element) opening;
                Element exterior2 = (Element) polygon2.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
                String[] posString2 = posList2.getTextContent().trim().split(" ");
                List<Point3D> point2s = ThreeDUtil.createListPoint(posString2);
                double[] plane2 = SolveEquationUtil.findPlaneEquation(point2s.get(0), point2s.get(1), point2s.get(2));
                Geometry geometry2 = ThreeDUtil.createPolygon(point2s);
                System.out.println(SolveEquationUtil.onPlane(plane1, plane2));
            }
        }
        return openingIDs;
    }

    private Map<String, List<Node>> createWallAndOpening(Element wallSurface) {
        NodeList lod3MultiSurfaces = wallSurface.getElementsByTagName(TagName.BLDG_LOD3_MULTISURFACE);
        List<Node> walls = new ArrayList<>();
        List<Node> openings = new ArrayList<>();
        for (int i = 0; i < lod3MultiSurfaces.getLength(); i++) {
            Node lod3 = lod3MultiSurfaces.item(i);
            Node parent = lod3.getParentNode();
            if (Objects.equals(parent.getNodeName(), TagName.BLDG_WALLSURFACE)) {
                walls.add(lod3);
            }
        }

        NodeList doors = wallSurface.getElementsByTagName(TagName.BLDG_DOOR);
        NodeList windows = wallSurface.getElementsByTagName(TagName.BLDG_WINDOW);
        for (int i = 0; i < doors.getLength(); i++) {
            openings.add(doors.item(i));
        }
        for (int i = 0; i < windows.getLength(); i++) {
            openings.add(windows.item(i));
        }

        if (openings.isEmpty()) return null;
        Map<String, List<Node>> result = new HashMap<>();
        result.put("WALLS", walls);
        result.put("OPENINGS", openings);
        return result;
    }
}