package org.plateaubuilder.core.io.mesh.converters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.plateaubuilder.core.io.mesh.AxisTransformer;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;
import org.plateaubuilder.core.utils3d.geom.Vec3f;

/**
 * 抽象的な3D形式のハンドラを表すクラスです。
 */
abstract public class Abstract3DFormatHandler {

    private AxisTransformer _axisTransformer;

    /**
     * Abstract3DFormatHandler クラスの新しいインスタンスを初期化します。
     * 
     * @param axisTransformer 座標変換を行うための AxisTransformer オブジェクト
     */
    public Abstract3DFormatHandler(AxisTransformer axisTransformer) {
        _axisTransformer = axisTransformer;
    }

    /**
     * 座標軸を変換して頂点を変換します。
     * 
     * @param x X座標
     * @param y Y座標
     * @param z Z座標
     * @return 変換された頂点の座標
     */
    protected Vec3f convertVertexAxis(float x, float y, float z) {
        return _axisTransformer.transform(x, y, z);
    }

    /**
     * ファイルのURLを指定して初期化を行います。
     * 
     * @param fileUrl ファイルのURL
     * @throws IOException        入出力エラーが発生した場合
     * @throws URISyntaxException URIの構文が正しくない場合
     */
    abstract public void initialize(String fileUrl) throws IOException, URISyntaxException;

    /**
     * 三角形モデルのマップを作成します。
     * 
     * @return 作成された三角形モデルのマップ
     */
    abstract public Map<String, List<TriangleModel>> createTriangleModelsMap();

    /**
     * 表面データを作成します。
     * 
     * @return 作成された表面データのマップ
     * @throws IOException        入出力エラーが発生した場合
     * @throws URISyntaxException URIの構文が正しくない場合
     */
    abstract public Map<String, AbstractSurfaceData> createSurfaceData() throws IOException, URISyntaxException;
}
