package org.plateaubuilder.core.citymodel.geometry;

import java.util.Arrays;
import java.util.List;

import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.editor.transform.TransformManipulator;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

import javafx.scene.Parent;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

/**
 * LODビューのインターフェースです。
 * 
 */
public interface ILODView {
    /**
     * 親ノードを取得します。
     * 
     * @return 親ノード
     */
    public Parent getParent();

    /**
     * {@code MeshView}を取得します。
     * 
     * @return {@code MeshView}
     */
    public MeshView getMeshView();

    /**
     * {@code PolygonView}の一覧を取得します。
     * 
     * @return {@code PolygonView}の一覧
     */
    public List<PolygonView> getPolygons();

    /**
     * 頂点バッファを取得します。
     * 
     * @return 頂点バッファ
     */
    public VertexBuffer getVertexBuffer();

    /**
     * テクスチャ座標バッファを取得します。
     * 
     * @return テクスチャ座標バッファ
     */
    public TexCoordBuffer getTexCoordBuffer();

    /**
     * GML、各頂点バッファへ情報を適用
     */
    public void reflectGML();

    /**
     * 
     */
    public TransformManipulator getTransformManipulator();

    /**
     * この{@code ILODView}を保持する{@code IFeatureView}を取得します。
     * 
     * @return {@code IFeatureView}
     */
    default public IFeatureView getFeatureView() {
        var parent = getParent();
        if (parent instanceof IFeatureView) {
            return (IFeatureView) parent;
        }
        return null;
    }

    /**
     * メッシュを取得します。
     * 
     * @return メッシュ
     */
    default public Mesh getTotalMesh() {
        if (getMeshView() == null)
            return null;
        var mesh = (TriangleMesh) getMeshView().getMesh();
        var outMesh = new TriangleMesh();
        outMesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        outMesh.getNormals().addAll(mesh.getNormals());
        outMesh.getPoints().addAll(mesh.getPoints());
        outMesh.getTexCoords().addAll(mesh.getTexCoords());

        for (var polygon : getPolygons()) {
            outMesh.getFaces().addAll(polygon.getFaceBuffer().getBufferAsArray());
        }

        var smooths = new int[outMesh.getFaces().size() / 9];
        Arrays.fill(smooths, 1);
        outMesh.getFaceSmoothingGroups().addAll(smooths);

        return outMesh;
    }
}
