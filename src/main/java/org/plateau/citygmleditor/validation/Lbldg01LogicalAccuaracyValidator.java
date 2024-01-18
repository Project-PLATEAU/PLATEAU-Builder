package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.apache.commons.lang3.ArrayUtils;
import org.locationtech.jts.geom.*;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Lbldg01LogicalAccuaracyValidator implements IValidator {

    @Override
    public List<ValidationResultMessage> validate(CityModelView cityModel) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();

        File input = new File(World.getActiveInstance().getCityModel().getGmlPath());
        NodeList solids = XmlUtil.getAllTagFromXmlFile(input, TagName.GML_SOLID);

        GeometryFactory geometryFactory = new GeometryFactory();
        List<Geometry> solidGeometries = new ArrayList<>();
        //List  polygon in solids
        for (int i = 0; i < solids.getLength(); i++) {
            Geometry solidGeometry = geometryFactory.createEmpty(0);
            List<String[]> list = getPosListInSolid((Element) solids.item(i));
            for (String[] posString : list) {

                List<Point3D> point3Ds = ThreeDUtil.createListPoint(posString);

                Geometry polygon = ThreeDUtil.createPolygon(point3Ds);
                solidGeometry.union(polygon);
            }
            solidGeometries.add(solidGeometry);
        }

        for (int i = 0; i < solidGeometries.size()-1; i++) {
            for (int j = 0; j < solidGeometries.size(); j++) {
                if (solidGeometries.get(i).intersects(solidGeometries.get(j))) {
                    messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, "Errror"));
                }
            }
        }

        return messages;
    }

    List<String[]> getPosListInSolid(Element solid) {
        NodeList polygons = solid.getElementsByTagName(TagName.GML_POLYGON);
        if (polygons.getLength() <= 0)
            return new ArrayList<>();

        List<String[]> result = new ArrayList<>();
        for (int i = 0; i < polygons.getLength(); i++) {
            Element polygon = (Element) polygons.item(0);
            NodeList exteriors = polygon.getElementsByTagName(TagName.GML_EXTERIOR);
            if (exteriors.getLength() <= 0)
                return new ArrayList<>();

            NodeList posLists = ((Element) exteriors.item(0)).getElementsByTagName(TagName.GML_POSLIST);
            if (Objects.isNull(posLists))
                return new ArrayList<>();

            for (int k = 0; k < posLists.getLength(); k++) {
                Element poslist = (Element) posLists.item(k);
                String[] posString = poslist.getTextContent().trim().split(" ");
                for (int j = 0; j < posString.length; j++) {
                    if (j % 3 == 2) {
                        posString[j] = "0";
                    }
                }
                result.add(posString);
            }
        }

        return result;
    }
}



