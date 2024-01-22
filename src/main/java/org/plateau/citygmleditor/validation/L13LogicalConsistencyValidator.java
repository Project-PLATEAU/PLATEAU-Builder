package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import javax.xml.parsers.ParserConfigurationException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.validation.exception.InvalidGmlStructureException;
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
import java.util.stream.Collectors;


public class L13LogicalConsistencyValidator implements IValidator {

  public static Logger logger = Logger.getLogger(L13LogicalConsistencyValidator.class.getName());

  @Override
  public List<ValidationResultMessage> validate(CityModelView cityModel)
      throws ParserConfigurationException, IOException, SAXException {

    List<ValidationResultMessage> messages = new ArrayList<>();
    Map<Node, Set<Node>> buildingWithErrorPolygonsSurfacePaths = new HashMap<>();

    var polygons = CityGmlUtil.getXmlDocumentFrom(cityModel).getElementsByTagName(TagName.GML_POLYGON);

    // Check if exterior and interior of each polygon is invalid
    for (int i = 0; i < polygons.getLength(); i++) {
      validateExteriorAndInteriors(polygons.item(i), buildingWithErrorPolygonsSurfacePaths, messages);
    }

    buildingWithErrorPolygonsSurfacePaths.forEach((buildingNode, errorPolygonSurfacePath) -> {
      String buildingGmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();

      var nodeErrorDetail = errorPolygonSurfacePath.stream()
          .map(node -> {
            Node gmlIdNode = node.getAttributes().getNamedItem(TagName.GML_ID);

            var gmlId = gmlIdNode != null ? gmlIdNode.getTextContent() : "";
            return String.format("%s gml:id=\"%s\"", node.getNodeName(), gmlId);
          }).collect(Collectors.joining(", "));

      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_L13_001, buildingGmlId, nodeErrorDetail)));
    });

    return messages;
  }

  private void validateExteriorAndInteriors(Node polygon, Map<Node, Set<Node>> buildingWithErrorLinearRing,
                                            List<ValidationResultMessage> messages) {

    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(polygon, TagName.BLDG_BUILDING, TagName.GML_ID);

    Polygon exterior;
    List<Polygon> interiors;

    try {
      exterior = getExterior(polygon, messages);
      interiors = getInteriors(polygon, messages);
    } catch (InvalidGmlStructureException | InvalidPosStringException e) {
      logger.severe(e.getMessage());
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, polygon);
      return;
    }

    // Validate Standard 1: に内周が存在し、以下の条件のいずれかに合致する場合、エラーとする
    // All interiors should be within exterior
    var invalidStandard1 = interiors.stream().anyMatch(interior -> !interior.within(exterior));

    // Validate Standard 3: 内周同士が重なる、または包含関係にある。
    var invalidStandard3 = !isValidStandard3(interiors);

    // Validate Standard 2: 内周と外周が接し、gml:Polygonが2つ以上に分割されている
    // Union all interiors
    var interiorsUnionResult = new GeometryFactory()
        .createGeometryCollection(interiors.toArray(new Polygon[interiors.size()]))
        .union();

    // Get difference between exterior and union of interiors
    // It means get the result which is the part of exterior that is not in union of interiors
    // If interiors split exterior into multiple polygons, then the polygon is invalid
    var invalidStandard2 = exterior.difference(interiorsUnionResult) instanceof MultiPolygon;

    if (invalidStandard1 || invalidStandard2 || invalidStandard3) {
      // Update error list of building and exit function
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, polygon);
    }

  }

  // Validate Standard 3: 内周同士が重なる、または包含関係にある。
  private static boolean isValidStandard3(List<Polygon> interiors) {
    for (int i = 0; i < interiors.size(); i++) {
      for (int j = i + 1; j < interiors.size(); j++) {
        var interior1 = interiors.get(i);
        var interior2 = interiors.get(j);
        if (interior1.covers(interior2)
            || interior1.overlaps(interior2)
            || interior2.covers(interior1)
            || interior2.overlaps(interior1)) {
          return false;
        }
      }
    }
    return true;
  }


  public Polygon getExterior(Node polygon, List<ValidationResultMessage> messages) {
    var exteriorList = new ArrayList<Node>();

    XmlUtil.recursiveFindNodeByTagName(polygon, exteriorList, TagName.GML_EXTERIOR);

    // Each polygon should have exactly 1 exterior and 1 or more interiors
    if (exteriorList.size() != 1) {
      throw new InvalidGmlStructureException("Polygon should have exactly 1 exterior");
    }

    var exterior = exteriorList.get(0);

    return linearRingToPolygon(exterior.getFirstChild().getNextSibling(), messages);
  }

  public List<Polygon> getInteriors(Node polygon, List<ValidationResultMessage> messages) {

    var interiorList = new ArrayList<Node>();

    XmlUtil.recursiveFindNodeByTagName(polygon, interiorList, TagName.GML_INTERIOR);

    return interiorList.stream()
        .map(p-> linearRingToPolygon(p.getFirstChild().getNextSibling(), messages))
        .collect(Collectors.toList());
  }

  /**
   * Return Polygon which created by LinearRing
   * @param linearRingNode LinearRing node
   * @return Polygon which created by LinearRing
   */
  private Polygon linearRingToPolygon(Node linearRingNode, List<ValidationResultMessage> messages) {

    var points = get3dPoints(linearRingNode, messages).stream()
        .map(point -> new Coordinate(point.getX(), point.getY(), point.getZ()))
        .toArray(Coordinate[]::new);

    try {
      return new Polygon(new LinearRing(new CoordinateArraySequence(points), new GeometryFactory()), null, new GeometryFactory());
    } catch (IllegalArgumentException e) {
      logger.severe("Got error when convert linear ring to polygon " + e.getMessage());
      throw new InvalidPosStringException(e);
    }
  }

  /**
   * Get list of 3d points from LinearRing node
   * @param linearRingNode LinearRing node
   * @return list of 3d points
   */
  private List<Point3D> get3dPoints(Node linearRingNode, List<ValidationResultMessage> messages) {
    Element linearRingElement = (Element) linearRingNode;
    // There are 2 cases: posList and pos of LinearRing
    NodeList posListNodes = linearRingElement.getElementsByTagName(TagName.GML_POSLIST);
    NodeList posNodes = linearRingElement.getElementsByTagName(TagName.GML_POS);

    if (posListNodes.getLength() > 0) {
      String[] posString = posListNodes.item(0).getTextContent().split(" ");
      try {
        return ThreeDUtil.createListPoint(posString);
      } catch (InvalidPosStringException e) {
        Node parentNode = XmlUtil.findNearestParentByAttribute(linearRingNode, TagName.GML_ID);
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                MessageFormat.format(MessageError.ERR_L13_002,
                        parentNode.getAttributes().getNamedItem("gml:id").getTextContent(),
                        linearRingNode.getFirstChild().getNodeValue())));
        return new ArrayList<>();
      }
    } else if (posNodes.getLength() > 0) {
      List<Point3D> point3DS = new ArrayList<>();
      for (int i = 0; i < posNodes.getLength(); i++) {
        Node posNode = posNodes.item(i);
        String[] posString = posNode.getTextContent().split(" ");
        try {
          point3DS.addAll(ThreeDUtil.createListPoint(posString));
        } catch (InvalidPosStringException e) {
          Node parentNode = XmlUtil.findNearestParentByAttribute(posNode, TagName.GML_ID);
          messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                  MessageFormat.format(MessageError.ERR_L13_002,
                          parentNode.getAttributes().getNamedItem("gml:id").getTextContent(),
                          posNode.getFirstChild().getNodeValue())));
          return new ArrayList<>();
        }
      }
      return point3DS;
    }
    return new ArrayList<>();
  }
}
