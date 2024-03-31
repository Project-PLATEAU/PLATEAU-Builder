package org.plateau.plateaubuilder.citymodel.geometry;

import javafx.scene.Parent;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.plateaubuilder.control.transform.TransformManipulator;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.control.surfacetype.BuildingSurfaceTypeView;
import org.plateau.plateaubuilder.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.plateaubuilder.utils3d.polygonmesh.VertexBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * MeshViewを取得します。
     * @return メッシュビュー
     */
    public MeshView getMeshView();

    default public List<BoundarySurfaceView> getBoundaries() {
        return null;
    }

    default public Mesh getTotalMesh() {
        if (getMeshView() == null)
            return null;
        var mesh = (TriangleMesh)getMeshView().getMesh();
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

    /**
     * テクスチャ座標バッファを取得します。
     * @return テクスチャ座標バッファ
     */
    public TexCoordBuffer getTexCoordBuffer();

    /**
     * 
     */
    public TransformManipulator getTransformManipulator();

    public BuildingSurfaceTypeView getSurfaceTypeView();

    /**
     * GML、各頂点バッファへ情報を適用
     */
    public void reflectGML();

    /**
     * 使用しているテクスチャパス
     * @return
     */
    default public List<String> getTexturePaths() {
        var parentNode = this.getParent();
        while (parentNode != null) {
            if (parentNode instanceof CityModelView)
                break;
            parentNode = parentNode.getParent();
        }
        var cityModelView = (CityModelView)parentNode;
        var ret = new ArrayList<String>();
        for (var polygon : getPolygons()) {
            if (polygon.getSurfaceData() == null)
                continue;
            var parameterizedTexture = (org.citygml4j.model.citygml.appearance.ParameterizedTexture) polygon.getSurfaceData().getGML();
            var imagePath = java.nio.file.Paths.get(parameterizedTexture.getImageURI());
            if (imagePath.startsWith("..")) {
                imagePath = java.nio.file.Paths.get(cityModelView.getGmlPath()).getParent().resolve(imagePath);
            }
            if (!ret.contains(imagePath.toString()))
                ret.add(imagePath.toString());
        }
        return ret;
    }
}
