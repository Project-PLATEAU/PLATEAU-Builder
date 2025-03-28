package org.plateaubuilder.validation;

import javafx.geometry.Point3D;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.validation.constant.MessageError;
import org.plateaubuilder.validation.constant.TagName;
import org.plateaubuilder.validation.exception.InvalidPosStringException;
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

    public String toString(String err1, String err2) {
      StringBuilder messageError = new StringBuilder(MessageFormat.format(err1, buildingID));
      for (LODInvalid lodInvalid : lodInvalids) {
        messageError.append(MessageFormat.format(err2, lodInvalid.polygon));
      }
      return messageError.toString();
    }
  }

  @Override
  public List<ValidationResultMessage> validate(CityModelView cityModelView)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();
    // get buildings from gml file
    NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);

    for (int i = 0; i < buildings.getLength(); i++) {
      Node tagBuilding = buildings.item(i);
      Element building = (Element) tagBuilding;
      String buildingID = building.getAttribute(TagName.GML_ID);
      List<Node> tagLODs = XmlUtil.getTagsByRegex(LOD1, tagBuilding);
      List<LODInvalid> lodInvalids = this.getInvalidLOD(tagLODs, L11, messages);

        if (lodInvalids.isEmpty()) {
            continue;
        }
      BuildingInvalid buildingInvalid = new BuildingInvalid();
      buildingInvalid.setBuildingID(buildingID);
      buildingInvalid.setLodInvalids(lodInvalids);

      List<GmlElementError> elementErrors = new ArrayList<>();
      for (LODInvalid lodInvalid : lodInvalids) {
        for (String polygonId : lodInvalid.getPolygon()) {
          elementErrors.add(new GmlElementError(buildingID, null, polygonId, lodInvalids.toString(), "LOD1", 0));
        }
      }

      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
          buildingInvalid.toString(MessageError.ERR_L11_002, MessageError.ERR_L11_002_1), elementErrors));
    }

    return messages;
  }

  public List<LODInvalid> getInvalidLOD(List<Node> tagLOD, String standard, List<ValidationResultMessage> messages) {
    List<LODInvalid> result = new ArrayList<>();
    for (Node lodNode : tagLOD) {
      Element lod = (Element) lodNode;
      NodeList tagPolygons = lod.getElementsByTagName(TagName.GML_POLYGON);
      List<String> polygonInvalids = this.getListPolygonInvalid(tagPolygons, standard, messages);

        if (polygonInvalids.isEmpty()) {
            continue;
        }
      LODInvalid lodInvalid = new LODInvalid();
      String content = lod.getTagName().trim();
      lodInvalid.setLodTag(content);
      lodInvalid.setPolygon(polygonInvalids);
      result.add(lodInvalid);
    }

    return result;
  }

  private List<String> getListPolygonInvalid(NodeList tagPolygons, String standard,
      List<ValidationResultMessage> messages) {
    List<String> polygonIdInvalids = new ArrayList<>();

    for (int i = 0; i < tagPolygons.getLength(); i++) {
      Element polygon = (Element) tagPolygons.item(i);
      String polygonID = polygon.getAttribute(TagName.GML_ID);
      NodeList tagPosList = polygon.getElementsByTagName(TagName.GML_POSLIST);

      List<String> invalidPoslist = this.getListCoordinateInvalid(tagPosList, standard, messages);
        if (invalidPoslist.isEmpty()) {
            continue;
        }
      polygonIdInvalids.add(polygonID.isBlank() ? String.valueOf(invalidPoslist) : polygonID);
    }

    return polygonIdInvalids;
  }

  private List<String> getListCoordinateInvalid(NodeList tagPoslists, String standard,
      List<ValidationResultMessage> messages) {
    List<String> invalid = new ArrayList<>();

    for (int i = 0; i < tagPoslists.getLength(); i++) {
      Node tagPosList = tagPoslists.item(i);
      Element posList = (Element) tagPosList;

      String[] posString = posList.getTextContent().trim().split(" ");
      try {
        // split posList into points
        List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);
          if (this.arePointsInAPlane(point3Ds, standard)) {
              continue;
          }
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

  private boolean arePointsInAPlane(List<Point3D> points, String standard) {
      if (points.size() <= 3) {
          return true;
      }

    // resolve plane equator by 3 points
    double[] planeEquation = SolveEquationUtil.findPlaneEquation(points.get(0), points.get(1), points.get(2));

    if (planeEquation[0] == 0.0 && planeEquation[1] == 0.0 && planeEquation[2] == 0.0) {
      logger.severe("Plane is not exist");
      throw new RuntimeException("Invalid Coefficient");
    }

    for (int i = 3; i < points.size(); i++) {
      if (Objects.equals(standard, L11)) {
        boolean inValidStandard = this.distanceFromPointToPlane(points.get(i), planeEquation) != 0.0;
          if (inValidStandard) {
              return false;
          }
      }
      if (Objects.equals(standard, L12)) {
        boolean inValidStandard = this.distanceFromPointToPlane(points.get(i), planeEquation) > 0.03;
          if (inValidStandard) {
              return false;
          }
      }
    }

    return true;
  }

  private double distanceFromPointToPlane(Point3D pointInput, double[] plane) {
    Point3D pointProject = this.projectOntoPlane(plane, pointInput);
    return ThreeDUtil.distance(pointInput, pointProject);
  }

  /**
   * The equation of the line is {x = x0 + at, y = y0 + bt , z = z0 + ct} with a,b,c is coordinates of plane's
   * normal_vector Find the projection of the point on the plane
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