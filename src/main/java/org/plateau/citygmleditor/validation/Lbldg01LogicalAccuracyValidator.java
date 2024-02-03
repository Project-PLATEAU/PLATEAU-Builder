package org.plateau.citygmleditor.validation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.geometry.Point3D;
import javax.xml.parsers.ParserConfigurationException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
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

public class Lbldg01LogicalAccuracyValidator implements IValidator {

  private static final String BUILDING_ID = "buildingId";

  interface Lbldg01ErrorType {
    int ERR_INVALID_FORMAT_POINTS = 0;
    int ERR_SOLID_INTERSECT = 1;
  }

  /**
   * Find all solids in lodTagName (lod1Solid or lod2Solid) in buildings
   * @param buildings list of building nodes
   * @param lodTagName TagName.BLDG_LOD_1_SOLID or TagName.BLDG_LOD_2_SOLID
   * @return list of solid nodes
   */
  private List<Node> getAllSolidsInLodSolidInBuildings(List<Node> buildings, String lodTagName) {
    List<Node> solids = new ArrayList<>();
    for (var building : buildings) {
      String buildingGmlId = building.getAttributes().getNamedItem(TagName.GML_ID).getTextContent();

      List<Node> lodSolids = XmlUtil.findAllNodeByTag(building, lodTagName);
      for (var solid : lodSolids) {
        XmlUtil.findAllNodeByTag(solid, TagName.GML_SOLID).stream()
            .peek(n -> n.setUserData(BUILDING_ID, buildingGmlId, null))
            .forEach(solids::add);
      }
    }
    return solids;
  }


  @Override
  public List<ValidationResultMessage> validate(CityModelView cityModel)
      throws ParserConfigurationException, IOException, SAXException {

    List<Node> buildings = XmlUtil.findAllNodeByTag(CityGmlUtil.getXmlDocumentFrom(cityModel),
        TagName.BLDG_BUILDING);


    List<ValidationResultMessage> validationResults = new ArrayList<>();
    Set<GmlElementError> elementErrors = new HashSet<>();

    validateBuildingSolids(buildings, elementErrors);

    elementErrors.stream()
        .collect(Collectors.groupingBy(GmlElementError::getBuildingId))
        .forEach((buildingId, buildingElementErrors) -> {
            var invalidPolygonTxt = new ArrayList<String>();
            var invalidSolidTxt = new ArrayList<String>();

            buildingElementErrors.forEach(error -> {
              if (error.getError() == Lbldg01ErrorType.ERR_INVALID_FORMAT_POINTS) {
                invalidPolygonTxt.add(String.format(MessageError.ERR_LBLDG_01_POLYGON_DETAIL, error.getErrorElementId()));
              } else if (error.getError() == Lbldg01ErrorType.ERR_SOLID_INTERSECT) {
                invalidSolidTxt.add(String.format(MessageError.ERR_LBLDG_01_SOLID_DETAIL, error.getErrorElementId()));
              }
            });

            if (!invalidPolygonTxt.isEmpty()) {
              var polygonsError = String.format(MessageError.ERR_LBLDG_01_POLYGON, String.join("\n", invalidPolygonTxt));
              var buildingWarningMsg = String.format(MessageError.ERR_LBLDG_01_BUILDING, buildingId, polygonsError);
              validationResults.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingWarningMsg, buildingElementErrors));
            }

            if (!invalidSolidTxt.isEmpty()) {
              var solidsError = String.format(MessageError.ERR_LBLDG_01_SOLID, String.join("\n", invalidSolidTxt));
              var buildingWarningMsg = String.format(MessageError.ERR_LBLDG_01_BUILDING, buildingId, solidsError);
              validationResults.add(new ValidationResultMessage(ValidationResultMessageType.Warning, buildingWarningMsg, buildingElementErrors));
            }
        });


    return validationResults;
  }

  private void validateBuildingSolids(List<Node> buildings, Set<GmlElementError> elementErrors) {
    // Find all lod1Solids of buildings
    List<Node> lod1Solids = getAllSolidsInLodSolidInBuildings(buildings, TagName.BLDG_LOD_1_SOLID);
    // Find all lod2Solids of buildings
    List<Node> lod2Solids = getAllSolidsInLodSolidInBuildings(buildings, TagName.BLDG_LOD_2_SOLID);

    checkSolidIntersect(lod1Solids, elementErrors);
    checkSolidIntersect(lod2Solids, elementErrors);
  }

  private void checkSolidIntersect(List<Node> solids, Set<GmlElementError> elementErrors) {
    for (int i = 0; i < solids.size(); i++) {
      Node solid1 = solids.get(i);
      for (int j = i + 1; j < solids.size(); j++) {
        Node solid2 = solids.get(j);
        if (isSolidIntersect(solid1, solid2, elementErrors)) {
          String building1Id = XmlUtil.getUserDataAttribute(solid1, BUILDING_ID, String.class);
          String solid1Id = XmlUtil.getAttribute(solid1, TagName.GML_ID);

          String building2Id = XmlUtil.getUserDataAttribute(solid2, BUILDING_ID, String.class);
          String solid2Id = XmlUtil.getAttribute(solid2, TagName.GML_ID);
          var error = Lbldg01ErrorType.ERR_SOLID_INTERSECT;
          var element1Error = new GmlElementError(building1Id, solid1Id, null, solid1Id, solid1.getNodeName(), error);
          var element2Error = new GmlElementError(building2Id, solid2Id, null, solid2Id, solid2.getNodeName(), error);

          elementErrors.add(element1Error);
          elementErrors.add(element2Error);
        }
      }
    }
  }

  private boolean isSolidIntersect(Node solid1, Node solid2, Set<GmlElementError> elementErrors) {
    Geometry projectedOnOxyPlane1;
    try {
      projectedOnOxyPlane1 = getSolidExteriorProjectedOnOxyPlane(solid1, elementErrors);
    } catch (IllegalArgumentException | InvalidPosStringException e) {
      return false;
    }
    Geometry projectedOnOxyPlane2;
    try {
      projectedOnOxyPlane2 = getSolidExteriorProjectedOnOxyPlane(solid2, elementErrors);
    } catch (IllegalArgumentException | InvalidPosStringException e) {
      return false;
    }

    // Check intersection
    // If intersection is a polygon, then two solids intersect
    // Otherwise, two solids do not intersect (perhaps touch or no touch)
    return (projectedOnOxyPlane1.intersection(projectedOnOxyPlane2)) instanceof Polygon;
  }

  /**
   * Get result of union the exterior polygons of a solid and project them on Oxy plane
   *
   * @param solid solid node
   * @return Get result of union the exterior polygons of a solid and project them on Oxy plane
   */
  Geometry getSolidExteriorProjectedOnOxyPlane(Node solid, Set<GmlElementError> elementErrors) {
    String buildingGmlId = XmlUtil.getUserDataAttribute(solid, BUILDING_ID, String.class);
    String solidGmlId = XmlUtil.getAttribute(solid, TagName.GML_ID);

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
              elementErrors.add(new GmlElementError(
                  buildingGmlId,
                  solidGmlId,
                  null,
                  XmlUtil.getGmlId(polygon),
                  polygon.getNodeName(),
                  Lbldg01ErrorType.ERR_INVALID_FORMAT_POINTS
              ));
              isError = true;
            }
          }
        }
      }
    }

    if (isError) {
      throw new IllegalArgumentException(String.format("There are some polygons of building %s are invalid points", buildingGmlId));
    }
    // Union all exterior polygons
    return new GeometryFactory().createGeometryCollection(
        results.toArray(new Polygon[results.size()])).union();
  }
}
