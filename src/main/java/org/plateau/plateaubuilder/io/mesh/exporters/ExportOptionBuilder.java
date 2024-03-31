package org.plateau.plateaubuilder.io.mesh.exporters;

import org.plateau.plateaubuilder.io.mesh.AxisEnum;
import org.plateau.plateaubuilder.utils3d.geom.Vec3d;

/**
 * ExportOptionBuilderクラスは、ExportOptionを構築するためのビルダーパターンを提供します。
 */
public class ExportOptionBuilder {
    private Vec3d offset;
    private AxisEnum axisEast;
    private AxisEnum axisTop;

    /**
     * ExportOptionBuilderクラスの新しいインスタンスを作成します。
     */
    public ExportOptionBuilder() {
    }

    /**
     * オフセットを設定します。
     * 
     * @param offset オフセット
     * @return ExportOptionBuilderのインスタンス
     */
    public ExportOptionBuilder offset(Vec3d offset) {
        this.offset = offset;
        return this;
    }

    /**
     * 東軸を設定します。
     * 
     * @param axisEast 東軸
     * @return ExportOptionBuilderのインスタンス
     */
    public ExportOptionBuilder axisEast(AxisEnum axisEast) {
        this.axisEast = axisEast;
        return this;
    }

    /**
     * 上軸を設定します。
     * 
     * @param axisTop 上軸
     * @return ExportOptionBuilderのインスタンス
     */
    public ExportOptionBuilder axisTop(AxisEnum axisTop) {
        this.axisTop = axisTop;
        return this;
    }

    /**
     * ExportOptionのインスタンスを構築します。
     * 
     * @return ExportOptionのインスタンス
     */
    public ExportOption build() {
        return new ExportOption(offset, axisEast, axisTop);
    }
}
