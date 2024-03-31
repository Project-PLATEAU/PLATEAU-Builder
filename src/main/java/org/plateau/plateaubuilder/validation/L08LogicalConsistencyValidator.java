package org.plateau.plateaubuilder.validation;

import javafx.geometry.Point3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.locationtech.jts.geom.LineSegment;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.validation.constant.L08ErrorType;
import org.plateau.plateaubuilder.validation.constant.MessageError;
import org.plateau.plateaubuilder.validation.constant.SegmentRelationship;
import org.plateau.plateaubuilder.validation.constant.TagName;
import org.plateau.plateaubuilder.utils.CityGmlUtil;
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

public class L08LogicalConsistencyValidator implements IValidator {

  private static final int CONTINUOUS_ERROR_CODE = 1;
  private static final int INTERSECTION_ERROR_CODE = 2;
  private static final int TOUCH_ERROR_CODE = 3;

  public static Logger logger = Logger.getLogger(L08LogicalConsistencyValidator.class.getName());

  public List<ValidationResultMessage> validate(CityModelView cityModelView)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    NodeList lineStringNodes = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.GML_LINESTRING);
    Map<Node, Set<ErrorLineString>> buildingWithErrorLineString = new HashMap<>();

    for (int i = 0; i < lineStringNodes.getLength(); i++) {
      try {
        checkPointsIntersect(lineStringNodes.item(i), buildingWithErrorLineString);
      } catch (InvalidPosStringException e) {
        Node building = XmlUtil.findNearestParentByAttribute(lineStringNodes.item(i), TagName.GML_ID);
        Node polygon = XmlUtil.findNearestParentByName(lineStringNodes.item(i), TagName.GML_POLYGON);
        String errorElementNodeName = lineStringNodes.item(i).getFirstChild().getNodeName();

        int error = L08ErrorType.INVALID_FORMAT;

        GmlElementError gmlElementError = new GmlElementError(
                XmlUtil.getGmlId(building),
                null,
                XmlUtil.getGmlId(polygon),
                XmlUtil.getGmlId(lineStringNodes.item(i)),
                errorElementNodeName,
                error
        );

        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                        MessageFormat.format(MessageError.ERR_L08_002, XmlUtil.getGmlId(building), lineStringNodes.item(i).getFirstChild().getNodeValue(),
                        List.of(gmlElementError))));
      }
    }

    buildingWithErrorLineString.forEach((buildingNode, lineStringNodesWithError) -> {
      String buildingGmlId = XmlUtil.getGmlId(buildingNode);
      String msg = MessageFormat.format(MessageError.ERR_L08_001, buildingGmlId);

      List<GmlElementError> gmlElementErrors = new ArrayList<>();
      for (ErrorLineString errorLineString : lineStringNodesWithError) {
        String errorElementNodeName = errorLineString.getLineStringError().getFirstChild().getNodeName();
        int error = L08ErrorType.NO_ERROR;

        Node gmlIdItem = errorLineString.getLineStringError().getAttributes().getNamedItem(TagName.GML_ID);
        Node polygon = XmlUtil.findNearestParentByName(errorLineString.getLineStringError(), TagName.GML_POLYGON);

        String gmlId = gmlIdItem == null ? "" : gmlIdItem.getTextContent();

        if (errorLineString.getErrorCode() == INTERSECTION_ERROR_CODE) {
          error = L08ErrorType.INTERSECTION_ERROR_CODE;
          msg = msg + String.format("<gml:LineString gml:id=\"%s\">が自己交差しています。\n", gmlId);
        } else if (errorLineString.getErrorCode() == TOUCH_ERROR_CODE) {
          error = L08ErrorType.TOUCH_ERROR_CODE;
          msg = msg + String.format("<gml:LineString gml:id=\"%s\">が自己接触しています。\n", gmlId);
        } else if (errorLineString.getErrorCode() == CONTINUOUS_ERROR_CODE) {
          error = L08ErrorType.CONTINUOUS_ERROR_CODE;
          msg = msg + "\n" + gmlId;
        }
        
        gmlElementErrors.add(new GmlElementError(
                buildingGmlId,
                null,
                XmlUtil.getGmlId(polygon),
                XmlUtil.getGmlId(errorLineString.getLineStringError()),
                errorElementNodeName,
                error
        ));
      }

      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
              msg,
              gmlElementErrors));
    });

    return messages;
  }

  private void checkPointsIntersect(Node lineStringNode, Map<Node, Set<ErrorLineString>> buildingWithErrorLineString) {
    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(lineStringNode, TagName.BLDG_BUILDING, TagName.GML_ID);

    List<LineSegment> lineSegments = getLineSegments(lineStringNode);

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
          // Line last and first are also continuous (if last point equals first point)
          boolean isReverseContinuous = second.p1.equals(first.p0) && (j == 0 && k == lineSegments.size() - 1);

          int validCode = 0;

          if (isContinuous) {
            if (!first.p1.equals(second.p0)) {
              validCode = CONTINUOUS_ERROR_CODE;
            }
          } else if (isReverseContinuous) {
            if (!second.p1.equals(first.p0)) {
              validCode = CONTINUOUS_ERROR_CODE;
            }
          } else {
            // 2 line segments should not have no intersection
            var relationship = ThreeDUtil.checkSegmentsRelationship(
                new Vector3D(first.p0.x, first.p0.y, first.p0.z), new Vector3D(first.p1.x, first.p1.y, first.p1.z),
                new Vector3D(second.p0.x, second.p0.y, second.p0.z), new Vector3D(second.p1.x, second.p1.y, second.p1.z)
            );
            if (relationship == SegmentRelationship.INTERSECT || relationship == SegmentRelationship.OVERLAP) {
              validCode = INTERSECTION_ERROR_CODE;
            } else if (relationship == SegmentRelationship.TOUCH) {
              validCode = TOUCH_ERROR_CODE;
            }
          }

          // If 2 line segments are not continuous and intersected
          if (validCode > 0) {
            logger.severe(String.format("L08 Line have (%s and %s) is not valid", first, second));
            // Update error list of building
            ErrorLineString errorLineString = new ErrorLineString();
            Set<ErrorLineString> errorList = buildingWithErrorLineString.getOrDefault(buildingNode, new HashSet<>());
            errorLineString.setLineStringError(lineStringNode);
            errorLineString.setErrorCode(validCode);
            errorList.add(errorLineString);
            buildingWithErrorLineString.put(buildingNode, errorList);
          }
        }
      }
    }
  }

  private boolean checkTouchLineSegments(LineSegment first, LineSegment second) {
    double vectorSecondP0 = (second.p0.x - first.p0.x)*(first.p1.x - first.p0.x) +
            (second.p0.y - first.p0.y)*(first.p1.y - first.p0.y) +
            (second.p0.z - first.p0.z)*(first.p1.z - first.p0.z);

    double vectorSecondP1 = (second.p1.x - first.p0.x)*(first.p1.x - first.p0.x) +
            (second.p1.y - first.p0.y)*(first.p1.y - first.p0.y) +
            (second.p1.z - first.p0.z)*(first.p1.z - first.p0.z);

    if (vectorSecondP0 == 0 || vectorSecondP1 == 0) {
      return true;
    }

    return false;
  }

  private List<LineSegment> getLineSegments(Node lineStringNode) {

    Element lineStringElement = (Element) lineStringNode;
    String[] posString = lineStringElement.getElementsByTagName(TagName.GML_POSLIST).item(0).getTextContent().split(" ");
    // Convert string to 3d points
    List<Point3D> points = ThreeDUtil.createListPoint(posString);;
    // Convert 3d points to line segments
    return ThreeDUtil.getLineSegments(points);
  }
}


class ErrorLineString {
  public ErrorLineString() {}

  private Node LineStringError;
  private int errorCode;

  public void setErrorCode(int errorCode) {
    this.errorCode = errorCode;
  }

  public int getErrorCode() {
    return this.errorCode;
  }

  public void setLineStringError (Node lineStringError) {
    this.LineStringError = lineStringError;
  }

  public Node getLineStringError() {
    return this.LineStringError;
  }
}