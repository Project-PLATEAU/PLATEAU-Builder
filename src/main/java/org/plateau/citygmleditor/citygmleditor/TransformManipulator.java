package org.plateau.citygmleditor.citygmleditor;

import java.util.ArrayList;
import java.util.List;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.World;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.transform.Translate;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;

/**
 * ギズモによる座標・回転・スケールを管理するクラス
 * 各LODSolidで保持することでギズモによる操作が可能
 */
public class TransformManipulator {
    private Node solid;

    private Point3D location;
    private Point3D rotation;
    private Point3D scale;

    private Point3D origin;

    private Transform transformCache;

    /**
     * コンストラクタ
     * @param node ギズモ操作対象ノード
     */
    public TransformManipulator(Node node) {
        solid = node;

        this.location = new Point3D(0, 0, 0);
        this.rotation = new Point3D(0, 0, 0);
        this.scale = new Point3D(1, 1, 1);

        transformCache = new Translate();
    }

    /**
     * ギズモ操作対象ノードを返す
     * @return ギズモ操作対象ノード
     */
    public Node getSolidView() {
        return solid;
    }

    /**
     * 座標情報を返す
     * @return
     */
    public Point3D getLocation() {
        return location;
    }

    /**
     * 座標情報を更新
     * @param locate
     */
    public void setLocation(Point3D locate) {
        this.location = locate;
    }
    
    /**
     * 回転情報を返す
     * @return
     */
    public Point3D getRotation() {
        return rotation;
    }

    /**
     * 回転情報を更新
     * @param rotate
     */
    public void setRotation(Point3D rotate) {
        this.rotation = rotate;
    }
    
    /**
     * スケール情報を返す
     * @return
     */
    public Point3D getScale() {
        return scale;
    }

    /**
     * スケール情報を更新
     * @param scale
     */
    public void setScale(Point3D scale) {
        this.scale = scale;
    }

    /**
     * 原点座標を更新
     */
    public void updateOrigin() {
        BoundingBox bb = (BoundingBox) solid.getBoundsInParent();
        origin = new Point3D(bb.getCenterX(), bb.getCenterY(), bb.getMinZ());
    }

    /**
     * 原点座標を返す
     * @return
     */
    public Point3D getOrigin() {
        return origin;
    }

    /**
     * 座標変換を返す
     * @return
     */
    public Transform getTransformCache() {
        return transformCache;
    }

    /**
     * 座標変換を追加
     * @param transformDelta 座標変換
     */
    public void addTransformCache(Transform transformDelta) {
        transformCache = transformCache.createConcatenation(transformDelta);
    }

    /**
     * 頂点に対し現在の座標変換を適用
     * @param coordinates
     * @return
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
     * 頂点バッファに対し現在の座標変換を適用
     * @param vertices
     * @return
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
