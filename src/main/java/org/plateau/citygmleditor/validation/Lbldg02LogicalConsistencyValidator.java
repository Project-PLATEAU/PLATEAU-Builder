package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Geometry;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.constant.MessageError;
import org.plateau.citygmleditor.constant.TagName;
import org.plateau.citygmleditor.utils.*;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Lbldg02LogicalConsistencyValidator implements IValidator {
    static class BuildingInvalid {
        private String ID;
        private List<String> buildingParts;
        private List<String> polygons;

        public void setPolygons(List<String> polygons) {
            this.polygons = polygons;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public void setBuidlingPart(List<String> buidlingPart) {
            this.buildingParts = buidlingPart;
        }

        public String toString() {
            if (CollectionUtil.isEmpty(polygons) && CollectionUtil.isEmpty(buildingParts)) return "";
            if (CollectionUtil.isEmpty(buildingParts)) {
                String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n"
                        + this.polygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>")
                        .collect(Collectors.joining(":\n"));
                return String.format(MessageError.ERR_L_BLDG_02_001, ID, polygonStr, "");
            }
            if (CollectionUtil.isEmpty(polygons)) {
                String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
                return String.format(MessageError.ERR_L_BLDG_02_001, ID, bpStr, "");
            }
            String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
            String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n" + this.polygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>").collect(Collectors.joining(":\n"));
            return String.format(MessageError.ERR_L_BLDG_02_001, ID, polygonStr, "\n" + bpStr);
        }
    }

    static class BuildingPart {
        private String ID;

        private List<String> solid;

        public void setID(String ID) {
            this.ID = ID;
        }

        public List<String> getSolid() {
            return solid;
        }

        public void setSolid(List<String> solid) {
            this.solid = solid;
        }

        public String getID() {
            return ID;
        }

        @Override
        public String toString() {
            return "bldg:BuildingPart = " + this.ID + " solid = " + solid;
        }
    }

    public List<ValidationResultMessage> validate(CityModelView cityModelView) throws ParserConfigurationException, IOException, SAXException {
        List<ValidationResultMessage> messages = new ArrayList<>();
        NodeList buildings = CityGmlUtil.getXmlDocumentFrom(cityModelView).getElementsByTagName(TagName.BLDG_BUILDING);
        for (int i = 0; i < buildings.getLength(); i++) {
            List<GmlElementError> elementErrors = new ArrayList<>();
            Element building = (Element) buildings.item(i);
            String buildingID = building.getAttribute(TagName.GML_ID);

            NodeList buildingPart = building.getElementsByTagName(TagName.BLGD_BUILDING_PART);
            if (buildingPart.getLength() == 0) continue;

            List<Node> lod1Solid = createLod1Solid(buildingPart);
            if (lod1Solid.isEmpty()) continue;
            // validate solid lod 1 in building part
            List<Solid> solidModel = this.createSolidModel(lod1Solid);
            List<Solid> invalidSolidLod1 = this.validateSolid(solidModel);
            // validate format points
            List<String> invalidPolygon = this.getWrongFormatePolygon(buildingPart);
            if (invalidPolygon.isEmpty() && invalidSolidLod1.isEmpty()) continue;

            BuildingInvalid buildingInvalid = this.setBuildingError(buildingID, invalidPolygon, invalidSolidLod1, elementErrors);

            String buildingStr = buildingInvalid.toString();
            if (!buildingStr.isBlank()) {
                messages.add(new ValidationResultMessage(ValidationResultMessageType.Error, buildingStr, elementErrors));
            }
        }

        return messages;
    }

    private List<Node> createLod1Solid(NodeList buildingParts) {
        List<Node> result = new ArrayList<>();
        for (int i = 0; i < buildingParts.getLength(); i++) {
            Element buildingPart = (Element) buildingParts.item(i);
            Node lod1Solid = buildingPart.getElementsByTagName(TagName.BLDG_LOD_1_SOLID).item(0);
            if (lod1Solid == null) continue;
            result.add(lod1Solid);
        }
        return result;
    }

    private BuildingInvalid setBuildingError(String buildingID, List<String> invalidPolygon, List<Solid> solids, List<GmlElementError> elementErrors) {
        List<String> invalidBP = solids.stream().map(Solid::getBuildingPartID).collect(Collectors.toList());
        BuildingInvalid buildingInvalid = new BuildingInvalid();
        buildingInvalid.setID(buildingID);
        buildingInvalid.setPolygons(invalidPolygon);
        buildingInvalid.setBuidlingPart(invalidBP);
        elementErrors.add(new GmlElementError(
                buildingID,
                invalidBP.toString(),
                invalidPolygon.toString(),
                null,
                null,
                0));

        return buildingInvalid;
    }

    public List<String> getWrongFormatePolygon(NodeList nodeList) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element element = (Element) nodeList.item(i);
            NodeList posLists = element.getElementsByTagName(TagName.GML_POSLIST);
            for (int j = 0; j < posLists.getLength(); j++) {
                Node posList = posLists.item(j);
                String[] posString = posList.getTextContent().trim().split(" ");
                if (posString.length % 3 != 0) {
                    Element parent = (Element) XmlUtil.findNearestParentByName(posList, TagName.GML_POLYGON);
                    assert parent != null;
                    String polygonID = parent.getAttribute(TagName.GML_ID);
                    result.add(polygonID.isBlank() ? "[]" : polygonID);
                }
            }
        }
        return result;
    }

    private List<Solid> createSolidModel(List<Node> lodSolids) {
        List<Solid> result = new ArrayList<>();
        for (Node solid : lodSolids) {
            Element solidE = (Element) solid;
            Element parent = (Element) XmlUtil.findNearestParentByName(solid, TagName.BLGD_BUILDING_PART);
            if (parent == null) continue;
            String buildingPartID = parent.getAttribute(TagName.GML_ID);
            NodeList polygons = solidE.getElementsByTagName(TagName.GML_POLYGON);
            List<List<Point3D>> faces = new ArrayList<>();
            for (int i = 0; i < polygons.getLength(); i++) {
                faces.add(this.getPoint3Ds((Element) polygons.item(i)));
            }
            Solid solidModel = new Solid();
            solidModel.setBuildingPartID(buildingPartID);
            solidModel.setFaces(faces);
            result.add(solidModel);
        }
        return result;
    }

    private List<Point3D> getPoint3Ds(Element element) {
        Element exterior = (Element) element.getElementsByTagName(TagName.GML_EXTERIOR).item(0);
        Element posList = (Element) exterior.getElementsByTagName(TagName.GML_POSLIST).item(0);
        String[] posString = posList.getTextContent().trim().split(" ");
        return ThreeDUtil.createListPoint(posString);
    }

    private List<Solid> validateSolid(List<Solid> solids) {
        List<Solid> invalid = new ArrayList<>();
        // validation solids
        for (int i = 0; i < solids.size(); i++) {
            Solid solid = solids.get(i);
            boolean isValid = this.isValidSolid(solid, solids, i);
            if (!isValid) invalid.add(solid);
        }
        return invalid;
    }

    private boolean isValidSolid(Solid solid, List<Solid> solids, int index) {
        List<List<Point3D>> otherFaces = new ArrayList<>();
        for (int i = 0; i < solids.size(); i++) {
            if (i == index) continue;
            otherFaces.addAll(solids.get(i).getFaces());
        }
        // check touch with other faces
        boolean isTouch = this.touch(solid, otherFaces);
        // check intersection with other faces
        boolean isIntersect = this.intersect(solid, otherFaces);
        return isTouch && !isIntersect;
    }

    private boolean intersect(Solid solid, List<List<Point3D>> faces) {
        // if any face of sold1 intersect with project solid (xOy, yOz, xOz) => intersect 3D
        for (List<Point3D> face1 : faces) {
            List<Point3D> face1yOz = this.projectYOZ(face1);
            List<Point3D> face1xOz = this.projectXOZ(face1);
            Geometry g1xOy = ThreeDUtil.createPolygon(face1);
            Geometry g1yOz = ThreeDUtil.createPolygon(face1yOz);
            Geometry g1xOz = ThreeDUtil.createPolygon(face1xOz);
            for (List<Point3D> face2 : solid.getFaces()) {
                Geometry g2xOy = ThreeDUtil.createPolygon(face2);
                Geometry g2yOz = ThreeDUtil.createPolygon(face1yOz);
                Geometry g2xOz = ThreeDUtil.createPolygon(face1xOz);
                if (g1xOy.intersects(g2xOy) && !g1xOy.touches(g2xOy)
                        && g1yOz.intersects(g2yOz) && !g1yOz.touches(g2yOz)
                        && g1xOz.intersects(g2xOz) && !g1xOz.touches(g2xOz)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean touch(Solid solid, List<List<Point3D>> faces) {
        // if any face of sold1 same plane any face of other solid and intersect 2D => touch 3D
        for (List<Point3D> face1 : faces) {
            double[] planeface1 = this.findPlane(face1);
            Geometry g1 = ThreeDUtil.createPolygon(face1);
            for (List<Point3D> face2 : solid.getFaces()) {
                double[] planeface2 = this.findPlane(face2);
                Geometry g2 = ThreeDUtil.createPolygon(face2);
                if (SolveEquationUtil.equalPlane(planeface1, planeface2) && g1.intersects(g2) && !g1.touches(g2))
                    return true;
            }
        }
        return false;
    }

    private double[] findPlane(List<Point3D> face) {
        double[] result = SolveEquationUtil.findPlaneEquation(face.get(0), face.get(1), face.get(2));
        if (result != null) return result;
        double[] reFind = new double[4];
        int size = face.size();
        for (int i = 1; i < size; i++) {
            Point3D point1 = face.get(i);
            for (int j = i + 1; j < size; j++) {
                if (j + 1 > size) break;
                Point3D point2 = face.get(j);
                reFind = SolveEquationUtil.findPlaneEquation(point1, point2, face.get(j + 1));
                if (reFind != null) return reFind;
            }
        }
        return reFind;
    }


    private List<Point3D> projectYOZ(List<Point3D> input) {
        return input.stream().map(p -> new Point3D(p.getY(), p.getZ(), p.getX())).collect(Collectors.toList());
    }

    private List<Point3D> projectXOZ(List<Point3D> input) {
        return input.stream().map(p -> new Point3D(p.getX(), p.getZ(), p.getY())).collect(Collectors.toList());
    }
}