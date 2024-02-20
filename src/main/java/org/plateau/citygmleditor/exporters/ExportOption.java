package org.plateau.citygmleditor.exporters;

import org.plateau.citygmleditor.citygmleditor.AxisEnum;
import org.plateau.citygmleditor.utils3d.geom.Vec3d;

/**
 * ExportOptionクラスは、エクスポートオプションを表すためのクラスです。
 */
public class ExportOption {
    private Vec3d offset;

    private AxisEnum axisEast;

    private AxisEnum axisTop;

    /**
     * ExportOptionクラスのデフォルトコンストラクタです。
     */
    public ExportOption() {
    }

    /**
     * ExportOptionクラスのコンストラクタです。
     * 
     * @param offset オフセットの座標
     * @param axisEast 東軸の方向
     * @param axisTop 上軸の方向
     */
    public ExportOption(Vec3d offset, AxisEnum axisEast, AxisEnum axisTop) {
        this.offset = offset;
        this.axisEast = axisEast;
        this.axisTop = axisTop;
    }

    /**
     * オフセットの座標を取得します。
     * 
     * @return オフセットの座標
     */
    public Vec3d getOffset() {
        return offset;
    }

    /**
     * 東軸の方向を取得します。
     * 
     * @return 東軸の方向
     */
    public AxisEnum getAxisEast() {
        return axisEast;
    }

    /**
     * 上軸の方向を取得します。
     * 
     * @return 上軸の方向
     */
    public AxisEnum getAxisTop() {
        return axisTop;
    }
}
