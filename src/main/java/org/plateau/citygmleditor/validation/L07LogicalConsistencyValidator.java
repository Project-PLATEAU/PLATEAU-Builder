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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class L07LogicalConsistencyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(L07LogicalConsistencyValidator.class.getName());
    public static final double VALID_DISTANCE_L07 = 0.01;

    private final static String FORMAT_ERROR = "FORMAT";
    private final static String LOGICAL_ERROR = "LOGICAL";

    static class BuildingInvalid {
        private String ID;
        private Map<String, List<String>> linearRings;
        private Map<String, List<String>> lineStrings;

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setLinearRings(Map<String, List<String>> linearRings) {
            this.linearRings = linearRings;
        }

        public void setLineStrings(Map<String, List<String>> lineStrings) {
            this.lineStrings = lineStrings;
        }

        public String toString() {
            String errorFormatLineString = this.lineStrings.get(FORMAT_ERROR).stream().map(f -> "<gml:LineString gml:id=\"" + f + "\">")
                    .collect(Collectors.joining("\n"));
            String errorLogicalLineString = this.lineStrings.get(LOGICAL_ERROR).stream().map(l -> "<gml:LineString gml:id=\"" + l + "\">")
                    .collect(Collectors.joining("\n"));
            String errorFormatLinearRing = this.linearRings.get(FORMAT_ERROR).stream().map(f -> "<gml:LinearRings gml:id=\"" + f + "\">")
                    .collect(Collectors.joining("\n"));
            String errorLogicalLinearRing = this.linearRings.get(LOGICAL_ERROR).stream().map(l -> "<gml:LinearRings gml:id=\"" + l + "\">")
                    .collect(Collectors.joining("\n"));
            String errorFormat = (errorFormatLineString.isBlank() ? "" : ("\n" + errorFormatLineString) + "\n")
                    + (errorFormatLinearRing.isBlank() ? "" : errorFormatLinearRing + "\n");
            String errorLogical = (errorLogicalLineString.isBlank() ? "" : ("\n" + errorLogicalLineString) + "\n")
                    + (errorLogicalLinearRing.isBlank() ? "" : errorLogicalLinearRing + "\n");
            return MessageFormat.format(MessageError.ERR_L07_002_1, ID, errorFormat, errorLogical);
        }
    }

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        List<ValidationResultMessage> messages = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            List<GmlElementError> elementErrors = new ArrayList<>();

            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid linearRing tags
            NodeList tagLinearRings = building.getElementsByTagName(TagName.GML_LINEARRING);
            Map<String, List<String>> linearRingIDInvalids = this.getListTagIDInvalid(tagLinearRings);
            // get invalid lineString tags
            NodeList tagLineStrings = building.getElementsByTagName(TagName.GML_LINESTRING);
            Map<String, List<String>> lineStringIDvalids = this.getListTagIDInvalid(tagLineStrings);

            if (linearRingIDInvalids.get(FORMAT_ERROR).isEmpty() && linearRingIDInvalids.get(LOGICAL_ERROR).isEmpty()
                    && lineStringIDvalids.get(FORMAT_ERROR).isEmpty() && lineStringIDvalids.get(LOGICAL_ERROR).isEmpty())
                continue;

            BuildingInvalid buildingInvalid = new BuildingInvalid();
            buildingInvalid.setID(buildingID);
            buildingInvalid.setLinearRings(linearRingIDInvalids);
            buildingInvalid.setLineStrings(lineStringIDvalids);

            List<String> linearRings = linearRingIDInvalids.get(FORMAT_ERROR);
            linearRings.addAll(linearRingIDInvalids.get(LOGICAL_ERROR));
            List<String> lineStrings = lineStringIDvalids.get(FORMAT_ERROR);
            lineStrings.addAll(lineStringIDvalids.get(LOGICAL_ERROR));

            // add linearRing to gmlElementError
            for (String linearRing : linearRings) {
                elementErrors.add(new GmlElementError(
                        buildingID,
                        null,
                        null,
                        linearRing, TagName.GML_LINEARRING,
                        0));
            }

            // add lineString to gmlElementError
            for (String lineString : lineStrings) {
                elementErrors.add(new GmlElementError(
                        buildingID,
                        null,
                        null,
                        lineString, TagName.GML_LINESTRING,
                        0));
            }

            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingInvalid.toString(), elementErrors));
        }

        return messages;
    }

    private Map<String, List<String>> getListTagIDInvalid(NodeList tags) {
        if (tags == null) return null;
        Map<String, List<String>> invalidTag = new HashMap<>();

        List<String> errorFormat = new ArrayList<>();
        List<String> errorLogical = new ArrayList<>();
        for (int i = 0; i < tags.getLength(); i++) {
            Node tag = tags.item(i);
            Element tagElement = (Element) tag;
            String[] posString = tagElement.getTextContent().trim().split(" ");
            String attribute = tagElement.getAttribute(TagName.GML_ID).trim();

            // split posList into points
            List<Point3D> point3Ds;
            try {
                point3Ds = ThreeDUtil.createListPoint(posString);
            } catch (InvalidPosStringException e) {
                errorFormat.add(attribute);
                continue;
            }
            // check polygon is closed
            if (!point3Ds.get(0).equals(point3Ds.get(point3Ds.size() - 1))) {
                errorFormat.add(attribute);
                continue;
            }
            // check valid distance from two point int point3Ds
            if (this.isListPointValid(point3Ds)) continue;
            errorLogical.add(attribute);
        }
        invalidTag.put(FORMAT_ERROR, errorFormat);
        invalidTag.put(LOGICAL_ERROR, errorLogical);
        return invalidTag;
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
