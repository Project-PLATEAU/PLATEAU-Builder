package org.plateau.citygmleditor.validation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.geometry.Point3D;
import javax.xml.parsers.ParserConfigurationException;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.PolygonMeshUtils;
import org.plateau.citygmleditor.utils3d.polygonmesh.Tessellator;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import org.plateau.citygmleditor.validation.exception.InvalidGmlStructureException;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class L13LogicalConsistencyValidator implements IValidator {

  public static Logger logger = Logger.getLogger(L13LogicalConsistencyValidator.class.getName());

  @Override
  public List<ValidationResultMessage> validate(CityModelView cityModel)
      throws ParserConfigurationException, IOException, SAXException {

    List<ValidationResultMessage> messages = new ArrayList<>();
    Map<Node, Set<Node>> buildingWithErrorPolygonsSurfacePaths = new HashMap<>();

    var polygons = CityGmlUtil.getAllTagFromCityModel(cityModel, TagName.GML_POLYGON);

    // Check if exterior and interior of each polygon is invalid
    for (int i = 0; i < polygons.getLength(); i++) {
      validateExteriorAndInteriors(polygons.item(i), buildingWithErrorPolygonsSurfacePaths);
    }

    buildingWithErrorPolygonsSurfacePaths.forEach((buildingNode, errorPolygonSurfacePath) -> {
      String buildingGmlId = buildingNode.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      errorPolygonSurfacePath.forEach(polygonSurfacePath -> {
        String gmlId = polygonSurfacePath.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_L13_001, buildingGmlId, gmlId)));
      });
    });

    return messages;
  }

  private void validateExteriorAndInteriors(Node polygon, Map<Node, Set<Node>> buildingWithErrorLinearRing) {

    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(polygon, TagName.BLDG_BUILDING, TagName.GML_ID);

    Polygon exterior;
    List<Polygon> interiors;

    try {
      exterior = getExterior(polygon);
      interiors = getInteriors(polygon);
    } catch (InvalidGmlStructureException | InvalidPosStringException e) {
      logger.severe(e.getMessage());
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, polygon);
      return;
    }

    // Validate Standard 1: に内周が存在し、以下の条件のいずれかに合致する場合、エラーとする
    var invalidStandard1 = interiors.stream().anyMatch(interior -> interior.crosses(exterior));

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


  public Polygon getExterior(Node polygon) {
    var exteriorList = new ArrayList<Node>();

    XmlUtil.recursiveFindNodeByTagName(polygon, exteriorList, TagName.GML_EXTERIOR);

    // Each polygon should have exactly 1 exterior and 1 or more interiors
    if (exteriorList.size() != 1) {
      throw new InvalidGmlStructureException("Polygon should have exactly 1 exterior");
    }

    var exterior = exteriorList.get(0);

    return linearRingToPolygon(exterior.getFirstChild().getNextSibling());
  }

  public List<Polygon> getInteriors(Node polygon) {

    var interiorList = new ArrayList<Node>();

    XmlUtil.recursiveFindNodeByTagName(polygon, interiorList, TagName.GML_INTERIOR);

    return interiorList.stream()
        .map(p-> linearRingToPolygon(p.getFirstChild().getNextSibling()))
        .collect(Collectors.toList());
  }

  /**
   * Return Polygon which created by LinearRing
   * @param linearRingNode LinearRing node
   * @return Polygon which created by LinearRing
   */
  private Polygon linearRingToPolygon(Node linearRingNode) {

    var points = get3dPoints(linearRingNode).stream()
        .map(point -> new Coordinate(point.getX(), point.getY(), point.getZ()))
        .toArray(Coordinate[]::new);

    return new Polygon(new LinearRing(new CoordinateArraySequence(points), new GeometryFactory()), null, new GeometryFactory());
  }

  /**
   * Get list of 3d points from LinearRing node
   * @param linearRingNode LinearRing node
   * @return list of 3d points
   */
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
    return List.of();
  }
}
