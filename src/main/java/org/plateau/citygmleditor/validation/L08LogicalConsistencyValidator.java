package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.SolveEquationUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class L08LogicalConsistencyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(L08LogicalConsistencyValidator.class.getName());

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<String> invalidBuildings = new ArrayList<>();

        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);
            // get invalid lineString tags
            NodeList lineStrings = building.getElementsByTagName(TagName.GML_LINESTRING);
            List<String> invalidLineString = this.getInvalidTag(lineStrings);
            if (invalidLineString.isEmpty()) continue;
            String invalidBuilding = "gml:id=" + buildingID + "lineString=" + invalidLineString;
            invalidBuildings.add(invalidBuilding);
        }

        if (CollectionUtil.isEmpty(invalidBuildings)) return new ArrayList<>();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (String invalid : invalidBuildings) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, MessageFormat.format(MessageError.ERR_L08_001, invalid)));
        }
        return messages;
    }

    private List<String> getInvalidTag(NodeList lineStrings) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < lineStrings.getLength(); i++) {
            Element lineString = (Element) lineStrings.item(0);
            Element posList = (Element) lineString.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString = posList.getTextContent().trim().split(" ");

            if (posString.length % 3 != 0) result.add(Arrays.toString(posString));

            List<Point3D> point3DS = ThreeDUtil.createListPoint(posString);
            // check line intersect or touch others
            for (int j = 2; j < point3DS.size(); j++) {
                Point3D currentPoint = point3DS.get(j);
                boolean isPolistValid = this.checkPointIntersecOrTouch(j, currentPoint, point3DS);
                if (isPolistValid) continue;
                result.add(Arrays.toString(posString));
            }


        }
        return result;
    }

    private boolean checkPointIntersecOrTouch(int index, Point3D currentPoint, List<Point3D> pointsInput) {
        for (int i = 0; i < index; i++) {
            Point3D startPoint = pointsInput.get(i);
            Point3D endPoint = pointsInput.get(i + 1);
            Point3D intersection = this.findIntersection(currentPoint, startPoint, endPoint);

            boolean intersect = intersection == null || (startPoint.distance(intersection) + intersection.distance(endPoint)) == startPoint.distance(endPoint);
            if (intersect) return false;
        }
        return true;
    }

    private Point3D findIntersection(Point3D currentPoint, Point3D startPoint, Point3D endPoint) {
        // unit of d1
        Vec3f unit1 = SolveEquationUtil.createUnit(startPoint, endPoint);
        // unit of d2
        Vec3f unit2 = SolveEquationUtil.createUnit(endPoint, currentPoint);

        // find M1 in d1 (endpoint) M2 in d2 (currentPoint)
        float m1m2x = (float) (currentPoint.getX() - endPoint.getX());
        float m1m2y = (float) (currentPoint.getY() - endPoint.getY());

        // find parameter of line equation (t)
        double t1 = (SolveEquationUtil.calculateDeterminant(unit2.y, unit2.x, m1m2y, m1m2x)) / (SolveEquationUtil.calculateDeterminant(unit1.x, unit2.x, unit1.y, unit2.y));
        double t2 = (-m1m2x + unit1.x * t1) / unit2.x;

        if ((endPoint.getZ() + unit1.z * t1) != (currentPoint.getZ() + unit2.z * t2)) return null;
        return new Point3D(endPoint.getX() + unit1.x * t1, endPoint.getY() + unit1.y * t1, endPoint.getZ() + unit1.z * t1);
    }
}