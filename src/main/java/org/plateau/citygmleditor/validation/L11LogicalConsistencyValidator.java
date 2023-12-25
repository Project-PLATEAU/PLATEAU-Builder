package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
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

        public String getLodTag() {
            return lodTag;
        }

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
    public List<ValidationResultMessage> validate(CityModel cityModelView) throws ParserConfigurationException, IOException, SAXException {
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
            String content = polygon.getTextContent().trim();
            NodeList tagPosList = polygon.getElementsByTagName(TagName.GML_POSLIST);

            if (!this.isPosListValid(tagPosList, standard)) {
                polygonIdInvalids.add(content);
            }
        }

        return polygonIdInvalids;
    }

    private boolean isPosListValid(NodeList tagPoslists, String standard) {
        for (int i = 0; i < tagPoslists.getLength(); i++) {
            Node tagPosList = tagPoslists.item(i);
            Element posList = (Element) tagPosList;

            String[] posString = posList.getTextContent().trim().split(" ");

            try {
                // split posList into points
                List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);
                return this.arePointsInAPlane(point3Ds, standard);
            } catch (RuntimeException e) {
                return false;
            }
        }

        return true;
    }


    private boolean arePointsInAPlane(List<Point3D> points, String standard) {
        if (points.size() <= 3) return true;

        // resolve plane equator by 3 points
        double[] planeEquation = this.findPlaneEquation(points.get(0), points.get(1), points.get(2));

        if (planeEquation[0] == 0.0 && planeEquation[1] == 0.0 && planeEquation[2] == 0.0) {
            logger.severe("Plane is not exist");
            throw new RuntimeException("Invalid Coefficient");
        }

        for (int i = 3; i < points.size(); i++) {
            boolean isValidStandard;
            if (Objects.equals(standard, L11)) {
                isValidStandard = this.distanceFromPointToPlane(points.get(i), planeEquation) == 0.0;
                return isValidStandard;
            }
            if (Objects.equals(standard, L12)) {
                isValidStandard = this.distanceFromPointToPlane(points.get(i), planeEquation) <= 0.03;
                return isValidStandard;
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

    /**
     * The equation of the plane is 'Ax + By + Cz + D = 0'
     * Get Coefficients of the equation (A,B,C,D)
     */
    private double[] findPlaneEquation(Point3D point1, Point3D point2, Point3D point3) {
        Point3D vector12 = point2.subtract(point1);
        Point3D vector23 = point3.subtract(point2);

        double[] planeEquation = new double[4];
        double xNormalVector = this.calculateDeterminant(vector12.getY(), vector12.getZ(), vector23.getY(), vector23.getZ());
        double yNormalVector = this.calculateDeterminant(vector12.getZ(), vector12.getX(), vector23.getZ(), vector23.getX());
        double zNormalVector = this.calculateDeterminant(vector12.getX(), vector12.getY(), vector23.getX(), vector23.getY());

        Vec3f normalVector = new Vec3f((float) xNormalVector, (float) yNormalVector, (float) zNormalVector);
        normalVector.normalize();
        // Coefficient A
        planeEquation[0] = normalVector.x;
        // Coefficient B
        planeEquation[1] = normalVector.y;
        // Coefficient C
        planeEquation[2] = normalVector.z;
        // Coefficient D
        planeEquation[3] = -(normalVector.x * point1.getX() + normalVector.y * point1.getY() + normalVector.z * point1.getZ());

        return planeEquation;
    }

    private double calculateDeterminant(double a1, double a2, double b1, double b2) {
        return a1 * b2 - a2 * b1;
    }
}
