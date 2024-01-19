package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.LineSegment;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;


public class L09LogicalConsistencyValidator implements IValidator {

  public static Logger logger = Logger.getLogger(L09LogicalConsistencyValidator.class.getName());

  public List<ValidationResultMessage> validate(CityModelView cityModelView)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    NodeList linearRingNodes = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.GML_LINEARRING);
    Map<Node, Set<Node>> buildingWithErrorLinearRing = new HashMap<>();

    for (int i = 0; i < linearRingNodes.getLength(); i++) {
      Node linearRingNode = linearRingNodes.item(i);
      try {
        checkPointsNonDuplicatedAndClosed(linearRingNode, buildingWithErrorLinearRing);
        checkPointsIntersect(linearRingNode, buildingWithErrorLinearRing);
      } catch (InvalidPosStringException e) {
        Node parentNode = XmlUtil.findNearestParentByAttribute(linearRingNodes.item(i), TagName.GML_ID);
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                MessageFormat.format(MessageError.ERR_L09_001,
                        parentNode.getAttributes().getNamedItem("gml:id").getTextContent(),
                        linearRingNodes.item(i).getFirstChild().getNodeValue())));
      }
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
    List<LineSegment> lineSegments;

    try {
      lineSegments = getLineSegments(linearRingNode);
    } catch (InvalidPosStringException e) {
      // If there is any error when parsing posString, update error list of building
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, linearRingNode);
      return;
    }

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
            // 2 line segments should not have no intersection
            isValid = first.intersection(second) == null;
          }

          if (!isValid) {
            logger.severe(String.format("L09 Line have (%s and %s) is not countinuous", first, second));
            // Update error list of building
            CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, linearRingNode);

          }
        }
      }
    }
  }

  private void checkPointsNonDuplicatedAndClosed(Node linearRingNode, Map<Node, Set<Node>> buildingWithErrorLinearRing) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BLDG_BUILDING, TagName.GML_ID);

    try {

      List<Point3D> points = get3dPoints(linearRingNode);

      boolean isClosed = points.get(0).equals(points.get(points.size() - 1));
      if (!isClosed) {
        logger.severe(String.format("L09 polygon have is don't close startpoint (%s) and endpoint (%s)", points.get(0), points.get(points.size() - 1)));
      }

      // Only first and last point are duplicated
      // Check if there is any other duplicated point
      boolean isDuplicated = new HashSet<>(points).size() != points.size() - 1;
      if (isDuplicated){
        logger.severe(String.format("L09 polygon have duplicate point (%s)", points));
      }
      if (!isClosed || isDuplicated) {
        CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, linearRingNode);
      }
    } catch (InvalidPosStringException e) {
      // If there is any error when parsing posString, update error list of building
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, linearRingNode);
    }
  }

  private List<Point3D> get3dPoints(Node linearRingNode) {
    Element linearRingElement = (Element) linearRingNode;
    // There are 2 cases: posList and pos of LinearRing
    NodeList posListNodes = linearRingElement.getElementsByTagName(TagName.GML_POSLIST);
    NodeList posNodes = linearRingElement.getElementsByTagName(TagName.GML_POS);

    if (posListNodes.getLength() > 0) {
      String[] posString = posListNodes.item(0).getTextContent().split(" ");
      return ThreeDUtil.createListPoint(posString);
    } else if (posNodes.getLength() > 0) {
      List<Point3D> point3DS = new ArrayList<>();
      for (int i = 0; i < posNodes.getLength(); i++) {
        Node posNode = posNodes.item(i);
        String[] posString = posNode.getTextContent().split(" ");
        point3DS.addAll(ThreeDUtil.createListPoint(posString));
      }
      return point3DS;
    }
    return new ArrayList<>();
  }

  private List<LineSegment> getLineSegments(Node linearRingNode) {
    // Convert 3d points to line segments
    return ThreeDUtil.getLineSegments(get3dPoints(linearRingNode));
  }
}
