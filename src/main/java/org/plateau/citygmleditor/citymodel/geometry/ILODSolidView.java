package org.plateau.citygmleditor.citymodel.geometry;

import java.util.ArrayList;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;

import javafx.scene.Parent;
import org.plateau.citygmleditor.citygmleditor.TransformManipulator;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;

/**
 * LODSolidのインターフェースを表します。
 * これを実装するSolidクラスではSolidの頂点とテクスチャ座標情報が保持され、面情報は各Polygonが保持します。
 */
public interface ILODSolidView {
    /**
     * AbstractSolidを取得します。
     * @return AbstractSolid
     */
    public AbstractSolid getAbstractSolid();

    /**
     * 親ノードを取得します。
     * @return 親ノード
     */
    public Parent getParent();

    /**
     * {@code PolygonView}の一覧を取得します。
     * @return {@code PolygonView}の一覧
     */
    public ArrayList<PolygonView> getPolygons();

    /**
     * 頂点バッファを取得します。
     * @return 頂点バッファ
     */
    public VertexBuffer getVertexBuffer();

    /**
     * テクスチャ座標バッファを取得します。
     * @return テクスチャ座標バッファ
     */
    public TexCoordBuffer getTexCoordBuffer();

    /**
     * 
     */
    public TransformManipulator getTransformManipulator();

    /**
     * GML、各頂点バッファへ情報を適用
     */
    public void refrectGML();
}
