package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.LineSegment;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
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
        Node gmlId = XmlUtil.findNearestParentByAttribute(lineStringNodes.item(i), TagName.GML_ID);
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                MessageFormat.format(MessageError.ERR_L08_002,
                        gmlId.getAttributes().getNamedItem("gml:id").getTextContent(),
                        lineStringNodes.item(i).getFirstChild().getNodeValue())));
      }
    }

    buildingWithErrorLineString.forEach((buildingNode, lineStringNodesWithError) -> {
      String gmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      String msg = MessageFormat.format(MessageError.ERR_L08_001, gmlId);
      for (ErrorLineString errorLineString : lineStringNodesWithError) {
        if (errorLineString.getErrorCode() == INTERSECTION_ERROR_CODE) {
          msg = msg + String.format("<gml:LineString gml:id=\"%s\">が自己交差しています。\n",
                          errorLineString.getLineStringError().getAttributes().getNamedItem(TagName.GML_ID).getTextContent());
        } else if (errorLineString.getErrorCode() == TOUCH_ERROR_CODE) {
          msg = msg + String.format("<gml:LineString gml:id=\"%s\">が自己接触しています。\n",
                  errorLineString.getLineStringError().getAttributes().getNamedItem(TagName.GML_ID).getTextContent());
        } else if (errorLineString.getErrorCode() == CONTINUOUS_ERROR_CODE) {
          msg = msg + "\n" + errorLineString.getLineStringError().getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
        }
      }

//      String.format("L08: Building which gml:id=\"%s\" has %s <gml:LineString> invalid", gmlId, lineStringNodesWithError.size());
      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, msg));
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
          // Line first and last are also continuous
          boolean isReverseContinuous = j == 0 && k == lineSegments.size() - 1;

          int validCode = 0;

          if (isContinuous) {
            validCode = ThreeDUtil.isLinesContinuous(first, second) ? CONTINUOUS_ERROR_CODE : validCode;
          } else if (isReverseContinuous) {
            validCode = ThreeDUtil.isLinesContinuous(second, first) ? CONTINUOUS_ERROR_CODE : validCode;
          } else {
            // 2 line segments should not have no intersection
            validCode = first.intersection(second) == null ? INTERSECTION_ERROR_CODE : validCode;
            if (checkTouchLineSegments(first, second)) {
              validCode = first.intersection(second) == null ? TOUCH_ERROR_CODE : validCode;
            }
          }

          // If 2 line segments are not continuous and intersected
          if (validCode > 0) {
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