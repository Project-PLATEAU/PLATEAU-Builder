package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.*;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;


public class L11LogicalConsistencyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(L11LogicalConsistencyValidator.class.getName());
    private final String LOD1 = ".*[lLoOdD]1.*";
    private final String L11 = "L11";
    private final String L12 = "L12";

    static class LODInvalid {
        private String lodTag;

        private List<String> polygon;

        public void setLodTag(String lodTag) {
            this.lodTag = lodTag;
        }

        public List<String> getPolygon() {
            return polygon;
        }

        public void setPolygon(List<String> polygon) {
            this.polygon = polygon;
        }

        public String toString() {
            return "LOD = " + lodTag + " [Polygon = " + polygon + "]";
        }
    }

    static class BuildingInvalid {
        private String buildingID;

        private List<LODInvalid> lodInvalids;

        public String getBuildingID() {
            return buildingID;
        }

        public void setBuildingID(String buildingID) {
            this.buildingID = buildingID;
        }

        public void setLodInvalids(List<LODInvalid> lodInvalids) {
            this.lodInvalids = lodInvalids;
        }

        public String toString() {
            return "buildingID = " + buildingID + " \n" + lodInvalids + "]";
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        // get buildings from gml file
        NodeList buildings = CityGmlUtil.getAllTagFromCityModel(cityModelView, TagName.BLDG_BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            List<Node> tagLODs = XmlUtil.getTagsByRegex(LOD1, tagBuilding);
            List<LODInvalid> lodInvalids = this.getInvalidLOD(tagLODs, L11);

            if (CollectionUtil.isEmpty(lodInvalids)) continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setLodInvalids(lodInvalids);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    MessageFormat.format(MessageError.ERR_L11_001, invalid)));
        }
        return messages;
    }

    public List<LODInvalid> getInvalidLOD(List<Node> tagLOD, String standard) {
        List<LODInvalid> result = new ArrayList<>();
        for (Node lodNode : tagLOD) {
            Element lod = (Element) lodNode;
            NodeList tagPolygons = lod.getElementsByTagName(TagName.GML_POLYGON);
            List<String> polygonInvalids = this.getListPolygonInvalid(tagPolygons, standard);

            if (CollectionUtil.isEmpty(polygonInvalids)) continue;
            LODInvalid lodInvalid = new LODInvalid();
            String content = lod.getTagName().trim();
            lodInvalid.setLodTag(content);
            lodInvalid.setPolygon(polygonInvalids);
            result.add(lodInvalid);
        }

        return result;
    }

    private List<String> getListPolygonInvalid(NodeList tagPolygons, String standard) {
        List<String> polygonIdInvalids = new ArrayList<>();

        for (int i = 0; i < tagPolygons.getLength(); i++) {
            Element polygon = (Element) tagPolygons.item(i);
            String polygonID = polygon.getAttribute(TagName.GML_ID);
            NodeList tagPosList = polygon.getElementsByTagName(TagName.GML_POSLIST);

            List<String> invalidPoslist = this.getListCoordinateInvalid(tagPosList, standard);
            if (invalidPoslist.isEmpty()) continue;
            polygonIdInvalids.add(polygonID.isBlank() ? String.valueOf(invalidPoslist) : polygonID);
        }

        return polygonIdInvalids;
    }

    private List<String> getListCoordinateInvalid(NodeList tagPoslists, String standard) {
        List<String> invalid = new ArrayList<>();

        for (int i = 0; i < tagPoslists.getLength(); i++) {
            Node tagPosList = tagPoslists.item(i);
            Element posList = (Element) tagPosList;

            String[] posString = posList.getTextContent().trim().split(" ");
            try {
                // split posList into points
                List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);
                if (this.arePointsInAPlane(point3Ds, standard)) continue;
                invalid.add(Arrays.toString(posString));
            } catch (InvalidPosStringException e) {
                invalid.add(Arrays.toString(posString));
            }
        }
        return invalid;
    }

    private boolean arePointsInAPlane(List<Point3D> points, String standard) {
        if (points.size() <= 3) return true;

        // resolve plane equator by 3 points
        double[] planeEquation = SolveEquationUtil.findPlaneEquation(points.get(0), points.get(1), points.get(2));

        if (planeEquation[0] == 0.0 && planeEquation[1] == 0.0 && planeEquation[2] == 0.0) {
            logger.severe("Plane is not exist");
            throw new RuntimeException("Invalid Coefficient");
        }

        for (int i = 3; i < points.size(); i++) {
            if (Objects.equals(standard, L11)) {
                boolean inValidStandard = this.distanceFromPointToPlane(points.get(i), planeEquation) != 0.0;
                if (inValidStandard) return false;
            }
            if (Objects.equals(standard, L12)) {
                boolean inValidStandard = this.distanceFromPointToPlane(points.get(i), planeEquation) > 0.03;
                if (inValidStandard) return false;
            }
        }

        return true;
    }

    private double distanceFromPointToPlane(Point3D pointInput, double[] plane) {
        Point3D pointProject = this.projectOntoPlane(plane, pointInput);
        return ThreeDUtil.distance(pointInput, pointProject);
    }

    /**
     * The equation of the line is {x = x0 + at, y = y0 + bt , z = z0 + ct} with a,b,c is coordinates of plane's normal_vector
     * Find the projection of the point on the plane
     *
     * @param plane known
     * @param point need to find project
     */
    private Point3D projectOntoPlane(double[] plane, Point3D point) {
        // find t in the equation of line
        double a = plane[0];
        double b = plane[1];
        double c = plane[2];
        double d = plane[3];
        double t = -(a * point.getX() + b * point.getY() + c * point.getZ() + d) / (a * a + b * b + c * c);

        return new Point3D(a * t + point.getX(), b * t + point.getY(), c * t + point.getZ());
    }
}
