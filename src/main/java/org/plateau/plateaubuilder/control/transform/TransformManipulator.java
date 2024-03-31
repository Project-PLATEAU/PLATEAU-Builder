package org.plateau.plateaubuilder.control.transform;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.plateau.plateaubuilder.geometry.GeoCoordinate;
import org.plateau.plateaubuilder.utils3d.geom.Vec3f;
import org.plateau.plateaubuilder.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * ノードに対する座標・回転・スケールを管理するクラス
 * LODSolid継承クラスで保持することでギズモによる操作が可能になります。
 */
public class TransformManipulator {
    private Node solid;

    private ObjectProperty<Point3D> location = new SimpleObjectProperty<>();
    private ObjectProperty<Point3D> rotation = new SimpleObjectProperty<>();
    private ObjectProperty<Point3D> scale = new SimpleObjectProperty<>();

    private Point3D origin;

    private Transform transformCache;

    /**
     * コンストラクタ
     * 
     * @param node 操作対象ノード
     */
    public TransformManipulator(Node node) {
        solid = node;

        this.location.set(new Point3D(0, 0, 0));
        this.rotation.set(new Point3D(0, 0, 0));
        this.scale.set(new Point3D(1, 1, 1));

        transformCache = new Translate();
    }

    /**
     * 操作対象ノードを返します。
     * 
     * @return 操作対象ノード
     */
    public Node getSolidView() {
        return solid;
    }

    /**
     * 位置情報のプロパティを返します。
     * 
     * @return 位置情報のプロパティ
     */
    public ObjectProperty<Point3D> getLocationProperty() {
        return location;
    }

    /**
     * 位置情報を返します。
     * 
     * @return 位置座標
     */
    public Point3D getLocation() {
        return location.get();
    }

    /**
     * 位置情報を更新します。
     * 
     * @param locate オフセット移動量
     */
    public void setLocation(Point3D locate) {
        this.location.set(locate);
    }
    
    /**
     * 回転情報のプロパティを返します。
     * 
     * @return 回転情報のプロパティ
     */
    public ObjectProperty<Point3D> getRotationProperty() {
        return rotation;
    }

    /**
     * 回転情報を返します。
     * 
     * @return 回転
     */
    public Point3D getRotation() {
        return rotation.get();
    }

    /**
     * 回転情報を更新します。
     * 
     * @param rotate オフセット回転量
     */
    public void setRotation(Point3D rotate) {
        this.rotation.set(rotate);
    }
    
    /**
     * スケール情報のプロパティを返します。
     * 
     * @return スケール情報のプロパティ
     */
    public ObjectProperty<Point3D> getScaleProperty() {
        return scale;
    }

    /**
     * スケール情報を返します。
     * 
     * @return スケール
     */
    public Point3D getScale() {
        return scale.get();
    }

    /**
     * スケール情報を更新します。
     * 
     * @param scale スケール
     */
    public void setScale(Point3D scale) {
        this.scale.set(scale);
    }

    /**
     * 原点座標を更新します。
     * このメソッドは、操作対象のバウンディングボックス底面中心を求め原点座標として保持します。
     */
    public void updateOrigin() {
        BoundingBox bb = (BoundingBox) solid.getBoundsInParent();
        origin = new Point3D(bb.getCenterX(), bb.getCenterY(), bb.getMinZ());
    }

    /**
     * 原点座標を更新します。
     * このメソッドは、与えた座標を原点座標として保持します。
     * 
     * @param newOrigin 操作対象の原点座標
     */
    public void updateOrigin(Point3D newOrigin) {
        origin = newOrigin;
    }

    /**
     * 原点座標を返します。
     * 
     * @return 操作対象の原点座標
     */
    public Point3D getOrigin() {
        return origin;
    }

    /**
     * 保持している座標変換の結果を返します。
     * 
     * @return 座標変換
     */
    public Transform getTransformCache() {
        return transformCache;
    }

    /**
     * 座標変換を追加します。
     * このメソッドは、保持された座標変換に与えられた座標変換を加え結合した結果を保持します。
     * 
     * @param transformDelta 座標変換
     */
    public void addTransformCache(Transform transformDelta) {
        transformCache = transformCache.createConcatenation(transformDelta);
    }

    /**
     * 頂点リストに対し現在の座標変換を適用した結果を返します。
     * 
     * @param coordinates 頂点リスト
     * @return 座標変換された頂点リスト
     */
    public List<Double> unprojectTransforms(List<Double> coordinates) {
        List<Double> ret = new ArrayList<>();

        for (int i = 0; i < coordinates.size(); i = i + 3) {
            var geoCoordinate = new GeoCoordinate(coordinates.get(i), coordinates.get(i + 1), coordinates.get(i + 2));
            
            // ワールド座標に投影
            var position = World.getActiveInstance().getGeoReference().project(geoCoordinate);

            // 座標変換情報から座標変換
            Point3D point = new Point3D(position.x, position.y, position.z);
            var pivot = getOrigin();
            Transform transform = new Translate();
            transform = transform.createConcatenation(transformCache);
            transform = transform.createConcatenation(new Scale(getScale().getX(), getScale().getY(), getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
            point = transform.transform(point);
            
            // ワールド座標から逆投影
            var geoCoordinateConvert = World.getActiveInstance().getGeoReference().unproject(new Vec3f((float)point.getX(), (float)point.getY(), (float)point.getZ()));
            
            ret.add(geoCoordinateConvert.lat);
            ret.add(geoCoordinateConvert.lon);
            ret.add(geoCoordinateConvert.alt);
        }

        return ret;
    }
    
    /**
     * 頂点バッファに対し現在の座標変換を適用した結果を返します。
     * 
     * @param vertices 頂点バッファ
     * @return 座標変換された頂点バッファ
     */
    public List<Vec3f> unprojectVertexTransforms(List<Vec3f> vertices) {
        List<Vec3f> ret = new ArrayList<>();

        for (var vertex : vertices) {
            var pivot = getOrigin();
            Transform transform = new Translate();
            transform = transform.createConcatenation(transformCache);
            transform = transform.createConcatenation(new Scale(getScale().getX(), getScale().getY(), getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
            var point = transform.transform(new Point3D(vertex.x, vertex.y, vertex.z));
            ret.add(new Vec3f((float) point.getX(), (float) point.getY(), (float) point.getZ()));
        }

        return ret;
    }
}
