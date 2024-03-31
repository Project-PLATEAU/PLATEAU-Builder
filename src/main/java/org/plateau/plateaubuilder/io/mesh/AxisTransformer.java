package org.plateau.plateaubuilder.io.mesh;

import org.plateau.plateaubuilder.utils3d.geom.Vec3f;

/**
 * AxisTransformerは、軸の方角を考慮して変換を行うクラスです。
 */
public class AxisTransformer {
    private AxisDirection source;

    private AxisDirection dest;

    /**
     * AxisTransformerのコンストラクタです。
     * 
     * @param source 変換元の軸の方角
     * @param dest 変換先の軸の方角
     */
    public AxisTransformer(AxisDirection source, AxisDirection dest) {
        this.source = source;
        this.dest = dest;
    }

    /**
     * 指定された座標を変換します。
     * 
     * @param x X座標
     * @param y Y座標
     * @param z Z座標
     * @return 変換後の座標
     */
    public Vec3f transform(float x, float y, float z) {
        var axisX = this.source.getAxis(this.dest.getX());
        var axisY = this.source.getAxis(this.dest.getY());
        var axisZ = this.source.getAxis(this.dest.getZ());

        return new Vec3f(
            getAxisValue(axisX, x, y, z),
            getAxisValue(axisY, x, y, z),
            getAxisValue(axisZ, x, y, z)
        );
    }

    private float getAxisValue(AxisEnum axis, float x, float y, float z) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
            case NEGATIVE_X:
                return -x;
            case NEGATIVE_Y:
                return -y;
            case NEGATIVE_Z:
                return -z;
            default:
                throw new IllegalArgumentException("Invalid axis");
        }
    }
}
