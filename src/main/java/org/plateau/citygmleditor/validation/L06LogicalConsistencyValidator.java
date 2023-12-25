package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.citygml4j.model.gml.geometry.primitives.DirectPosition;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 幾何オブジェクトインスタンスの座標値に含まれる、緯度、経度、標高が、その幾何オブジェクトインスタンスを含む都市モデル（core:CityModel）の空間範囲に含まれていることを検査します。
 **/
public class L06LogicalConsistencyValidator implements IValidator {

    /**
     * The function checks L06 validate
     *
     * @param cityModelView the model of city contain: building, surfaceMember, polygon, ...
     */
    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();
        org.citygml4j.model.citygml.core.CityModel cityModel = cityModelView.getGmlObject();

        // Read all posList objects in gml file
        File input = new File(World.getActiveInstance().getCityModel().getGmlPath());
        NodeList nodes = XmlUtil.getAllTagFromXmlFile(input, TagName.GML_POSLIST);

        DirectPosition lowerCorner = cityModel.getBoundedBy().getEnvelope().getLowerCorner();
        DirectPosition upperCorner = cityModel.getBoundedBy().getEnvelope().getUpperCorner();
        if (Objects.isNull(lowerCorner) || Objects.isNull(upperCorner)) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    "L06: The upperCorner or lowerCorner is Null"));
            return messages;
        }
        Point3D lowerPoint = new Point3D(lowerCorner.getValue().get(0), lowerCorner.getValue().get(1), lowerCorner.getValue().get(2));
        Point3D upperPoint = new Point3D(upperCorner.getValue().get(0), upperCorner.getValue().get(1), upperCorner.getValue().get(2));

        Map<String, String> errorMap = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            List<String> values = new ArrayList<>(Arrays.asList(node.getFirstChild().getNodeValue().split(" ")));
            List<Point3D> point3Ds = ThreeDUtil.createListPoint(values.toArray(new String[0]));
            if (values.size() % 3 != 0) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                        "L06: The coordinates in postList error!"));
            }

            Node nodeParentBuilding = XmlUtil.findNearestParentByAttribute(node, TagName.GML_ID);
            String id = nodeParentBuilding.getAttributes().getNamedItem("gml:id").getTextContent();

            // Check the point within spatial extent of the model city
            for (Point3D point3D : point3Ds) {
                if (lowerPoint.getX() > point3D.getX() || point3D.getX() > upperPoint.getX()
                        || lowerPoint.getY() > point3D.getY() || point3D.getY() > upperPoint.getY()
                        || lowerPoint.getZ() > point3D.getZ() || point3D.getZ() > upperPoint.getZ()) {
                    if (errorMap.containsKey(id)) {
                        errorMap.replace(id, String.format("%s\n%s %s %s", errorMap.get(id), point3D.getX(), point3D.getY(), point3D.getZ()));
                    } else {
                        errorMap.put(id, String.format("L06: gml:id=\"%s\"のbuildingにエラー座標値：%s %s %s", id, point3D.getX(), point3D.getY(), point3D.getZ()));
                    }
                }
            }
        }
        for (Map.Entry<String, String> k : errorMap.entrySet()) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, k.getValue()));
        }

        return messages;
    }
}
