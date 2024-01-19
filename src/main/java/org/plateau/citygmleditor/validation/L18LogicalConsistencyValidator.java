package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.Relationship;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.PythonUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
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

    static class BuildingInvalid {
        private String buildingID;

        private String compositeSurface;

        public String getBuildingID() {
            return buildingID;
        }

        public void setBuildingID(String buildingID) {
            this.buildingID = buildingID;
        }

        public void setCompositeSurface(String compositeSurface) {
            this.compositeSurface = compositeSurface;
        }

        public String toString() {
            return "buildingID = " + buildingID + " \n" + "compositeSurface = " + compositeSurface + "]";
        }
    }

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        File gmlFile = new File(World.getActiveInstance().getCityModel().getGmlPath());
        // get buildings from gml file
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(gmlFile, TagName.BLDG_BUILDING);
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            NodeList compositeSurfaces = building.getElementsByTagName(TagName.GML_COMPOSITE_SURFACE);

            List<String> invalidSurface = this.getInvalidSurface(compositeSurfaces);
            if (invalidSurface.isEmpty()) continue;
            BuildingInvalid invalid = new BuildingInvalid();
            invalid.setBuildingID(buildingID);
            invalid.setCompositeSurface(String.valueOf(invalidSurface));
            buildingInvalids.add(invalid);
        }
        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_L18_001, invalid)));
        }
        return messages;
    }

    private List<String> getInvalidSurface(NodeList compositeSurfaces) throws IOException {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < compositeSurfaces.getLength(); i++) {
            Element surface = (Element) compositeSurfaces.item(0);
            NodeList polygons = surface.getElementsByTagName(TagName.GML_POLYGON);

            boolean isPolygonValid = this.isPolygonValid(polygons);
            if (isPolygonValid) continue;
            String surfaceID = surface.getAttribute(TagName.GML_ID);
            if (!surfaceID.isEmpty()) {
                result.add("gml:id=" + surfaceID);
            } else {
                List<String> allPoslist = new ArrayList<>();
                for (int j = 0; j < polygons.getLength(); j++) {
                    Element polygon = (Element) polygons.item(i);
                    Element exterior = (Element) polygon.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                    Element posList = (Element) exterior.getElementsByTagName(TagName.GML_POSLIST).item(0);
                    String[] posString = posList.getTextContent().trim().split(" ");
                    allPoslist.add(Arrays.toString(posString));
                }
                result.add("gml:id=[]" + allPoslist);
            }
        }

        return result;
    }

    /**
     * check faces of surface intersect itself
     *
     * @param polygons list polygon input
     *                 return true if faces of surface touch at least 1 a other face and not intersect others faces
     */
    private boolean isPolygonValid(NodeList polygons) throws IOException {
        int totalPolygon = polygons.getLength();
        for (int i = 0; i < totalPolygon; i++) {
            Element polygon1 = (Element) polygons.item(i);
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");
            if (posString1.length % 3 != 0) {
                return false;
            }

            int count = 0;
            for (int j = i + 1; j < totalPolygon; j++) {
                Element polygon2 = (Element) polygons.item(j);
                Element exterior2 = (Element) polygon2.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
                Element posList2 = (Element) exterior2.getElementsByTagName(TagName.GML_POSLIST).item(0);
                String[] posString2 = posList2.getTextContent().trim().split(" ");

                Map<String, String> runCmdResult = PythonUtil.checkIntersecWithPyCmd(AppConst.PATH_PYTHON, posString1, posString2);
                if (!runCmdResult.get("ERROR").isBlank()) {
                    logger.severe("Error when running python file");
                    return false;
                }
                String output = runCmdResult.get("OUTPUT").trim();
                if (Objects.equals(output, Relationship.INTERSECT)) return false;
                if (Objects.equals(output, Relationship.TOUCH)) continue;

                if (Objects.equals(output, Relationship.NOT_INTERSECT)) {
                    count++;
                }
            }

            if (count == totalPolygon - 1) return false;
        }
        return true;
    }
}
