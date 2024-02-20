package org.plateau.citygmleditor.converters;

import org.plateau.citygmleditor.citygmleditor.AxisEnum;
import org.plateau.citygmleditor.utils3d.geom.Vec3d;

/**
 * ConvertOptionクラスは、変換オプションを表すためのクラスです。
 */
public class ConvertOption {
    public static final double DEFAULT_WALL_THRESHOLD = 80;

    private double wallThreshold;

    private boolean useGeoReference;

    private Vec3d offset;

    private AxisEnum axisEast;

    private AxisEnum axisTop;

    /**
     * ConvertOptionクラスのデフォルトコンストラクタです。
     */
    public ConvertOption() {
    }

    /**
     * ConvertOptionクラスの新しいインスタンスを初期化します。
     * 
     * @param wallThreshold 面検出角度の閾値
     * @param useGeoReference ジオリファレンスを使用するかどうか
     * @param offset オフセット
     * @param axisEast 東軸
     * @param axisTop 上軸
     */
    public ConvertOption(double wallThreshold, boolean useGeoReference, Vec3d offset, AxisEnum axisEast, AxisEnum axisTop) {
        this.wallThreshold = wallThreshold;
        this.useGeoReference = useGeoReference;
        this.offset = offset;
        this.axisEast = axisEast;
        this.axisTop = axisTop;
    }

    /**
     * 面検出角度の閾値を取得します。
     * 
     * @return 面検出角度の閾値
     */
    public double getWallThreshold() {
        return wallThreshold;
    }

    /**
     * ジオリファレンスを使用するかどうかを取得します。
     * 
     * @return ジオリファレンスを使用するかどうか
     */
    public boolean isUseGeoReference() {
        return useGeoReference;
    }

    /**
     * オフセットを取得します。
     * 
     * @return オフセット
     */
    public Vec3d getOffset() {
        return offset;
    }

    /**
     * 東軸を取得します。
     * 
     * @return 東軸
     */
    public AxisEnum getAxisEast() {
        return axisEast;
    }

    /**
     * 上軸を取得します。
     * 
     * @return 上軸
     */
    public AxisEnum getAxisUp() {
        return axisTop;
    }
}
