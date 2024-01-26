package org.plateau.citygmleditor.validation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.geometry.Point3D;
import org.locationtech.jts.geom.*;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Lbldg01LogicalAccuracyValidator implements IValidator {

  enum ErrorType {
    ERR_INVALID_FORMAT_POINTS, ERR_SOLID_INTERSECT
  }

  @Override
  public List<ValidationResultMessage> validate(CityModelView cityModel)
      throws ParserConfigurationException, IOException, SAXException {

    List<Node> buildings = XmlUtil.findAllNodeByTag(CityGmlUtil.getXmlDocumentFrom(cityModel),
        TagName.BLDG_BUILDING);

    Map<Node, Map<Node, ErrorType>> buildingWithErrorSolids = new HashMap<>();

    for (var building : buildings) {
      // Loop through all buildings
      validateBuildingSolids(building, buildingWithErrorSolids);
    }

    if (buildingWithErrorSolids.isEmpty()) {
      return List.of();
    }

    StringBuffer finalErrorMsg = new StringBuffer("\n")
        .append(MessageError.ERR_LBLDG_01_PREFIX)
        .append("\n");

    buildingWithErrorSolids.forEach((building, errorNodes) -> {
      String buildingGmlId = building.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      var invalidPolygonTxt = new ArrayList<String>();
      var invalidSolidTxt = new ArrayList<String>();

      errorNodes.forEach((key, value) -> {
        var gmlIdNode = key.getAttributes().getNamedItem(TagName.GML_ID);
        var gmlId = gmlIdNode != null ? gmlIdNode.getTextContent() : "";
        if (value == ErrorType.ERR_INVALID_FORMAT_POINTS) {
          invalidPolygonTxt.add(String.format(MessageError.ERR_LBLDG_01_POLYGON_DETAIL, gmlId));
        } else if (value == ErrorType.ERR_SOLID_INTERSECT) {
          invalidSolidTxt.add(String.format(MessageError.ERR_LBLDG_01_SOLID_DETAIL, gmlId));
        }
      });

      String invalidFormatMsg = invalidPolygonTxt.isEmpty() ? "" : String.format(MessageError.ERR_LBLDG_01_POLYGON, String.join("\n", invalidPolygonTxt));
      String intersectFormatMsg = invalidSolidTxt.isEmpty() ? "" : String.format(MessageError.ERR_LBLDG_01_SOLID, String.join("\n", invalidSolidTxt));

      var buildingErrorMsg = Stream.of(String.format(MessageError.ERR_LBLDG_01_BUILDING, buildingGmlId), invalidFormatMsg, intersectFormatMsg)
          .filter(s -> !s.isEmpty())
          .collect(Collectors.joining("\n"));
      finalErrorMsg.append(buildingErrorMsg).append("\n");
    });

    var message = new ValidationResultMessage(ValidationResultMessageType.Error,
        finalErrorMsg.toString());
    return List.of(message);
  }

  private void validateBuildingSolids(Node building,
      Map<Node, Map<Node, ErrorType>> buildingWithErrorSolids) {
    // Find all lod1Solids of building
    List<Node> lod1Solids = XmlUtil.findAllNodeByTag(building, TagName.BLDG_LOD_1_SOLID);
    // Find all lod2Solids of building
    List<Node> lod2Solids = XmlUtil.findAllNodeByTag(building, TagName.BLDG_LOD_2_SOLID);

    List<Node> solids = new ArrayList<>();

    // push all solids to one list
    lod1Solids.forEach(solid -> solids.addAll(XmlUtil.findAllNodeByTag(solid, TagName.GML_SOLID)));
    lod2Solids.forEach(solid -> solids.addAll(XmlUtil.findAllNodeByTag(solid, TagName.GML_SOLID)));

    // With each combination of two solids, compute their projection of their solids on 2D and check intersection
    for (int i = 0; i < solids.size(); i++) {
      Node solid1 = solids.get(i);
      Geometry projectedOnOxyPlane1;
      try {
        projectedOnOxyPlane1 = getSolidExteriorProjectedOnOxyPlane(building, solid1,
            buildingWithErrorSolids);
      } catch (IllegalArgumentException | InvalidPosStringException e) {
        continue;
      }
      for (int j = i + 1; j < solids.size(); j++) {
        Node solid2 = solids.get(j);
        Geometry projectedOnOxyPlane2;
        try {
          projectedOnOxyPlane2 = getSolidExteriorProjectedOnOxyPlane(building, solid2,
              buildingWithErrorSolids);
        } catch (IllegalArgumentException | InvalidPosStringException e) {
          continue;
        }

        // Check intersection
        // If intersection is a polygon, then two solids intersect
        // Otherwise, two solids do not intersect (perhaps touch or no touch)
        if ((projectedOnOxyPlane1.intersection(projectedOnOxyPlane2)) instanceof Polygon) {
          CollectionUtil.updateErrorMap(buildingWithErrorSolids, building, solid1, ErrorType.ERR_SOLID_INTERSECT);
          CollectionUtil.updateErrorMap(buildingWithErrorSolids, building, solid2, ErrorType.ERR_SOLID_INTERSECT);
        }
      }
    }
  }

  /**
   * Get result of union the exterior polygons of a solid and project them on Oxy plane
   *
   * @param solid solid node
   * @return Get result of union the exterior polygons of a solid and project them on Oxy plane
   */
  Geometry getSolidExteriorProjectedOnOxyPlane(Node building, Node solid,
      Map<Node, Map<Node, ErrorType>> buildingWithErrorSolids) {
    boolean isError = false;
    List<Geometry> results = new ArrayList<>();
    // Find list of solid's exteriors
    List<Node> solidExteriors = XmlUtil.findAllNodeByTag(solid, TagName.GML_EXTERIOR);
    for (Node solidExterior : solidExteriors) {
      // Find list of exterior's polygons
      List<Node> polygons = XmlUtil.findAllNodeByTag(solidExterior, TagName.GML_POLYGON);
      for (Node polygon : polygons) {
        // Find list of polygon's exterior
        List<Node> exteriors = XmlUtil.findAllNodeByTag(polygon, TagName.GML_EXTERIOR);
        for (Node exterior : exteriors) {
          // Find list of exterior's linear rings
          List<Node> linearRings = XmlUtil.findAllNodeByTag(exterior, TagName.GML_LINEARRING);
          for (Node linearRing : linearRings) {
            // Project linear ring on Oxy plane (convert z = 0)
            var pointOnOxyPlane = ThreeDUtil.get3dPoints(linearRing).stream()
                .map(p -> new Point3D(p.getX(), p.getY(), 0)).collect(Collectors.toList());

            try {
              results.add(ThreeDUtil.createPolygon(pointOnOxyPlane));
            } catch (IllegalArgumentException e) {
              CollectionUtil.updateErrorMap(buildingWithErrorSolids, building, polygon,
                  ErrorType.ERR_INVALID_FORMAT_POINTS);
              isError = true;
            }
          }
        }
      }
    }

    if (isError) {
      String buildingGmlId = building.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();
      throw new IllegalArgumentException(
          String.format("There are some polygons of building %s are invalid points",
              buildingGmlId));
    }
    // Union all exterior polygons
    return new GeometryFactory().createGeometryCollection(
        results.toArray(new Polygon[results.size()])).union();
  }
}
