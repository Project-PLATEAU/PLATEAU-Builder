package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.citygml4j.model.citygml.core.CityModel;
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


public class L07LogicalConsistencyValidator implements IValidator {
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
            return "BuildingID= " + this.ID + "\n" + "LinearRing= " + this.linearRings + "\n" + "LineString= " + this.lineStrings;
        }
    }

    public List<ValidationResultMessage> validate(CityModel cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        File file = new File(World.getActiveInstance().getCityModel().getGmlPath());
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(file, TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid linearRing tags
            NodeList tagLinearRings = building.getElementsByTagName(TagName.GML_LINEARRING);
            List<String> linearRingIDInvalids = this.getListTagIDInvalid(tagLinearRings);
            // get invalid lineString tags
            NodeList tagLineStrings = building.getElementsByTagName(TagName.GML_LINEARRING);
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
            List<Point3D> point3Ds = new ArrayList<>();
            try {
                point3Ds = ThreeDUtil.createListPoint(posString);
            } catch (RuntimeException e) {
                String attribute = tagElement.getAttribute(TagName.GML_ID);
                tagInvalids.add(attribute);
                continue;
            }

            // check valid distance from two point int point3Ds
            if (this.isListPointValid(point3Ds)) continue;
            String attribute = tagElement.getAttribute(TagName.GML_ID);
            tagInvalids.add(attribute);
        }

        return tagInvalids;
    }

    private boolean isListPointValid(List<Point3D> points) {
        int pointSize = points.size();
        // polygon have less than 2 points is invalid
        if (pointSize < 2) {
            return false;
        }
        // don't need to check the endpoint
        for (int i = 0; i < pointSize - 1; i++) {
            if (ThreeDUtil.distance(points.get(i), points.get(i + 1)) < 0.01) return false;
        }
        return true;
    }
}
