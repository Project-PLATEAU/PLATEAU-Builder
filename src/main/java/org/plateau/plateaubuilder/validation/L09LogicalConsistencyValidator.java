package org.plateau.plateaubuilder.validation;

import javafx.geometry.Point3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.locationtech.jts.geom.LineSegment;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.validation.constant.MessageError;
import org.plateau.plateaubuilder.validation.constant.SegmentRelationship;
import org.plateau.plateaubuilder.validation.constant.TagName;
import org.plateau.plateaubuilder.utils.CityGmlUtil;
import org.plateau.plateaubuilder.utils.CollectionUtil;
import org.plateau.plateaubuilder.utils.ThreeDUtil;
import org.plateau.plateaubuilder.utils.XmlUtil;
import org.plateau.plateaubuilder.validation.exception.InvalidPosStringException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class L09LogicalConsistencyValidator implements IValidator {

  private final static int NO_ERROR = 0;
  private final static int SELF_INTERSECT_ERROR = 1;
  private final static int SELF_CONTACT_ERROR = 2;
  private final static int CLOSE_ERROR = 3;
  private final static int DUPLICATE_ERROR = 4;
  private final static int INVALID_FORMAT_EXCEPTION = 5;

  public static Logger logger = Logger.getLogger(L09LogicalConsistencyValidator.class.getName());

  public List<ValidationResultMessage> validate(CityModelView cityModelView)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    NodeList linearRingNodes = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.GML_LINEARRING);
    Map<Node, Set<LinearRingError>> buildingWithErrorLinearRing = new HashMap<>();

    for (int i = 0; i < linearRingNodes.getLength(); i++) {
      Node linearRingNode = linearRingNodes.item(i);
      try {
        if (isPointsNonDuplicatedAndClosed(linearRingNode, buildingWithErrorLinearRing)) {
          checkPointsIntersect(linearRingNode, buildingWithErrorLinearRing);
        }
      } catch (InvalidPosStringException e) {
        Node building = XmlUtil.findNearestParentByTagAndAttribute(linearRingNodes.item(i), TagName.BLDG_BUILDING, TagName.GML_ID);
        Node polygonNode = XmlUtil.findNearestParentByAttribute(linearRingNode, TagName.GML_POLYGON);

        int error = INVALID_FORMAT_EXCEPTION;
        GmlElementError gmlElementError = new GmlElementError(
                XmlUtil.getGmlId(building),
                null,
                XmlUtil.getGmlId(polygonNode),
                XmlUtil.getGmlId(linearRingNodes.item(i)),
                linearRingNodes.item(i).getNodeName(),
                error
        );

        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                MessageFormat.format(MessageError.ERR_L09_001,
                        XmlUtil.getGmlId(building),
                        linearRingNodes.item(i).getFirstChild().getNodeValue()),
                List.of(gmlElementError)));
      }
    }

    buildingWithErrorLinearRing.forEach((buildingNode, linearRingNodesWithError) -> {

      String gmlId = XmlUtil.getGmlId(buildingNode);
      String errorMessage = MessageFormat.format(MessageError.ERR_L09_002_1, gmlId);
      List<GmlElementError> gmlElementErrors = new ArrayList<>();

      for (LinearRingError linearRing : linearRingNodesWithError) {
        Node polygonNode = XmlUtil.findNearestParentByName(linearRing.getLinearRing(), TagName.GML_POLYGON);

        String errorElementNodeName = linearRing.getLinearRing().getFirstChild().getNodeName();
        int error = linearRing.getErrorCode();

        GmlElementError gmlElementError = new GmlElementError(
                gmlId,
                null,
                XmlUtil.getGmlId(polygonNode),
                XmlUtil.getGmlId(linearRing.getLinearRing()),
                errorElementNodeName,
                error
        );
        gmlElementErrors.add(gmlElementError);

        Node linearRingNode = linearRing.getLinearRing().getAttributes().getNamedItem(TagName.GML_ID);
        String linearRingId = linearRingNode != null ? linearRingNode.getTextContent() : "";
        errorMessage = errorMessage + setErrorMessage(linearRing.getErrorCode(), linearRingId);
      }

      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, errorMessage, gmlElementErrors));
    });

    return messages;
  }

  private void checkPointsIntersect(Node linearRingNode, Map<Node, Set<LinearRingError>> buildingWithErrorLinearRing) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BLDG_BUILDING, TagName.GML_ID);
    List<LineSegment> lineSegments;

    try {
      lineSegments = getLineSegments(linearRingNode);
    } catch (InvalidPosStringException e) {
      // If there is any error when parsing posString, update error list of building
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, new LinearRingError(
          INVALID_FORMAT_EXCEPTION, linearRingNode));
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
          int errorCode = NO_ERROR;
          if (isContinuous) {
            // End point of first line segment is start point of second line segment
            errorCode = first.p1.equals(second.p0) ? NO_ERROR : SELF_CONTACT_ERROR;
          } else if (isReverseContinuous) {
            // End point of second line segment is start point of first line segment
            errorCode = second.p1.equals(first.p0) ? NO_ERROR : SELF_CONTACT_ERROR;
          } else {
            var relationship = ThreeDUtil.checkSegmentsRelationship(
                new Vector3D(first.p0.x, first.p0.y, first.p0.z), new Vector3D(first.p1.x, first.p1.y, first.p1.z),
                new Vector3D(second.p0.x, second.p0.y, second.p0.z), new Vector3D(second.p1.x, second.p1.y, second.p1.z)
            );
            if (relationship == SegmentRelationship.INTERSECT || relationship == SegmentRelationship.OVERLAP) {
              errorCode = SELF_INTERSECT_ERROR;
            } else if (relationship == SegmentRelationship.TOUCH) {
              errorCode = SELF_CONTACT_ERROR;
            }
          }

          if (errorCode != NO_ERROR) {
            logger.severe(String.format("L09 Line have (%s and %s) is not valid", first, second));
            // Update error list of building
            CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, new LinearRingError(errorCode, linearRingNode));
            return;
          }
        }
      }
    }
  }

  private boolean isPointsNonDuplicatedAndClosed(Node linearRingNode, Map<Node, Set<LinearRingError>> buildingWithErrorLinearRing) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BLDG_BUILDING, TagName.GML_ID);

    try {

      List<Point3D> points = get3dPoints(linearRingNode);

      boolean isClosed = points.get(0).equals(points.get(points.size() - 1));
      if (!isClosed) {
        logger.severe(String.format("L09 polygon have is don't close startpoint (%s) and endpoint (%s)", points.get(0), points.get(points.size() - 1)));
        CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, new LinearRingError(CLOSE_ERROR, linearRingNode));
        return false;
      }

      // Only first and last point are duplicated
      // Check if there is any other duplicated point
      boolean isDuplicated = new HashSet<>(points).size() != points.size() - 1;
      if (isDuplicated){
        var duplicatePoints = points.stream().filter(i -> Collections.frequency(points, i) > 1).collect(Collectors.toSet());
        logger.severe(String.format("L09 polygon have duplicate point (%s)", duplicatePoints));
        CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, new LinearRingError(DUPLICATE_ERROR, linearRingNode));
        return false;
      }

    } catch (InvalidPosStringException e) {
      // If there is any error when parsing posString, update error list of building
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, new LinearRingError(
          INVALID_FORMAT_EXCEPTION, linearRingNode));
      return false;
    }

    return true;
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

  private String setErrorMessage(int errorCode, String linearRingId) {
    switch (errorCode) {
      case SELF_INTERSECT_ERROR:
        return MessageFormat.format(MessageError.ERR_L09_SELF_INTERSECT, linearRingId);
      case SELF_CONTACT_ERROR:
        return MessageFormat.format(MessageError.ERR_L09_SELF_CONTACT, linearRingId);
      case CLOSE_ERROR:
        return MessageFormat.format(MessageError.ERR_L09_NON_CLOSED, linearRingId);
      case DUPLICATE_ERROR:
        return MessageFormat.format(MessageError.ERR_L09_DUPLICATE_POINT, linearRingId);
      case INVALID_FORMAT_EXCEPTION:
        return MessageFormat.format(MessageError.ERR_L09_INVALID_FORMAT, linearRingId);
    }
    return "";
  }
}

class LinearRingError {
  private int errorCode;
  private Node linearRing;

  public LinearRingError() {}

  public LinearRingError(int errorCode, Node linearRing) {
    this.errorCode = errorCode;
    this.linearRing = linearRing;
  }

  public void setErrorCode (int errorCode) {
    this.errorCode = errorCode;
  }

  public int getErrorCode() {
    return this.errorCode;
  }

  public void setLinearRing(Node linearRing) {
    this.linearRing = linearRing;
  }

  public Node getLinearRing() {
    return this.linearRing;
  }
}
