package org.plateau.citygmleditor.citygmleditor;

import java.util.ArrayList;
import java.util.List;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.World;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.scene.Node;

public class AutoGeometryAligner {
    /**
     * LOD1モデルに合わせてLOD2/3モデルの位置を調整します。
     * 
     * @param convertedCityModel 適用する都市モデル
     * @param id 適用する建物ID
     * @param lod 調整対象のLOD
     */
    public static void GeometryAlign(CityModelView convertedCityModel, String id, int lod) {
        ILODSolidView lod1SolidView = null;
        ILODSolidView lodNSolidView = null;
        for (var building : convertedCityModel.getChildrenUnmodifiable()) {
            if (!building.getId().equals(id))
                continue;
            var buildingView = (BuildingView) building;
            lod1SolidView = buildingView.getLOD1Solid();
            switch (lod) {
                case 2:
                    lodNSolidView = buildingView.getLOD2Solid();
                    break;
                case 3:
                    lodNSolidView = buildingView.getLOD3Solid();
                    break;
            }
        }
        if ((lod1SolidView == null) || (lodNSolidView == null)) {
            return;
        }
        var lod1Vertices = lod1SolidView.getVertexBuffer().getVertices();
        var lod1Geometry = createJTSGeometry(lod1Vertices);
        var lodNVertices = lodNSolidView.getVertexBuffer().getVertices();
        var lodNgeometry = createJTSGeometry(lodNVertices);
        if ((lod1Geometry == null) || (lodNgeometry == null))
            return;
        var geoReference = World.getActiveInstance().getGeoReference();
        var lod1Point = geoReference.project(new GeoCoordinate(lod1Geometry.getCentroid().getX(), lod1Geometry.getCentroid().getY(), 0));
        var lodNPoint = geoReference.project(new GeoCoordinate(lodNgeometry.getCentroid().getX(), lodNgeometry.getCentroid().getY(), 0));
        var translate = new Translate(lod1Point.x - lodNPoint.x, lod1Point.y - lodNPoint.y);
        
        // 10度づつの回転オフセット
        List<Geometry> geometries1 = new ArrayList<Geometry>();
        for (int i = 0; i < 360/10; i++) {
            var rotate = new Rotate(i*10, lodNPoint.x, lodNPoint.y, 0, Rotate.Z_AXIS);
            Transform transform = translate.createConcatenation(rotate);
            geometries1.add(createJTSGeometry(transformVertices(lodNVertices, transform)));
        }
        var index1 = findNearGeometryIndex(lod1Geometry, geometries1);
        var baseangle = index1 * 10;

        // ±1度づつの回転オフセット
        var angles = new ArrayList<Integer>();
        angles.add(baseangle);
        List<Geometry> geometries2 = new ArrayList<Geometry>();
        geometries2.add(geometries1.get(index1));
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 10; j++) {
                var angle = (baseangle + (j * (i == 0 ? 1 : -1)) + 360) % 360;
                angles.add(angle);
                var rotate = new Rotate(angle, lodNPoint.x, lodNPoint.y, 0, Rotate.Z_AXIS);
                Transform transform = translate.createConcatenation(rotate);
                geometries2.add(createJTSGeometry(transformVertices(lodNVertices, transform)));
            }
        } 
        var index2 = findNearGeometryIndex(lod1Geometry, geometries2);
        var fixangle = angles.get(index2);
        
        // 反映
        var manipulator = lodNSolidView.getTransformManipulator();

        var translateHeight = new Translate(0, 0, lod1SolidView.getTransformManipulator().getOrigin().getZ() - manipulator.getOrigin().getZ());
        var fixRotate = new Rotate(fixangle, lodNPoint.x, lodNPoint.y, 0, Rotate.Z_AXIS);
        
        manipulator.addTransformCache(translateHeight);
        manipulator.addTransformCache(translate);
        manipulator.addTransformCache(fixRotate);

        manipulator.updateOrigin(new Point3D(lodNPoint.x, lodNPoint.y, manipulator.getOrigin().getZ()));
        manipulator.setLocation(new Point3D(lod1Point.x - lodNPoint.x, lod1Point.y - lodNPoint.y, lod1SolidView.getTransformManipulator().getOrigin().getZ() - manipulator.getOrigin().getZ()));
        manipulator.setRotation(new Point3D(0.0, 0.0, fixangle));

        ((Node)lodNSolidView).getTransforms().clear();
        ((Node)lodNSolidView).getTransforms().add(manipulator.getTransformCache());
        lodNSolidView.reflectGML();

        CityGMLEditorApp.getFeatureSellection().setSelectElement((Node) lodNSolidView);
    }
    
    /**
     * 比較ジオメトリの中から基準ジオメトリに最も一致率の高いジオメトリのインデックスを返します。
     * 
     * @param baseGeometry 基準となるジオメトリ
     * @param compareGeometries 比較するジオメトリのリスト
     * @return 最も一致率の高いジオメトリのリストのインデックス
     */
    private static int findNearGeometryIndex(Geometry baseGeometry, List<Geometry> compareGeometries) {
        var overlapArea = 0.0d;
        int index = 0;
        for (int i = 0; i < compareGeometries.size(); i++) {
            var intersection = baseGeometry.intersection(compareGeometries.get(i));
            var overlap = intersection.getArea();
            if (overlap > overlapArea) {
                overlapArea = overlap;
                index = i;
            }
        }
        return index;
    }

    /**
     * 指定された変換を使用して、与えられたリスト内のすべての頂点を変換します。
     * 
     * @param org 変換する元の頂点のリスト
     * @param trans 適用する変換
     * @return 変換された頂点のリスト
     */
    private static List<Vec3f> transformVertices(List<Vec3f> org, Transform trans) {
        var ret = new ArrayList<Vec3f>();
        for (var v : org) {
            var newV = trans.transform(v.x, v.y, v.z);
            ret.add(new Vec3f((float) newV.getX(), (float) newV.getY(), (float) newV.getZ()));
        }
        return ret;
    }
    
    /**
     * 指定された頂点リストを使用して、JTSのジオメトリを作成します。
     * このメソッドは、指定された頂点を使用して凸包を作成し、その凸包のジオメトリを返します。
     * 頂点の z 座標は無視され、平面上の座標のみが考慮されます。
     * 
     * @param vertices 変換に使用される頂点のリスト
     * @return 指定された頂点から作成されたジオメトリ
     */
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
}
