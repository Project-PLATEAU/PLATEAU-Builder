package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.citygml4j.model.citygml.core.CityModel;
import org.plateau.citygmleditor.constant.TagName;
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
import java.util.ArrayList;
import java.util.List;


public class L11CompletenessValidator implements IValidator {
    private final String LOD1 = ".*[lLoOdD]1.*";

    static class LOD1Invalid {
        private String lod1ID;

        private List<String> polygonID;

        public String getLod1ID() {
            return lod1ID;
        }

        public void setLod1ID(String lod1ID) {
            this.lod1ID = lod1ID;
        }

        public List<String> getPolygonID() {
            return polygonID;
        }

        public void setPolygonID(List<String> polygonID) {
            this.polygonID = polygonID;
        }

        public String toString() {
            return "LOD1 = " + lod1ID + " [Polygon = " + polygonID.size() + "]";
        }
    }

    static class BuildingInvalid {
        private String buildingID;

        private List<LOD1Invalid> lod1Invalids;

        public String getBuildingID() {
            return buildingID;
        }

        public void setBuildingID(String buildingID) {
            this.buildingID = buildingID;
        }

        public List<LOD1Invalid> getLod1Invalids() {
            return lod1Invalids;
        }

        public void setLod1Invalids(List<LOD1Invalid> lod1Invalids) {
            this.lod1Invalids = lod1Invalids;
        }

        public String toString() {
            return "buildingID = " + buildingID + "\n" + " [LOD1Id = " + lod1Invalids + "]";
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModel cityModel) throws ParserConfigurationException, IOException, SAXException {
        File gmlFile = new File(World.getActiveInstance().getCityModel().getGmlPath());
        // get buildings from gml file
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(gmlFile, TagName.BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            List<Node> tagLOD1s = XmlUtil.getTagsByRegex(LOD1, tagBuilding);
            List<LOD1Invalid> lod1Invalids1 = this.getInvalidLOD1(tagLOD1s);

            if (CollectionUtil.collectionEmpty(lod1Invalids1)) continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setLod1Invalids(lod1Invalids1);
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.collectionEmpty(buildingInvalids)) return null;
        List<ValidationResultMessage> messages = new ArrayList<>();
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, String.format("%sは重複して使用されています。\n", buildingInvalids)));
        return messages;
    }

    private List<LOD1Invalid> getInvalidLOD1(List<Node> tagLOD1s) {
        List<LOD1Invalid> result = new ArrayList<>();
        for (Node nodeLOD1 : tagLOD1s) {
            Element lod1 = (Element) nodeLOD1;
            NodeList tagPolygons = lod1.getElementsByTagName(TagName.POLYGON);
            List<String> polygonInvalids = this.getListPolygonInvalid(tagPolygons);

            if (CollectionUtil.collectionEmpty(polygonInvalids)) continue;
            LOD1Invalid lod1Invalid = new LOD1Invalid();
            String lod1ID = lod1.getAttribute(TagName.GML_ID);
            lod1Invalid.setLod1ID(lod1ID);
            lod1Invalid.setPolygonID(polygonInvalids);
            result.add(lod1Invalid);
        }

        return result;
    }

    private List<String> getListPolygonInvalid(NodeList tagPolygons) {
        List<String> polygonIdInvalids = new ArrayList<>();

        for (int i = 0; i < tagPolygons.getLength(); i++) {
            Element polygon = (Element) tagPolygons.item(i);
            String attribute = polygon.getAttribute(TagName.GML_ID);
            NodeList tagPosList = polygon.getElementsByTagName(TagName.POSLIST);

            if (!this.isPosListValid(tagPosList)) {
                polygonIdInvalids.add(attribute);
            }
        }

        return polygonIdInvalids;
    }

    private boolean isPosListValid(NodeList tagPoslists) {
        for (int i = 0; i < tagPoslists.getLength(); i++) {
            Node tagPosList = tagPoslists.item(i);
            Element posList = (Element) tagPosList;

            String[] posString = posList.getTextContent().trim().split(" ");

            try {
                // split posList into points
                List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);
                return this.arePointsInAPlane(point3Ds);
            } catch (RuntimeException e) {
                return false;
            }
        }

        return true;
    }


    private boolean arePointsInAPlane(List<Point3D> points) {
        if (points.size() <= 3) return true;

        // resolve plane equator by 3 points
        double[] planeEquation = this.findPlaneEquation(points.get(0), points.get(1), points.get(2));
        for (int i = 3; i < points.size(); i++) {
            double x = points.get(i).getX();
            double y = points.get(i).getY();
            double z = points.get(i).getZ();
            boolean isPointOnPlane = planeEquation[0] * x + planeEquation[1] * y + planeEquation[2] * z + planeEquation[3] == 0.0;
            if (!isPointOnPlane) return false;
        }

        return true;
    }

    private double[] findPlaneEquation(Point3D point1, Point3D point2, Point3D point3) {
        Point3D vector12 = point2.subtract(point1);
        Point3D vector23 = point3.subtract(point2);

        double[] planeEquation = new double[4];
        double xNormalVector = this.calculateDeterminant(vector12.getY(), vector12.getZ(), vector23.getY(), vector23.getZ());
        double yNormalVector = this.calculateDeterminant(vector12.getZ(), vector12.getX(), vector23.getZ(), vector23.getX());
        double zNormalVector = this.calculateDeterminant(vector12.getX(), vector12.getY(), vector23.getX(), vector23.getY());

        Vec3f normalVector = new Vec3f((float) xNormalVector, (float) yNormalVector, (float) zNormalVector);
        normalVector.normalize();
        // The equation of the plane is 'Ax + By + Cz + D = 0'
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