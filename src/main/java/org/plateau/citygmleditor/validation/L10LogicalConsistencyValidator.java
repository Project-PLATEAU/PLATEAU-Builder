package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.apache.commons.lang3.ObjectUtils;
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
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;


public class L10LogicalConsistencyValidator implements IValidator {
  private final static int ERROR_L10 = 1;
  private final static int INVALID_FORMAT_EXCEPTION = 2;

  @Override
  public List<ValidationResultMessage> validate(CityModelView cityModel)
      throws ParserConfigurationException, IOException, SAXException {

    List<ValidationResultMessage> messages = new ArrayList<>();
    Map<Node, Set<Node>> buildingWithErrorPolygonsSurfacePaths = new HashMap<>();

    var rootDocument = CityGmlUtil.getXmlDocumentFrom(cityModel);

    var polygons = rootDocument.getElementsByTagName(TagName.GML_POLYGON);
    var surfacePaths = rootDocument.getElementsByTagName(TagName.GML_SURFACE_PATCH);

    // Check if exterior and interior of each polygon is invalid
    for (int i = 0; i < polygons.getLength(); i++) {
      validateExteriorAndInteriors(polygons.item(i), buildingWithErrorPolygonsSurfacePaths, messages);
    }

    // Check if exterior and interior of each surfacePath is invalid
    for (int i = 0; i < surfacePaths.getLength(); i++) {
      validateExteriorAndInteriors(surfacePaths.item(i), buildingWithErrorPolygonsSurfacePaths, messages);
    }

    buildingWithErrorPolygonsSurfacePaths.forEach((buildingNode, errorPolygonSurfacePath) -> {
      String buildingGmlId = XmlUtil.getGmlId(buildingNode);

      Set<Node> polygonErrors = buildingWithErrorPolygonsSurfacePaths.get(buildingNode);

      String polygonErrorStr = "";
      List<GmlElementError> gmlElementErrors = new ArrayList<>();
      for (Node polygon : polygonErrors) {
        String errorElementNodeName = polygon.getFirstChild().getNodeName();
        int error = ERROR_L10;

        GmlElementError gmlElementError = new GmlElementError(
                buildingGmlId,
                null,
                XmlUtil.getGmlId(polygon),
                XmlUtil.getGmlId(polygon),
                errorElementNodeName,
                error);
        gmlElementErrors.add(gmlElementError);

        var itemGmlId = polygon.getAttributes().getNamedItem(TagName.GML_ID);
        String gmiId = itemGmlId != null ? itemGmlId.getTextContent() : "";
        polygonErrorStr = polygonErrorStr + MessageFormat.format(MessageError.ERR_L10_002_1, gmiId);
      }

      messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
              MessageFormat.format(MessageError.ERR_L10_002, buildingGmlId, polygonErrorStr),
              gmlElementErrors));
    });

    return messages;
  }

  private void validateExteriorAndInteriors(Node polygonOrSurfacePath, Map<Node, Set<Node>> buildingWithErrorLinearRing,
                                            List<ValidationResultMessage> messages) {

    Node buildingNode = XmlUtil.findNearestParentByTagAndAttribute(polygonOrSurfacePath, TagName.BLDG_BUILDING,
        TagName.GML_ID);

    var exteriorList = new ArrayList<Node>();
    var interiorList = new ArrayList<Node>();

    XmlUtil.recursiveFindNodeByTagName(polygonOrSurfacePath, exteriorList, TagName.GML_EXTERIOR);
    XmlUtil.recursiveFindNodeByTagName(polygonOrSurfacePath, interiorList, TagName.GML_INTERIOR);

    // Each polygon or surfacePath should have only 1 exterior and 1 or more interiors

    List<Point3D> exteriorPoints;
    List<List<Point3D>> interiorListPoints;

    try {
      exteriorPoints = get3dPoints(exteriorList.get(0).getFirstChild().getNextSibling(), messages);
      interiorListPoints = interiorList.stream()
          .map(node -> get3dPoints(node.getFirstChild().getNextSibling(), messages))
          .collect(Collectors.toList());
    } catch (InvalidPosStringException e) {
      CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, polygonOrSurfacePath);
      return;
    }

    // Get normal vector of exterior
    var exteriorNormal = getNormalVector(exteriorPoints);

    // Get normal vector of each interior
    for (var interiorPoints : interiorListPoints) {
      var interiorNormal = getNormalVector(interiorPoints);
      // If angle between exterior normal and interior normal is 180, it means interior and exterior is reverse direction
      // In this case, it is valid, otherwise, it is invalid
      // 座標列の向きが不正なインスタンスをエラーとする。外周は反時計回り、内周は時計回りが正しい。
      if (!ObjectUtils.isEmpty(exteriorNormal) && exteriorNormal.angle(interiorNormal) != 180) {
        // Update error list of building and exit function
        CollectionUtil.updateErrorMap(buildingWithErrorLinearRing, buildingNode, polygonOrSurfacePath);
        return;
      }
    }
  }

  protected Point3D getNormalVector(List<Point3D> ringPoints) {
    // Remove duplicate points
    LinkedHashSet<Point3D> points = new LinkedHashSet<>(ringPoints);

    var ringVertexBuffer = new VertexBuffer();

    // The contour points are converted to coordinates in the world and registered. The end point is deleted because the start point and end point overlap.
    for (var point : points) {
      var geoCoordinate = new GeoCoordinate(point.getX(), point.getY(), point.getZ());
      var position = World.getActiveInstance().getGeoReference().project(geoCoordinate);
      ringVertexBuffer.addVertex(position);
    }

    // Tessellate the contour.
    var meshVertexBuffer = new VertexBuffer();
    var meshFaceBuffer = new FaceBuffer();
    Tessellator.tessellate(ringVertexBuffer, null, meshVertexBuffer, meshFaceBuffer);

    // Calculate normals
    var normals = PolygonMeshUtils.calculateNormal(meshVertexBuffer, meshFaceBuffer);

    if (ObjectUtils.isEmpty(normals)) {
      return null;
    }
    // Print normal(normals should be almost the same)
    return new Point3D(normals[0], normals[1], normals[2]).normalize();
  }

  private List<Point3D> get3dPoints(Node linearRingNode, List<ValidationResultMessage> messages) {
    Element linearRingElement = (Element) linearRingNode;
    // There are 2 cases: posList and pos of LinearRing
    NodeList posListNodes = linearRingElement.getElementsByTagName(TagName.GML_POSLIST);
    NodeList posNodes = linearRingElement.getElementsByTagName(TagName.GML_POS);
    Node building = XmlUtil.findNearestParentByTagAndAttribute(linearRingNode, TagName.BLDG_BUILDING, TagName.GML_ID);
    Node polygon = XmlUtil.findNearestParentByName(linearRingNode, TagName.GML_POLYGON);

    if (posListNodes.getLength() > 0) {
      String[] posString = posListNodes.item(0).getTextContent().split(" ");
      try {
        return ThreeDUtil.createListPoint(posString);
      } catch (InvalidPosStringException e) {

        String errorElementNodeName = linearRingNode.getFirstChild().getNodeName();
        int error = INVALID_FORMAT_EXCEPTION;
        GmlElementError gmlElementError = new GmlElementError(
                XmlUtil.getGmlId(building), null, XmlUtil.getGmlId(polygon), XmlUtil.getGmlId(linearRingNode), errorElementNodeName, error
        );

        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                MessageFormat.format(MessageError.ERR_L10_001,
                        XmlUtil.getGmlId(building),
                        linearRingElement.getFirstChild().getNodeValue()),
                List.of(gmlElementError)));
        return new ArrayList<>();
      }
    } else if (posNodes.getLength() > 0) {
      List<Point3D> point3DS = new ArrayList<>();
      for (int i = 0; i < posNodes.getLength(); i++) {
        Node posNode = posNodes.item(i);
        String[] posString = posNode.getTextContent().split(" ");
        String buildingId = XmlUtil.getGmlId(building);
        try {
          point3DS.addAll(ThreeDUtil.createListPoint(posString));
        } catch (InvalidPosStringException e) {
          int error = INVALID_FORMAT_EXCEPTION;
          GmlElementError gmlElementError = new GmlElementError(
                  buildingId,
                  null,
                  XmlUtil.getGmlId(polygon),
                  XmlUtil.getGmlId(linearRingNode),
                  linearRingNode.getNodeName(),
                  error
          );

          messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                  MessageFormat.format(MessageError.ERR_L10_003,
                          buildingId,
                          linearRingElement.getFirstChild().getNodeValue()),
                  List.of(gmlElementError)));

          return new ArrayList<>();
        }
      }
      return point3DS;
    }
    return new ArrayList<>();
  }
}
