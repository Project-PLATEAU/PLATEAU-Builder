package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.citygml4j.model.citygml.core.CityModel;
import org.locationtech.jts.geom.LineSegment;
import org.plateau.citygmleditor.constant.TagName;
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
import java.util.*;
import java.util.logging.Logger;


public class L09LogicalConsistencyValidator implements IValidator {

  public static Logger logger = Logger.getLogger(L09LogicalConsistencyValidator.class.getName());

  public List<ValidationResultMessage> validate(CityModel cityModel)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
    NodeList linearRingNodes = XmlUtil.getAllTagFromXmlFile(file, TagName.GML_LINEARRING);
    Map<Node, Set<Node>> buildingWithErrorLinearRing = new HashMap<>();

    for (int i = 0; i < linearRingNodes.getLength(); i++) {
      Node linearRingNode = linearRingNodes.item(i);
      checkPointsNonDuplicatedAndClosed(linearRingNode, buildingWithErrorLinearRing);
      checkPointsIntersect(linearRingNode, buildingWithErrorLinearRing);
    }

    buildingWithErrorLinearRing.forEach((buildingNode, linearRingNodesWithError) -> {
      String gmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      var msg = String.format("L09: Building which gml:id=\"%s\" has %s <gml:LinearRing> invalid", gmlId, linearRingNodesWithError.size());
      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, msg));
    });

    return messages;
  }

  private void checkPointsIntersect(Node linearRingNode, Map<Node, Set<Node>> buildingWithErrorLinearRing) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BLDG_BUILDING, TagName.GML_ID);

    List<LineSegment> lineSegments = getLineSegments(linearRingNode);

    // If there is only 1 line segment, there is no intersection
    if (lineSegments.size() >= 2) {
      // Convolution combination 2 of ine segments
      for (int j = 0; j < lineSegments.size() - 1; j++) {
        LineSegment first = lineSegments.get(j);
        for (int k = j + 1; k < lineSegments.size(); k++) {
          LineSegment second = lineSegments.get(k);
          // Check if 2 line segments are intersected
          // if j == k-1, 2 line segments are continuous
          boolean isContinuous = j == k-1;
          // Line first and last are also continuous
          boolean isReverseContinuous = j == 0 && k == lineSegments.size() - 1;

          boolean isValid;

          if (isContinuous) {
            isValid = ThreeDUtil.isLinesContinuous(first, second);
          } else if (isReverseContinuous) {
            isValid = ThreeDUtil.isLinesContinuous(second, first);
          } else {
            isValid = !ThreeDUtil.isLineIntersect(first, second);
          }

          if (!isValid) {
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
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BLDG_BUILDING, TagName.GML_ID);

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
    // There are 2 cases: posList and pos of LinearRing
    NodeList posListNodes = linearRingElement.getElementsByTagName(TagName.GML_POSLIST);
    NodeList posNodes = linearRingElement.getElementsByTagName(TagName.GML_POS);

    if (posListNodes.getLength() > 0) {
      String[] posString = posListNodes.item(0).getTextContent().split(" ");
      ThreeDUtil.createListPoint(posString);
    } else if (posNodes.getLength() > 0) {
      List<Point3D> point3DS = new ArrayList<>();
      for (int i = 0; i < posNodes.getLength(); i++) {
        Node posNode = posNodes.item(i);
        String[] posString = posNode.getTextContent().split(" ");
        point3DS.addAll(ThreeDUtil.createListPoint(posString));
      }
      return point3DS;
    }
    return List.of();
  }

  private List<LineSegment> getLineSegments(Node linearRingNode) {
    // Convert 3d points to line segments
    return ThreeDUtil.getLineSegments(get3dPoints(linearRingNode));
  }
}
