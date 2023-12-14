package org.plateau.citygmleditor.validation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javafx.geometry.Point3D;
import javafx.util.Pair;
import javax.xml.parsers.ParserConfigurationException;
import org.citygml4j.model.citygml.core.CityModel;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class L08CompletenessValidator implements IValidator {

  public static Logger logger = Logger.getLogger(XmlUtil.class.getName());

  public List<ValidationResultMessage> validate(CityModel cityModel)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
    NodeList lineStringNodes = XmlUtil.getAllTagFromXmlFile(file, TagName.LINESTRING);
    Map<Node, Set<Node>> buildingWithErrorLineString = new HashMap<>();

    for (int i = 0; i < lineStringNodes.getLength(); i++) {
      Node lineStringNode = lineStringNodes.item(i);
      Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(lineStringNode, TagName.BUILDING, TagName.GML_ID);

      List<LineSegment3D> lineSegments = getLineSegments(lineStringNode);

      // If there is only 1 line segment, there is no intersection
      if (lineSegments.size() >= 2) {
        // Convolution combination 2 of ine segments
        for (int j = 0; j < lineSegments.size() - 1; j++) {
          LineSegment3D first = lineSegments.get(j);
          for (int k = j + 1; k < lineSegments.size(); k++) {
            LineSegment3D second = lineSegments.get(k);
            // Check if 2 line segments are intersected
            // if j == k-1, 2 line segments are continuous
            if (isLineSegmentsIntersect(first.getStart(), first.getEnd(), second.getStart(), second.getEnd(), j == k-1)) {
              // Update error list of building
              Set<Node> errorList = buildingWithErrorLineString.getOrDefault(buildingNode, new HashSet<>());
              errorList.add(lineStringNode);
              buildingWithErrorLineString.put(buildingNode, errorList);
            }
          }
        }
      }
    }

    buildingWithErrorLineString.forEach((buildingNode, lineStringNodesWithError) -> {
      String gmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      var msg = String.format("Building which gml:id=\"%s\" has %s <gml:LineString> invalid", gmlId, lineStringNodesWithError.size());
      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, msg));
    });

    return messages;
  }

  private List<LineSegment3D> getLineSegments(Node lineStringNode) {
    Element lineStringElement = (Element) lineStringNode;
    String[] posString = lineStringElement.getElementsByTagName(TagName.POSLIST).item(0).getTextContent().split(" ");
    // Convert string to 3d points
    List<Point3D> points = get3dPoints(posString);
    // Convert 3d points to line segments
    return getLineSegments(points);
  }

  private List<LineSegment3D> getLineSegments(List<Point3D> points) {
    List<LineSegment3D> lineSegments = new ArrayList<>();
    for (int i = 0; i < points.size() - 1; i++) {
      Point3D start = points.get(i);
      Point3D end = points.get(i + 1);
      LineSegment3D lineSegment = new LineSegment3D(start, end);
      lineSegments.add(lineSegment);
    }
    return lineSegments;
  }

  private List<Point3D> get3dPoints(String[] posString) {
    int length = posString.length;
    if (length == 0 || length % 3 != 0) {
      throw new RuntimeException("Invalid String");
    }

    List<Point3D> points = new ArrayList<>();
    for (int i = 0; i <= length - 3; ) {
      try {
        double x = Double.parseDouble(posString[i++]);
        double y = Double.parseDouble(posString[i++]);
        double z = Double.parseDouble(posString[i++]);
        Point3D point = new Point3D(x, y, z);
        points.add(point);
      } catch (NumberFormatException e) {
        logger.severe("Error when parse from string to double");
        throw new RuntimeException("Invalid String");
      }
    }
    return points;
  }

  private Pair<Double, Double> solveLinearEquation(double a1, double b1, double c1, double a2, double b2, double c2) {
    double d = a1 * b2 - a2 * b1;
    if (d == 0) {
      return null;
    }
    double dx = c1 * b2 - c2 * b1;
    double dy = a1 * c2 - a2 * c1;
    return new Pair<>(dx / d, dy / d);
  }

  private boolean isPointOnLineSegment(Point3D point, Point3D start, Point3D end) {
    // Check if the point lies on the line defined by the start and end points
    Point3D direction = end.subtract(start);
    Point3D pointToStart = point.subtract(start);

    // Check if the vectors are collinear
    double dotProduct = direction.dotProduct(pointToStart);
    if (Math.abs(dotProduct) < 0) {
      return false;
    }

    // Check if the point is within the line segment
    double lengthSquared = direction.dotProduct(direction);
    double projectionLength = dotProduct / lengthSquared;
    boolean isOnLineSegment = projectionLength >= 0 && projectionLength <= 1;
    if (isOnLineSegment) {
      printIntersectionInfo(new LineSegment3D(start, end), new LineSegment3D(start, end), point);
      return true;
    }

    return false;
  }

  private boolean isLineSegmentsIntersect(Point3D firstStart, Point3D firstEnd,
      Point3D secondStart, Point3D secondEnd, boolean lineSegmentsAreContinuous) {

      // check if 2 lines are continuous
      if (lineSegmentsAreContinuous) {
          if (firstEnd.equals(secondStart) && !firstStart.equals(secondEnd)) {
              return false;
          }
      }

    Point3D line1Vector = firstEnd.subtract(firstStart);
    Point3D line2Vector = secondEnd.subtract(secondStart);

    // check if 2 lines are parallel or coincident
    if (line1Vector.crossProduct(line2Vector).equals(Point3D.ZERO)) {
      // Check if 2 line segments are coincident if one of the points of one line is on the other line segment
      return isPointOnLineSegment(firstStart, secondStart, secondEnd)
          || isPointOnLineSegment(firstEnd, secondStart, secondEnd)
          || isPointOnLineSegment(secondStart, firstStart, firstEnd)
          || isPointOnLineSegment(secondEnd, firstStart, firstEnd);
    }

    var parameterT1T2 = solveLinearEquation(
        line1Vector.getX(), line2Vector.getX(), secondStart.getX() - firstStart.getX(),
        line1Vector.getY(), line2Vector.getY(), secondStart.getY() - firstStart.getY()
    );

    if (parameterT1T2 == null) {
      // 2 lines are parallel or coincident
      return false;
    }

    var t1 = parameterT1T2.getKey();
    var t2 = parameterT1T2.getValue();

    Point3D intersect = firstStart.add(line1Vector.multiply(t1));

    if (t1 >= 0 && t1 <= 1 && t2 >= 0 && t2 <= 1) {
      // 2 lines are intersected
      printIntersectionInfo(new LineSegment3D(firstStart, firstEnd), new LineSegment3D(secondStart, secondEnd), intersect);
      return true;
    }

    return false;
  }

  private void printIntersectionInfo(LineSegment3D line1, LineSegment3D line2,
      Point3D intersection) {

    String lineInfo = String.format("Line 1: (%f, %f, %f) -> (%f, %f, %f)", line1.getStart().getX(),
        line1.getStart().getY(), line1.getStart().getZ(), line1.getEnd().getX(),
        line1.getEnd().getY(), line1.getEnd().getZ());

    String line2Info = String.format("Line 2: (%f, %f, %f) -> (%f, %f, %f)",
        line2.getStart().getX(), line2.getStart().getY(), line2.getStart().getZ(),
        line2.getEnd().getX(), line2.getEnd().getY(), line2.getEnd().getZ());

    String intersectionInfo = String.format("Intersection: (%f, %f, %f)", intersection.getX(),
        intersection.getY(), intersection.getZ());

    logger.info("----------------------------------\n" + lineInfo + "\n" + line2Info + "\n"
        + intersectionInfo);

  }
}
