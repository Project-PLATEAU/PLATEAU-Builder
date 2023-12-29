package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Geometry;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.CityGmlUtil;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.ThreeDUtil;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


public class LBldg01LogicalConsistencyValidator implements IValidator {
    public static Logger logger = Logger.getLogger(ThreeDUtil.class.getName());

    static class BuildingInvalid {
        private String ID;
        private List<String> lod1Solid;
        private List<String> lod2Solid;

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public List<String> getLod1Solid() {
            return lod1Solid;
        }

        public void setLod1Solid(List<String> lod1Solid) {
            this.lod1Solid = lod1Solid;
        }

        public List<String> getLod2Solid() {
            return lod2Solid;
        }

        public void setLod2Solid(List<String> lod2Solid) {
            this.lod2Solid = lod2Solid;
        }

        public String toString() {
//            String linearRingStr = this.linearRings == null ? "" : " LinearRing= " + this.linearRings;
//            String linearStringStr = this.lineStrings == null ? "" : " LineString= " + this.lineStrings;
            return "bldg:Building gml:id=" + this.ID + "\n" + "linearRingStr" + "\n" + "linearStringStr";
        }
    }

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<BuildingInvalid> buildingInvalids = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getAllTagFromCityModel(cityModelView, TagName.BLDG_BUILDING);

        for (int i = 0; i < buildings.getLength(); i++) {
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            // get invalid linearRing tags
            NodeList tagLod1Solids = building.getElementsByTagName(TagName.BLDG_LOD1SOLID);
            List<String> lod1Invalids = this.getListTagIDInvalid(tagLod1Solids);
            // get invalid lineString tags
            NodeList tagLod2Solids = building.getElementsByTagName(TagName.BLDG_LOD2SOLID);
            List<String> lineStringIDvalids = this.getListTagIDInvalid(tagLod2Solids);
        }

        if (CollectionUtil.isEmpty(buildingInvalids)) return List.of();
        List<ValidationResultMessage> messages = new ArrayList<>();
        for (BuildingInvalid invalid : buildingInvalids) {
            messages.add(new ValidationResultMessage(ValidationResultMessageType.Error,
                    MessageFormat.format(MessageError.ERR_L07_001, invalid)));
        }
        return messages;
    }

    private List<Map<String, Geometry>> createSolid(NodeList solids) {
        List<Map<String, Object>> valid = new ArrayList<>();

        for (int i = 0; i < solids.getLength(); i++) {
            Element solidElement = (Element) solids.item(i);
            String lod1Solid = solidElement.getAttribute(TagName.GML_ID).trim();
            NodeList exteriors = solidElement.getElementsByTagName(TagName.GML_EXTERIOR);
            Object faces = this.createListFaces(exteriors);

            String[] invalidFace = (String[]) faces.get("invalid");
            if (invalidFace.length > 0) {
                String attribute = solidElement.getAttribute(TagName.GML_ID);
                if (attribute.isBlank()) {
                    valid.add("gml:id=[]" + Arrays.toString(invalidFace));
                } else {
                    valid.add("gml:id=" + attribute);
                }
            }
        }
        return valid;
    }

    private List<String> getListTagIDInvalid(NodeList tags) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < tags.getLength(); i++) {
            Element element = (Element) tags.item(i);
            NodeList exteriors = element.getElementsByTagName(TagName.GML_EXTERIOR);
            Map<String, Object> faces = this.createListFaces(exteriors);
            String[] invalidFace = (String[]) faces.get("invalid");
            if (invalidFace.length > 0) {
                String attribute = element.getAttribute(TagName.GML_ID);
                if (attribute.isBlank()) {
                    result.add("gml:id=[]" + Arrays.toString(invalidFace));
                } else {
                    result.add("gml:id=" + attribute);
                }
            }
        }
        return result;
    }

    private Map<String, List<String>> getListSolidInvalid(List<Geometry> lod1Solid, List<Geometry> lod2Solid) {

        for (Geometry lod1 : lod1Solid) {
            boolean flag = false;
            for (Geometry lod2 : lod2Solid) {

            }
        }
    }

    private Object createListFaces(NodeList exteriors) {
        List<Object> result = new ArrayList<>();

        for (int i = 0; i < exteriors.getLength(); i++) {
            Element exterior = (Element) exteriors.item(i);
            Element posList = (Element) exterior.getElementsByTagName(TagName.GML_POSLIST).item(0);
            String[] posString = posList.getTextContent().trim().split(" ");
            // split posList into points
            List<Point3D> point3Ds;
            try {
                point3Ds = ThreeDUtil.createListPoint(posString);
                Geometry face = ThreeDUtil.createPolygon(point3Ds);
                result.add(face);
            } catch (InvalidPosStringException e) {
                result.add(posList);
            }
        }
        return result;
    }
}
