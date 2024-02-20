package org.plateau.citygmleditor.citygmleditor;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.World;
import javafx.geometry.Point3D;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.scene.Node;

public class AutoGeometryAligner {
    public static void GeometryAlign(CityModelView convertedCityModel, String id, int lod) {
        ILODSolidView lod1SolidView = null;
        ILODSolidView lodSolidView = null;
        for (var building : convertedCityModel.getChildrenUnmodifiable()) {
            if (!building.getId().equals(id))
                continue;
            var buildingView = (org.plateau.citygmleditor.citymodel.BuildingView) building;
            lod1SolidView = buildingView.getLOD1Solid();
            switch (lod) {
                case 2:
                    lodSolidView = buildingView.getLOD2Solid();
                    break;
                case 3:
                    lodSolidView = buildingView.getLOD2Solid();
                    break;
            }
        }
        if ((lod1SolidView == null) || (lodSolidView == null)) {
            return;
        }
        var lod1vertices = lod1SolidView.getVertexBuffer().getVertices();
        var lod1geometry = createJTSGeometry(lod1vertices);
        var lod2vertices = lodSolidView.getVertexBuffer().getVertices();
        var lod2geometry = createJTSGeometry(lod2vertices);
        if ((lod1geometry == null) || (lod2geometry == null))
            return;
        var geoReference = World.getActiveInstance().getGeoReference();
        var lod1point = geoReference.project(new GeoCoordinate(lod1geometry.getCentroid().getX(), lod1geometry.getCentroid().getY(), 0));
        var lod2point = geoReference.project(new GeoCoordinate(lod2geometry.getCentroid().getX(), lod2geometry.getCentroid().getY(), 0));
        var translate = new Translate(lod1point.x - lod2point.x, lod1point.y - lod2point.y);
        List<Geometry> lod2Geometries = new ArrayList<Geometry>();
        // 10度づつの回転オフセット
        for (int i = 0; i < 360/10; i++) {
            var rotate = new Rotate(i*10, lod2point.x, lod2point.y, 0, Rotate.Z_AXIS);
            Transform transform = translate.createConcatenation(rotate);
            lod2Geometries.add(createJTSGeometry(transformVertices(lod2vertices, transform)));
        }
        var index1 = findNear(lod1geometry, lod2Geometries);
        var baseangle = index1 * 10;
        var angles = new ArrayList<Integer>();
        angles.add(baseangle);
        // ±1度づつの回転オフセット
        lod2Geometries.clear();
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 10; j++) {
                var angle = (baseangle + (j * (i == 0 ? 1 : -1)) + 360) % 360;
                angles.add(angle);
                var rotate = new Rotate(angle, lod2point.x, lod2point.y, 0, Rotate.Z_AXIS);
                Transform transform = translate.createConcatenation(rotate);
                lod2Geometries.add(createJTSGeometry(transformVertices(lod2vertices, transform)));
            }
        } 
        var index2 = findNear(lod1geometry, lod2Geometries);
        var fixangle = angles.get(index2);
        
        // 反映
        var manipulator = lodSolidView.getTransformManipulator();

        var translateHeight = new Translate(0, 0, lod1SolidView.getTransformManipulator().getOrigin().getZ() - manipulator.getOrigin().getZ());
        var fixRotate = new Rotate(fixangle, lod2point.x, lod2point.y, 0, Rotate.Z_AXIS);
        
        manipulator.addTransformCache(translateHeight);
        manipulator.addTransformCache(translate);
        manipulator.addTransformCache(fixRotate);

        manipulator.updateOrigin(new Point3D(lod2point.x, lod2point.y, manipulator.getOrigin().getZ()));
        manipulator.setLocation(new Point3D(lod1point.x - lod2point.x, lod1point.y - lod2point.y, lod1SolidView.getTransformManipulator().getOrigin().getZ() - manipulator.getOrigin().getZ()));
        manipulator.setRotation(new Point3D(0.0, 0.0, fixangle));

        ((Node)lodSolidView).getTransforms().clear();
        ((Node)lodSolidView).getTransforms().add(manipulator.getTransformCache());
        lodSolidView.reflectGML();

        CityGMLEditorApp.getFeatureSellection().setSelectElement((Node) lodSolidView);
    }
    
    private static int findNear(Geometry lod1geometry, List<Geometry> lod2Geometries) {
        var overlapArea = 0.0d;
        int index = 0;
        for (int i = 0; i < lod2Geometries.size(); i++) {
            var intersection = lod1geometry.intersection(lod2Geometries.get(i));
            var overlap = intersection.getArea();
            if (overlap > overlapArea) {
                overlapArea = overlap;
                index = i;
            }
        }
        return index;
    }

    private static List<Vec3f> transformVertices(List<Vec3f> org, Transform trans){
        var ret = new ArrayList<Vec3f>();
        for (var v : org) {
            var newV = trans.transform(v.x, v.y, v.z);
            ret.add(new Vec3f((float)newV.getX(), (float)newV.getY(), (float)newV.getZ()));
        }
        return ret;
    }
    
    private static Geometry createJTSGeometry(List<Vec3f> vertices) {
        var coordinates = new ArrayList<Coordinate>();
        var geoReference = World.getActiveInstance().getGeoReference();
        for (var vertex : vertices) {
            vertex.z = 0.0f;
            var coord = geoReference.unproject(vertex);
            coordinates.add(new Coordinate(coord.lat, coord.lon));
        }
        return new ConvexHull(coordinates.toArray(new Coordinate[0]), new GeometryFactory()).getConvexHull();
    }

    private static javafx.scene.shape.Polygon createJavaFXPolygon(org.locationtech.jts.geom.Polygon jtsPolygon) {
        var geoReference = World.getActiveInstance().getGeoReference();
        javafx.scene.shape.Polygon javafxPolygon = new javafx.scene.shape.Polygon();

        // JTS Polygon の座標を取得して JavaFX Polygon に設定
        for (Coordinate coordinate : jtsPolygon.getCoordinates()) {
            var local = geoReference.project(new GeoCoordinate(coordinate.x, coordinate.y, coordinate.z));
            javafxPolygon.getPoints().addAll((double) local.x, (double) local.y);
        }

        return javafxPolygon;
    }
    
    private static org.locationtech.jts.geom.Polygon createJTSPolygon(Polygon javafxPolygon) {
        var geoReference = World.getActiveInstance().getGeoReference();
        List<Coordinate> coordinates = new ArrayList<>();
        var transform = javafxPolygon.getLocalToParentTransform();
        for (int i = 0; i < javafxPolygon.getPoints().size(); i += 2) {
            double x = javafxPolygon.getPoints().get(i);
            double y = javafxPolygon.getPoints().get(i + 1);
            Point3D point = Point3D.ZERO;
            try {
                point = transform.inverseTransform(new Point3D(x, y, 0));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            var coordinate = geoReference.unproject(new Vec3f((float)point.getX(), (float) point.getY(), (float)point.getZ()));
            coordinates.add(new Coordinate(coordinate.lat, coordinate.lon));
        }

        // Close the ring if needed
        if (coordinates.size() > 2 && !coordinates.get(0).equals2D(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(coordinates.get(0));
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        org.locationtech.jts.geom.LinearRing linearRing = geometryFactory.createLinearRing(coordinates.toArray(new Coordinate[0]));
        return geometryFactory.createPolygon(linearRing, null);
    }
}
