package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Geometry;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CollectionUtil;
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
import java.util.ArrayList;
import java.util.List;
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

        public String getCompositeSurface() {
            return compositeSurface;
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

            //Check each compositeSurfaces one by one
            for (int j = 0; j < compositeSurfaces.getLength(); j++) {
                Element compositeSurface = (Element) compositeSurfaces.item(j);
                List<String> compositeInvalids = this.getListCompositeSurfaceInvalid(compositeSurface);
                if (compositeInvalids.isEmpty()) continue;
                BuildingInvalid invalid = new BuildingInvalid();
                invalid.setBuildingID(buildingID);
                String compositeID = compositeSurface.getAttribute(TagName.GML_ID);
                if (compositeID.isBlank()) {
                    invalid.setCompositeSurface("gml:id=[]" + compositeInvalids);
                } else {
                    invalid.setCompositeSurface("gml:id=" + compositeID + compositeInvalids);
                }
                buildingInvalids.add(invalid);
            }
        }
        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_L18_001, invalid)));
        }
        return messages;
    }

    /**
     * Create Geometry by compositeSurface
     *
     * @return List Geomey by poslis in compositeSurface
     */
    private List<Geometry> createGeometry(Element compositeSurface) {
        List<Geometry> polygons = new ArrayList<>();
        NodeList posList = compositeSurface.getElementsByTagName(TagName.GML_POSLIST);
        for (int i = 0; i < posList.getLength(); i++) {
            Element posElement = (Element) posList.item(i);
            String[] posString = posElement.getTextContent().trim().split(" ");

            // split posList into points
            try {
                List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);
                Geometry polygon = ThreeDUtil.createPolygon(point3Ds);
                polygons.add(polygon);
            } catch (RuntimeException e) {
                logger.severe("Error when convert point to geometry");
                throw new RuntimeException("Invalid String");
            }
        }
        return polygons;
    }

    /**
     * Check each pair of planes in compositeSurface
     *
     * @param compositeSurface Element
     * @return list invalid planes in compositeSurface
     */
    private List<String> getListCompositeSurfaceInvalid(Element compositeSurface) {
        List<Geometry> geometries = this.createGeometry(compositeSurface);
        List<String> geometrys = new ArrayList<>();
        for (int i = 0; i < geometries.size(); i++) {
            Geometry geo1 = geometries.get(i);
            for (int j = i + 1; j < geometries.size() - 1; j++) {
                Geometry geo2 = geometries.get(j);
                if (!geo1.touches(geo2)) {
                    String geoString = geo2.toString();
                    geometrys.add(geoString);
                }
            }
        }

        return geometrys;
    }
}