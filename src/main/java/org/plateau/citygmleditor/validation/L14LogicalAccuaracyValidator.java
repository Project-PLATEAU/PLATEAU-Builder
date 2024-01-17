package org.plateau.citygmleditor.validation;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.PythonUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public class L14LogicalAccuaracyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(L14LogicalAccuaracyValidator.class.getName());

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {

        NodeList buildings = CityGmlUtil.getAllTagFromCityModel(cityModelView, TagName.BLDG_BUILDING);
        List<String> buildingError = new ArrayList<>();

        for (int i = 0; i < buildings.getLength(); i++) {
            Node tagBuilding = buildings.item(i);
            Element building = (Element) tagBuilding;
            String buildingID = building.getAttribute(TagName.GML_ID);
            List<String> invalidSolid = this.getInvalidSolid(building);

            if (invalidSolid.isEmpty()) continue;
            buildingError.add("buildingID=" + buildingID + "[" + invalidSolid + "]");
        }
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (String invalid : buildingError) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    MessageFormat.format(MessageError.ERR_L14_001, invalid)));
        }
        return messages;
    }

    private List<String> getInvalidSolid(Element building) throws IOException {
        NodeList solids = building.getElementsByTagName(TagName.GML_SOLID);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < solids.getLength(); i++) {
            Element solid = (Element) solids.item(i);
            NodeList polygons = solid.getElementsByTagName(TagName.GML_POLYGON);
            List<String> invalidPolygon = this.getInvalidPolygon(polygons);

            if (invalidPolygon.isEmpty()) continue;
            result.add("solid=[" + invalidPolygon + "]");
        }
        return result;
    }

    private List<String> getInvalidPolygon(NodeList polygons) throws IOException {
        List<String> result = new ArrayList<>();
        int totalPolygon = polygons.getLength();
        outterLoop:
        for (int i = 0; i < totalPolygon; i++) {
            Element polygon1 = (Element) polygons.item(i);
            Element exterior1 = (Element) polygon1.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
            Element posList1 = (Element) exterior1.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString1 = posList1.getTextContent().trim().split(" ");

            if (posString1.length % 3 != 0) {
                this.setValueInvalidPolygon(polygon1, posString1, result);
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
                    this.setValueInvalidPolygon(polygon1, posString1, result);
                }
                String output = runCmdResult.get("OUTPUT").trim();
                if (Objects.equals(output, "touch")) continue;
                if (Objects.equals(output, "intersect")) {
                    this.setValueInvalidPolygon(polygon1, posString1, result);
                    continue outterLoop;
                }
                if (Objects.equals(output, "do not intersect")) {
                    count++;
                }
            }
            if (count == totalPolygon - 1) {
                this.setValueInvalidPolygon(polygon1, posString1, result);
            }
        }
        return result;
    }

    private void setValueInvalidPolygon(Element polygon, String[] posList, List<String> invalidPolygon) {
        if (polygon.getAttribute(TagName.GML_ID).isBlank()) {
            invalidPolygon.add("Polygon=" + Arrays.toString(posList));
        } else {
            invalidPolygon.add("Polygon=" + polygon.getAttribute(TagName.GML_ID));
        }
    }
}
