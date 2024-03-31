package org.plateau.plateaubuilder.utils3d.geom.transform;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.plateau.plateaubuilder.utils3d.geom.Vec2f;
import org.plateau.plateaubuilder.utils3d.geom.Vec3f;

import java.util.ArrayList;
import java.util.List;

public class PlaneToXYPlaneTransform {
    /**
     * 3次元の頂点群をxy平面上に回転し投影します。
     * 頂点群は同一平面上に存在している必要があり、かつ最低3点以上の非共線点が存在する必要があります。
     * 投影後の上下向きは保証されません。
     */
    public static List<Vec2f> transform(List<Vec3f> points3d) {
        // 非共線点を探索
        var p1 = points3d.get(0).convertToVector3D();
        var p2 = points3d.get(1).convertToVector3D();

        // (p1, p2)から各点への距離を計算して非共線判定。0.01m以上離れていない場合共線点とする。

        var validPointFound = false;
        var p3 = Vector3D.ZERO;
        for (int i = 2; i < points3d.size(); ++i) {
            p3 = points3d.get(i).convertToVector3D();
            double distance = calculateDistanceFromLineToPoint(p1, p2, p3);
            if (distance > 0.01) {
                validPointFound = true;
                break;
            }
        }

        if (!validPointFound)
            throw new RuntimeException("No valid points found.");

        // 外積による法線ベクトルの計算
        Vector3D v1 = p2.subtract(p1);
        Vector3D v2 = p3.subtract(p1);
        Vector3D normal = v1.crossProduct(v2);

        // 回転行列を作成して平面をxy平面に合わせる
        Rotation rotation = new Rotation(Vector3D.PLUS_K, normal);

        var points2d = new ArrayList<Vec2f>();
        // 頂点群を変換
        for (var point : points3d) {
            Vector3D transformed = rotation.applyTo(point.convertToVector3D());
            points2d.add(new Vec2f((float)transformed.getX(), (float)transformed.getY()));
        }
        return points2d;
    }

    private static double calculateDistanceFromLineToPoint(Vector3D p, Vector3D q, Vector3D r) {
        Vector3D pq = q.subtract(p);
        Vector3D pr = r.subtract(p);

        Vector3D crossProduct = pq.crossProduct(pr);
        return crossProduct.getNorm() / pq.getNorm();
    }
}
