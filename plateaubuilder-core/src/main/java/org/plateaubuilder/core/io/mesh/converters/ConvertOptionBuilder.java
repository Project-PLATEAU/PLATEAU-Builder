package org.plateaubuilder.core.io.mesh.converters;

import org.plateaubuilder.core.io.mesh.AxisEnum;
import org.plateaubuilder.core.utils3d.geom.Vec3d;

/**
 * ConvertOptionBuilderクラスは、ConvertOptionを構築するためのビルダーパターンを提供します。
 */
public class ConvertOptionBuilder {
    private double wallThreshold = ConvertOption.DEFAULT_WALL_THRESHOLD;

    private boolean useGeoReference = false;

    private Vec3d offset = new Vec3d();

    private AxisEnum axisEast;

    private AxisEnum axisTop;

    /**
     * ConvertOptionBuilderクラスのコンストラクタです。
     */
    public ConvertOptionBuilder() {
    }

    /**
     * 面検出角度の閾値を設定します。
     * @param wallThreshold 壁の閾値
     * @return ConvertOptionBuilderのインスタンス
     */
    public ConvertOptionBuilder wallThreshold(double wallThreshold) {
        this.wallThreshold = wallThreshold;
        return this;
    }

    /**
     * ジオリファレンスを使用するかどうかを設定します。
     * @param useGeoReference ジオリファレンスを使用するかどうか
     * @return ConvertOptionBuilderのインスタンス
     */
    public ConvertOptionBuilder useGeoReference(boolean useGeoReference) {
        this.useGeoReference = useGeoReference;
        return this;
    }

    /**
     * オフセットを設定します。
     * @param offset オフセット
     * @return ConvertOptionBuilderのインスタンス
     */
    public ConvertOptionBuilder offset(Vec3d offset) {
        this.offset = offset;
        return this;
    }

    /**
     * 東軸を設定します。
     * @param axisEast 東軸
     * @return ConvertOptionBuilderのインスタンス
     */
    public ConvertOptionBuilder axisEast(AxisEnum axisEast) {
        this.axisEast = axisEast;
        return this;
    }

    /**
     * 上軸を設定します。
     * @param axisTop 上軸
     * @return ConvertOptionBuilderのインスタンス
     */
    public ConvertOptionBuilder axisUp(AxisEnum axisTop) {
        this.axisTop = axisTop;
        return this;
    }

    /**
     * ConvertOptionのインスタンスを構築します。
     * @return ConvertOptionのインスタンス
     */
    public ConvertOption build() {
        return new ConvertOption(wallThreshold, useGeoReference, offset, axisEast, axisTop);
    }
}
