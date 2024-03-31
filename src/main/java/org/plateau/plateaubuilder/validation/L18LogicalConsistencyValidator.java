package org.plateau.plateaubuilder.validation;

import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.validation.constant.MessageError;
import org.plateau.plateaubuilder.validation.constant.PolygonRelationship;
import org.plateau.plateaubuilder.validation.constant.TagName;
import org.plateau.plateaubuilder.utils.CollectionUtil;
import org.plateau.plateaubuilder.utils.PythonUtil;
import org.plateau.plateaubuilder.utils.ThreeDUtil;
import org.plateau.plateaubuilder.utils.XmlUtil;
import org.plateau.plateaubuilder.validation.exception.GeometryPyException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public class L18LogicalConsistencyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(ThreeDUtil.class.getName());

    private static final int NO_ERROR = 0;
    private static final int OVERLAP_ERROR = 1;
    private static final int NO_TOUCH_ERROR = 2;
    private static final int POSLiST_ERROR = 3;
    private static final int OTHER_ERROR = 4;

    static class BuildingInvalid {
        private String buildingID;
        private Map<Integer, String> compositeSurface;
        private String lineString;

        public void setLineString(String lineString) {
            this.lineString = lineString;
        }

        public String getBuildingID() {
            return buildingID;
        }

        public void setBuildingID(String buildingID) {
            this.buildingID = buildingID;
        }

        public void setCompositeSurface(Map<Integer, String> compositeSurface) {
            this.compositeSurface = compositeSurface;
        }

        public String toString() {
            String strBuilding = MessageFormat.format(MessageError.ERR_L18_003_1, buildingID);
            if (lineString == null || lineString.isBlank()) {
                strBuilding =  strBuilding + MessageFormat.format(MessageError.ERR_L18_003_4, lineString);
            } else {
                for (Map.Entry<Integer, String> surfaceError : compositeSurface.entrySet()) {
                    if (surfaceError.getKey() == OVERLAP_ERROR) {
                        strBuilding = strBuilding + MessageFormat.format(MessageError.ERR_L18_003_2, compositeSurface);
                    } else if (surfaceError.getKey() == NO_TOUCH_ERROR) {
                        strBuilding = strBuilding + MessageFormat.format(MessageError.ERR_L18_003_3, compositeSurface);
                    } else {
                        strBuilding = strBuilding + MessageFormat.format(MessageError.ERR_L18_003_4, compositeSurface);
                    }
                }
            }

            return strBuilding;
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        File gmlFile = new File(cityModel.getGmlPath());
        // get buildings from gml file
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(gmlFile, TagName.BLDG_BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            NodeList compositeSurfaces = building.getElementsByTagName(TagName.GML_COMPOSITE_SURFACE);

            List<String> poslistError = new ArrayList<>();
            Map<Integer, String> invalidSurface = this.getInvalidSurface(compositeSurfaces, poslistError);

            if (invalidSurface.isEmpty() && poslistError.isEmpty()) continue;
            BuildingInvalid invalid = new BuildingInvalid();
            invalid.setBuildingID(buildingID);
            invalid.setCompositeSurface(invalidSurface);
            invalid.setLineString(poslistError.toString());
            buildingInvalids.add(invalid);
        }
        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, invalid.toString()));
        }
        return messages;
    }

    private Map<Integer, String> getInvalidSurface(NodeList compositeSurfaces, List<String> poslistError) throws IOException {
        Map<Integer, String> result = new HashMap<>();
        for (int i = 0; i < compositeSurfaces.getLength(); i++) {
            Element surface = (Element) compositeSurfaces.item(0);
            NodeList polygons = surface.getElementsByTagName(TagName.GML_POLYGON);

            int polygonValidCode = this.checkPolygonValid(polygons, poslistError);
            if (polygonValidCode == NO_ERROR) continue;
            String surfaceID = surface.getAttribute(TagName.GML_ID);
            if (!surfaceID.isEmpty()) {
                result.put(polygonValidCode, surfaceID);
            } else {
                List<String> allPoslist = new ArrayList<>();
                for (int j = 0; j < polygons.getLength(); j++) {
                    Element polygon = (Element) polygons.item(i);
                    Element exterior = (Element) polygon.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                    Element posList = (Element) exterior.getElementsByTagName(TagName.GML_POSLIST).item(0);
                    String[] posString = posList.getTextContent().trim().split(" ");
                    allPoslist.add(Arrays.toString(posString));
                }
                result.put(polygonValidCode, String.valueOf(allPoslist));
            }
        }

        return result;
    }

    /**
     * check faces of surface intersect the other of itself
     *
     * @param polygons list polygon input
     *                 return true if faces of surface touch at least 1 a other face and not intersect others faces
     */
    private int checkPolygonValid(NodeList polygons, List<String> poslistError) throws IOException {
        int totalPolygon = polygons.getLength();
        for (int i = 0; i < totalPolygon; i++) {
            Element polygon1 = (Element) polygons.item(i);
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");
            if (posString1.length % 3 != 0) {
                poslistError.add(Arrays.toString(posString1));
                return POSLiST_ERROR;
            }

            int count = 0;
            for (int j = i + 1; j < totalPolygon; j++) {
                Element polygon2 = (Element) polygons.item(j);
                Element exterior2 = (Element) polygon2.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
                String[] posString2 = posList2.getTextContent().trim().split(" ");

                try {
                    PolygonRelationship relationship = PythonUtil.checkPolygonRelationship(AppConst.PATH_PYTHON, posString1, posString2);
                    // 1 poglyon of solid intersect with the others of solid is invalid
                    if (relationship == PolygonRelationship.INTERSECT_3D) return OVERLAP_ERROR;
                    // count the polygon not intersect with the others
                    if (relationship == PolygonRelationship.NOT_INTERSECT) {
                        count++;
                    }
                } catch (GeometryPyException geometryPyException) {
                    poslistError.add(Arrays.toString(posString1));
                    return OTHER_ERROR;
                }
            }

            // solid have n polygon if 1 polygon of solid not intersect with (n-1) others of polygon is invalid
            if (count == totalPolygon - 1) return NO_TOUCH_ERROR;
        }
        return NO_ERROR;
    }
}
