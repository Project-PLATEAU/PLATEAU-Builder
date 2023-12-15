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
import org.plateau.citygmleditor.utils.SolveEquationUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class L09CompletenessValidator implements IValidator {

  public static Logger logger = Logger.getLogger(L09CompletenessValidator.class.getName());

  public List<ValidationResultMessage> validate(CityModel cityModel)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
    NodeList linearRingNodes = XmlUtil.getAllTagFromXmlFile(file, TagName.LINEARRING);
    Map<Node, Set<Node>> buildingWithErrorLinearRing = new HashMap<>();

    for (int i = 0; i < linearRingNodes.getLength(); i++) {
      Node linearRingNode = linearRingNodes.item(i);
      checkPointsNonDuplicatedAndClosed(linearRingNode, buildingWithErrorLinearRing);
      checkPointsIntersect(linearRingNode, buildingWithErrorLinearRing);
    }

    buildingWithErrorLinearRing.forEach((buildingNode, linearRingNodesWithError) -> {
      String gmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      var msg = String.format("Building which gml:id=\"%s\" has %s <gml:LinearRing> invalid", gmlId, linearRingNodesWithError.size());
      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, msg));
    });

    return messages;
  }

  private void checkPointsIntersect(Node linearRingNode, Map<Node, Set<Node>> buildingWithErrorLinearRing) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BUILDING, TagName.GML_ID);

    List<LineSegment3D> lineSegments = getLineSegments(linearRingNode);

    // If there is only 1 line segment, there is no intersection
    if (lineSegments.size() >= 2) {
      // Convolution combination 2 of ine segments
      for (int j = 0; j < lineSegments.size() - 1; j++) {
        LineSegment3D first = lineSegments.get(j);
        for (int k = j + 1; k < lineSegments.size(); k++) {
          LineSegment3D second = lineSegments.get(k);
          // Check if 2 line segments are intersected
          // if j == k-1, 2 line segments are continuous
          boolean isContinuous = j == k-1;
          // Line first and last are also continuous
          boolean isReverseContinuous = j == 0 && k == lineSegments.size() - 1;

          boolean isIntersect;

          if (isReverseContinuous) {
            isIntersect = ThreeDUtil.isLineSegmentsIntersect(second.getStart(), second.getEnd(), first.getStart(), first.getEnd(), true);
          } else {
            isIntersect = ThreeDUtil.isLineSegmentsIntersect(first.getStart(), first.getEnd(), second.getStart(), second.getEnd(), isContinuous);
          }

          if (isIntersect) {
            // Update error list of building
            Set<Node> errorList = buildingWithErrorLinearRing.getOrDefault(buildingNode, new HashSet<>());
            errorList.add(linearRingNode);
            buildingWithErrorLinearRing.put(buildingNode, errorList);
          }
        }
      }
    }
  }

  private void checkPointsNonDuplicatedAndClosed(Node linearRingNode, Map<Node, Set<Node>> buildingWithErrorLinearRing) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BUILDING, TagName.GML_ID);

    List<Point3D> points = get3dPoints(linearRingNode);

    boolean isClosed = points.get(0).equals(points.get(points.size() - 1));

    // Only first and last point are duplicated
    // Check if there is any other duplicated point
    boolean isDuplicated = new HashSet<>(points).size() != points.size() - 1;

    if (!isClosed || isDuplicated) {
      Set<Node> errorList = buildingWithErrorLinearRing.getOrDefault(buildingNode, new HashSet<>());
      errorList.add(linearRingNode);
      buildingWithErrorLinearRing.put(buildingNode, errorList);
    }
  }

  private List<Point3D> get3dPoints(Node linearRingNode) {
    Element linearRingElement = (Element) linearRingNode;
    String[] posString = linearRingElement.getElementsByTagName(TagName.POSLIST).item(0).getTextContent().split(" ");
    // Convert string to 3d points
    return ThreeDUtil.createListPoint(posString);
  }

  private List<LineSegment3D> getLineSegments(Node linearRingNode) {
    // Convert 3d points to line segments
    return ThreeDUtil.getLineSegments(get3dPoints(linearRingNode));
  }
}
