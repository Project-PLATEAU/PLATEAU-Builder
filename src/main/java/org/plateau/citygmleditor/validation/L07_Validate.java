package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.citygml4j.model.citygml.core.CityModel;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;


public class L07_Validate implements IValidator {
    public static Logger logger = Logger.getLogger(XmlUtil.class.getName());

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

    public List<ValidationResultMessage> validate(CityModel cityModel, String pathGmlFile) throws ParserConfigurationException, IOException, SAXException {
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        File file = new File(pathGmlFile);
        NodeList buildings = XmlUtil.getAllTagFromXmlFile(file, TagName.BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid linearRing tags
            NodeList tagLinearRings = building.getElementsByTagName(TagName.LINEARRING);
            List<String> linearRingIDInvalids = this.getListTagIDInvalid(tagLinearRings);
            // get invalid lineString tags
            NodeList tagLineStrings = building.getElementsByTagName(TagName.LINEARRING);
            List<String> lineStringIDvalids = this.getListTagIDInvalid(tagLineStrings);

            if (collectionEmpty(linearRingIDInvalids) && collectionEmpty(lineStringIDvalids)) continue;
            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setID(buildingID);
            if (!collectionEmpty(linearRingIDInvalids)) {
                buildingInvalid.setLinearRings(linearRingIDInvalids);
            }
            if (!collectionEmpty(lineStringIDvalids)) {
                buildingInvalid.setLineStrings(lineStringIDvalids);
            }
            buildingInvalids.add(buildingInvalid);
        }

        List<ValidationResultMessage> messages = new ArrayList<>();
        if (collectionEmpty(buildingInvalids)) return messages;
        messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, String.format("%sは重複して使用されています。\n", buildingInvalids)));

        return messages;
    }

    public boolean collectionEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
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
                point3Ds = this.get3Dpoints(posString);
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

    private List<Point3D> get3Dpoints(String[] posString) {
        int length = posString.length;
        if (length == 0 || length % 3 != 0) throw new RuntimeException("Invalid String");

        List<Point3D> point3DS = new ArrayList<>();
        for (int i = 0; i <= length - 3; ) {
            try {
                double x = Double.parseDouble(posString[i++]);
                double y = Double.parseDouble(posString[i++]);
                double z = Double.parseDouble(posString[i++]);
                Point3D point = new Point3D(x, y, z);
                point3DS.add(point);
            } catch (NumberFormatException e) {
                logger.severe("Error when parse from string to double");
                throw new RuntimeException("Invalid String");
            }
        }
        return point3DS;
    }

    private boolean isListPointValid(List<Point3D> points) {
        int pointSize = points.size();
        // polygon have less than 2 points is invalid
        if (pointSize < 2) {
            return false;
        }
        // don't need to check the endpoint
        for (int i = 0; i < pointSize - 1; i++) {
            if (points.get(i).distance(points.get(i + 1)) < 0.01) return false;
        }
        return true;
    }
}
