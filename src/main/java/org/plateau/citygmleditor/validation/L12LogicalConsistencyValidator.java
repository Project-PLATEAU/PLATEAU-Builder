package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.*;
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
import java.util.logging.Logger;

public class L12LogicalConsistencyValidator implements IValidator {
    private final String LOD2OR3 = ".*[lLoOdD][23].*";
    public static Logger logger = Logger.getLogger(L12LogicalConsistencyValidator.class.getName());

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();
        // get buildings from gml file
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<L11LogicalConsistencyValidator.BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            List<GmlElementError> elementErrors = new ArrayList<>();
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            List<Node> tagLOD23s = XmlUtil.getTagsByRegex(LOD2OR3, tagBuilding);

            List<L11LogicalConsistencyValidator.LODInvalid> lodInvalids = this.getInvalidLOD(tagLOD23s, messages);

            if (CollectionUtil.isEmpty(lodInvalids)) continue;
            L11LogicalConsistencyValidator.BuildingInvalid buildingInvalid = new L11LogicalConsistencyValidator.BuildingInvalid();
            buildingInvalid.setBuildingID(buildingID);
            buildingInvalid.setLodInvalids(lodInvalids);
            buildingInvalids.add(buildingInvalid);
            elementErrors.add(new GmlElementError(buildingID, null, null, tagLOD23s.toString(), "LOD23", 0));

            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                            buildingInvalid.toString(MessageError.ERR_L12_002_1, MessageError.ERR_L12_002_2),
                    elementErrors));
        }

        return messages;
    }

    public List<L11LogicalConsistencyValidator.LODInvalid> getInvalidLOD(List<Node> tagLOD, List<ValidationResultMessage> messages) {
        List<L11LogicalConsistencyValidator.LODInvalid> result = new ArrayList<>();
        for (Node lodNode : tagLOD) {
            Element lod = (Element) lodNode;
            NodeList tagPolygons = lod.getElementsByTagName(TagName.GML_POLYGON);
            List<String> polygonInvalids = this.getListPolygonInvalid(tagPolygons, messages);

            if (CollectionUtil.isEmpty(polygonInvalids)) continue;
            L11LogicalConsistencyValidator.LODInvalid lodInvalid = new L11LogicalConsistencyValidator.LODInvalid();
            String content = lod.getTagName().trim();
            lodInvalid.setLodTag(content);
            lodInvalid.setPolygon(polygonInvalids);
            result.add(lodInvalid);
        }

        return result;
    }

    private List<String> getListPolygonInvalid(NodeList tagPolygons, List<ValidationResultMessage> messages) {
        List<String> polygonIdInvalids = new ArrayList<>();

        for (int i = 0; i < tagPolygons.getLength(); i++) {
            Element polygon = (Element) tagPolygons.item(i);
            String polygonID = polygon.getAttribute(TagName.GML_ID);
            NodeList tagPosList = polygon.getElementsByTagName(TagName.GML_POSLIST);

            List<String> invalidPoslist = this.getListCoordinateInvalid(tagPosList, messages);
            if (invalidPoslist.isEmpty()) continue;
            polygonIdInvalids.add(polygonID.isBlank() ? String.valueOf(invalidPoslist) : polygonID);
        }

        return polygonIdInvalids;
    }

    private List<String> getListCoordinateInvalid(NodeList tagPoslists, List<ValidationResultMessage> messages) {
        List<String> invalid = new ArrayList<>();

        for (int i = 0; i < tagPoslists.getLength(); i++) {
            Node tagPosList = tagPoslists.item(i);
            Element posList = (Element) tagPosList;

            String[] posString = posList.getTextContent().trim().split(" ");
            try {
                // split posList into points
                List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);
                if (this.isPoslistValid(point3Ds)) continue;
                invalid.add(Arrays.toString(posString));
            } catch (InvalidPosStringException e) {
                Node parentNode = XmlUtil.findNearestParentByAttribute(posList, TagName.GML_ID);
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                        MessageFormat.format(MessageError.ERR_L11_003,
                                parentNode.getAttributes().getNamedItem("gml:id").getTextContent(),
                                posList.getFirstChild().getNodeValue())));
                invalid.add(Arrays.toString(posString));
            }
        }
        return invalid;
    }

    private boolean isPoslistValid(List<Point3D> points) {
        // if polygon have <= 3 point always find the plone
        if (points.size() <= 3) return true;

        for (int i = 0; i < points.size(); i++) {
            Point3D point1 = points.get(i);
            for (int j = i + 1; j < points.size() - 2; j++) {
                Point3D point2 = points.get(j);
                Point3D point3 = points.get(j + 1);
                // resolve plane equator by 3 points
                double[] planeEquation = SolveEquationUtil.findPlaneEquation(point1, point2, point3);
                if (planeEquation[0] == 0.0 && planeEquation[1] == 0.0 && planeEquation[2] == 0.0) {
                    logger.severe("Plane is not exist");
                    throw new RuntimeException("Invalid Coefficient");
                }
                boolean inValidStandard = this.isDistanceValid(points, planeEquation);
                if (inValidStandard) return true;
            }
        }

        return false;
    }

    private boolean isDistanceValid(List<Point3D> points, double[] plane) {
        for (Point3D point : points) {
            Point3D pointProject = SolveEquationUtil.projectOntoPlane(plane, point);
            System.out.println(ThreeDUtil.distance(point, pointProject));
            if (ThreeDUtil.distance(point, pointProject) > 0.03) return false;
        }
        return true;
    }
}
