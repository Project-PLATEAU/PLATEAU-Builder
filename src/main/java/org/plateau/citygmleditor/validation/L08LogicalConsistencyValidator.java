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

import javax.xml.parsers.ParserConfigurationException;
import org.citygml4j.model.citygml.core.CityModel;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class L08LogicalConsistencyValidator implements IValidator {

  public static Logger logger = Logger.getLogger(L08LogicalConsistencyValidator.class.getName());

  public List<ValidationResultMessage> validate(CityModel cityModel)
      throws ParserConfigurationException, IOException, SAXException {
    List<ValidationResultMessage> messages = new ArrayList<>();

    File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
    NodeList lineStringNodes = XmlUtil.getAllTagFromXmlFile(file, TagName.GML_LINESTRING);
    Map<Node, Set<Node>> buildingWithErrorLineString = new HashMap<>();

    for (int i = 0; i < lineStringNodes.getLength(); i++) {
      checkPointsIntersect(lineStringNodes.item(i), buildingWithErrorLineString);
    }

    buildingWithErrorLineString.forEach((buildingNode, lineStringNodesWithError) -> {
      String gmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      var msg = String.format("L08: Building which gml:id=\"%s\" has %s <gml:LineString> invalid", gmlId, lineStringNodesWithError.size());
      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, msg));
    });

    return messages;
  }

  private void checkPointsIntersect(Node lineStringNode, Map<Node, Set<Node>> buildingWithErrorLineString) {
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
          boolean isContinuous = j == k-1 && ThreeDUtil.isLinesContinuous(first, second);
          boolean isIntersected = ThreeDUtil.isLineIntersect(first, second);

          // If 2 line segments are not continuous and intersected
          if (!isContinuous && isIntersected) {
            // Update error list of building
            Set<Node> errorList = buildingWithErrorLineString.getOrDefault(buildingNode, new HashSet<>());
            errorList.add(lineStringNode);
            buildingWithErrorLineString.put(buildingNode, errorList);
          }
        }
      }
    }
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
