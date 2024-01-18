package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class L07LogicalConsistencyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(L07LogicalConsistencyValidator.class.getName());
    public static final double VALID_DISTANCE_L07 = 0.01;

    static class BuildingInvalid {
        private String ID;
        private List<String> linearRings;
        private List<String> lineStrings;

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public List<String> getLinearRings() {
            return linearRings;
        }

        public void setLinearRings(List<String> linearRings) {
            this.linearRings = linearRings;
        }

        public List<String> getLineStrings() {
            return lineStrings;
        }

        public void setLineStrings(List<String> lineStrings) {
            this.lineStrings = lineStrings;
        }

        public String toString() {
            String linearRingStr = this.linearRings == null ? "" : " LinearRing= " + this.linearRings;
            String linearStringStr = this.lineStrings == null ? "" : " LineString= " + this.lineStrings;
            return "bldg:Building gml:id=" + this.ID + "\n" + linearRingStr + "\n" + linearStringStr;
        }
    }

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getAllTagFromCityModel(cityModelView, TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid linearRing tags
            NodeList tagLinearRings = building.getElementsByTagName(TagName.GML_LINEARRING);
            List<String> linearRingIDInvalids = this.getListTagIDInvalid(tagLinearRings);
            // get invalid lineString tags
            NodeList tagLineStrings = building.getElementsByTagName(TagName.GML_LINESTRING);
            List<String> lineStringIDvalids = this.getListTagIDInvalid(tagLineStrings);

            if (CollectionUtil.isEmpty(linearRingIDInvalids) && CollectionUtil.isEmpty(lineStringIDvalids))
                continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setID(buildingID);
            if (!CollectionUtil.isEmpty(linearRingIDInvalids)) {
                buildingInvalid.setLinearRings(linearRingIDInvalids);
            }
            if (!CollectionUtil.isEmpty(lineStringIDvalids)) {
                buildingInvalid.setLineStrings(lineStringIDvalids);
            }
            buildingInvalids.add(buildingInvalid);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    MessageFormat.format(MessageError.ERR_L07_001, invalid)));
        }
        return messages;
    }

    private List<String> getListTagIDInvalid(NodeList tags) {
        if (tags == null) return null;

        List<String> tagInvalids = new ArrayList<>();
        for (int i = 0; i < tags.getLength(); i++) {
            Node tag = tags.item(i);
            Element tagElement = (Element) tag;
            String[] posString = tagElement.getTextContent().trim().split(" ");

            // split posList into points
            List<Point3D> point3Ds;
            try {
                point3Ds = ThreeDUtil.createListPoint(posString);
            } catch (InvalidPosStringException e) {
                String attribute = tagElement.getAttribute(TagName.GML_ID);
                tagInvalids.add(this.createAttributeGmlID(attribute, posString));
                continue;
            }

            // check valid distance from two point int point3Ds
            if (this.isListPointValid(point3Ds)) continue;
            String attribute = tagElement.getAttribute(TagName.GML_ID);
            tagInvalids.add(this.createAttributeGmlID(attribute, posString));
        }

        return tagInvalids;
    }

    private String createAttributeGmlID(String attribute, String[] posString) {
        if (attribute.isBlank()) {
            attribute = "[]" + Arrays.toString(posString);
        } else {
            attribute = "[gml:id=" + attribute + "]";
        }
        return attribute;
    }

    private boolean isListPointValid(List<Point3D> points) {
        int pointSize = points.size();
        // polygon have less than 2 points is invalid
        if (pointSize < 2) {
            logger.severe(String.format("L07 Poslist have less than 2 points (%s)", points));
            return false;
        }
        // don't need to check the endpoint
        for (int i = 0; i < pointSize - 1; i++) {
            double distance = ThreeDUtil.distance(points.get(i), points.get(i + 1));
            if (distance < VALID_DISTANCE_L07) {
                logger.severe(String.format("L07 Distance between (%s and %s) = %s", points.get(i), points.get(i + 1), distance));
                return false;
            }
        }
        return true;
    }
}
