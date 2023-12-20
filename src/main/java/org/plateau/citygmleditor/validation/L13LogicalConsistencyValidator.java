package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.citygml4j.model.citygml.core.CityModel;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class L13LogicalConsistencyValidator implements IValidator {
    static class BuildingInvalid {
        private String buildingID;

        private List<String> polygon;

        public String getBuildingID() {
            return buildingID;
        }

        public void setBuildingID(String buildingID) {
            this.buildingID = buildingID;
        }

        public List<String> getPolygon() {
            return polygon;
        }

        public void setPolygon(List<String> polygon) {
            this.polygon = polygon;
        }

        public String toString() {
            return "buildingID = " + buildingID + "\n" + " [Polygon = " + polygon + "]";
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModel cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<Point3D> exterior = List.of(new Point3D(0, 0, 0), new Point3D(10, 0, 0)
                , new Point3D(10, 8, 0), new Point3D(0, 8, 0), new Point3D(0, 0, 0));
        List<Point3D> interior1 = List.of(new Point3D(1, 2, 0), new Point3D(4, 2, 0)
                , new Point3D(4, 6, 0), new Point3D(1, 6, 0), new Point3D(1, 2, 0));
        List<Point3D> interior2 = List.of(new Point3D(6, 1, 0), new Point3D(8, 1, 0)
                , new Point3D(8, 5, 0), new Point3D(6, 5, 0), new Point3D(6, 1, 0));
        List<Point3D> interior3 = List.of(new Point3D(7, 0, 0), new Point3D(10, 0, 0)
                , new Point3D(10, 2, 0), new Point3D(7, 2, 0), new Point3D(7, 0, 0));
        this.isPolygonValid(exterior, List.of(interior1, interior2, interior3));

        File gmlFile = new File(World.getActiveInstance().getCityModel().getGmlPath());
        // get buildings from gml file
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(gmlFile, TagName.BLDG_BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            NodeList polygons = building.getElementsByTagName(TagName.GML_POLYGON);
            List<String> polygonInvalids = this.getPolygonInvalids(polygons);

            if (CollectionUtil.isEmpty(polygonInvalids)) continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setPolygon(polygonInvalids);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    MessageFormat.format(MessageError.ERR_L13_001, invalid)));
        }
        return messages;
    }

    private List<String> getPolygonInvalids(NodeList tagPolygons) {
        List<String> polygonIdInvalids = new ArrayList<>();

        for (int i = 0; i < tagPolygons.getLength(); i++) {
            Element polygon = (Element) tagPolygons.item(i);
            List<Point3D> exterior = this.getExterior(polygon);
            List<List<Point3D>> interior = this.getInteriors(polygon);

            if (this.isPolygonValid(exterior, interior)) continue;
            polygonIdInvalids.add(polygon.getAttribute(TagName.GML_ID));
        }

        return polygonIdInvalids;
    }

    private List<Point3D> getExterior(Element polygon) {
        Element exterior = (Element) polygon.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
        if (exterior == null) return null;
        Node posList = exterior.getElementsByTagName(TagName.GML_POSLIST).item(0);
        String[] posString = posList.getTextContent().trim().split(" ");
        return ThreeDUtil.createListPoint(posString);
    }

    private List<List<Point3D>> getInteriors(Element polygon) {
        NodeList interiors = polygon.getElementsByTagName(TagName.GML_INTERIOR);
        List<List<Point3D>> interiorPoints = new ArrayList<>();
        for (int i = 0; i < interiors.getLength(); i++) {
            Element interior = (Element) interiors.item(i);
            Node posList = interior.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString = posList.getTextContent().trim().split(" ");
            List<Point3D> point3DS = ThreeDUtil.createListPoint(posString);
            interiorPoints.add(point3DS);
        }
        return interiorPoints;
    }

    private boolean isPolygonValid(List<Point3D> points, List<List<Point3D>> listPoints) {
        if (points.isEmpty() || listPoints.isEmpty()) return true;
        Geometry exterior = ThreeDUtil.createPolygon(points);
        List<Geometry> interiors = new ArrayList<>();
        listPoints.forEach(p -> {
            Geometry interior = ThreeDUtil.createPolygon(p);
            interiors.add(interior);
        });

        // check exterior and interior intersect
        for (Geometry interior : interiors) {
            Geometry difference = exterior.difference(interior);
//            if (this.countIntersect(exterior, interior, difference)) return false;
        }
        // check interors intersect
        for (int i = 0; i < interiors.size() - 1; i++) {
            Geometry interior1 = interiors.get(i);
            for (int j = 0; j < interiors.size(); j++) {
                if (j <= i) continue;
                Geometry interior2 = interiors.get(j);
                Geometry difference = interior1.difference(interior2);
//                if (this.countIntersect(interior1, interior2, difference)) return false;
            }
        }

        return true;
    }

    /**
     * count number of points intersect
     *
     * @param geo1 known
     * @param geo2 known
     * @param dif  common part
     *
     */
    private int countIntersect(Geometry geo1, Geometry geo2) {
        Coordinate[] coordinates1 = geo1.getCoordinates();
        Coordinate[] coordinates2 = geo2.getCoordinates();
        int count = 0;
        for (int i = 0; i < coordinates1.length; i++) {
            Coordinate coor1 = coordinates1[i];
            for (int j = 0; j < coordinates2.length; j++) {
                if (j <= i) continue;
                Coordinate coor2 = coordinates1[j];
                if (coor1.equals3D(coor2)) count ++;
            }
        }

        return count;
    }
}
