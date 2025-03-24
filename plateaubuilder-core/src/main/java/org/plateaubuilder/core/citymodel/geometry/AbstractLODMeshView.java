package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.gml.base.AbstractGML;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.transform.TransformManipulator;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

import javafx.scene.Parent;
import javafx.scene.shape.MeshView;

/**
 * 抽象的なLODメッシュビューの基底クラスです。
 * 
 * @param <TGML>  AbstractGMLを継承したクラスの型
 * @param <TMesh> MeshViewを継承したクラスの型
 */
abstract public class AbstractLODMeshView<TGML extends AbstractGML, TMesh extends MeshView> extends Parent {
    private final TGML gmlObject;
    private final int lod;
    private VertexBuffer vertexBuffer;
    private final TexCoordBuffer texCoordBuffer;
    private final List<MeshView> meshViews = new ArrayList<>();
    private final TMesh surfaceTypeView;
    private TransformManipulator transformManipulator = new TransformManipulator(this);

    /**
     * AbstractLODMeshViewクラスの新しいインスタンスを初期化します。
     * 
     * @param gmlObject      メッシュビューに関連付けられたGMLオブジェクト
     * @param lod            メッシュのLOD
     * @param vertexBuffer   メッシュの頂点バッファ
     * @param texCoordBuffer メッシュのテクスチャ座標バッファ
     */
    public AbstractLODMeshView(TGML gmlObject, int lod, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.lod = lod;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
        this.surfaceTypeView = createSurfaceTypeView();

        if (this.surfaceTypeView != null) {
            toggleSurfaceView(Editor.getCityModelViewMode().isSurfaceViewMode());

            Editor.getCityModelViewMode().isSurfaceViewModeProperty().addListener((observable, oldValue, newValue) -> {
                toggleSurfaceView(newValue);
            });
        }
    }

    /**
     * メッシュビューに関連付けられたGMLオブジェクトを取得します。
     * 
     * @return メッシュビューに関連付けられたGMLオブジェクト
     */
    public TGML getGmlObject() {
        return gmlObject;
    }

    /**
     * メッシュのLODを取得します。
     * 
     * @return メッシュのLOD
     */
    public int getLOD() {
        return lod;
    }

    /**
     * メッシュの頂点バッファを取得します。
     * 
     * @return メッシュの頂点バッファ
     */
    public VertexBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    /**
     * メッシュの頂点バッファを設定します。
     * 
     * @param vertexBuffer メッシュの頂点バッファ
     */
    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    /**
     * メッシュのテクスチャ座標バッファを取得します。
     * 
     * @return メッシュのテクスチャ座標バッファ
     */
    public TexCoordBuffer getTexCoordBuffer() {
        return this.texCoordBuffer;
    }

    /**
     * メッシュビューを取得します。
     * 
     * @return メッシュビュー
     */
    public MeshView getMeshView() {
        return meshViews.isEmpty() ? null : meshViews.get(0);
    }

    /**
     * メッシュビューを追加します。
     * 
     * @param meshView 追加するメッシュビュー
     */
    public void addMeshView(MeshView meshView) {
        getChildren().add(meshView);
        meshViews.add(meshView);
    }

    /**
     * サーフェスタイプのメッシュビューを取得します。
     * 
     * @return サーフェスタイプのメッシュビュー
     */
    public TMesh getSurfaceTypeView() {
        return surfaceTypeView;
    }

    /**
     * TransformManipulatorを取得します。
     * 
     * @return TransformManipulator
     */
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    /**
     * 変更内容をGMLへ反映します。
     */
    public void reflectGML() {
        var transformManipulator = getTransformManipulator();
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();
            polygon.getExteriorRing().getGML().getPosList().setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();
                polygon.getExteriorRing().getGML().getPosList().setValue(transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertexBuffer = new VertexBuffer();
        var vertices = transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }

    private void toggleSurfaceView(boolean isVisible) {
        for (var meshView : meshViews) {
            meshView.setVisible(!isVisible);
        }
        surfaceTypeView.setVisible(isVisible);
    }

    /**
     * サーフェスタイプのメッシュビューを作成します。
     * 
     * @return サーフェスタイプのメッシュビュー
     */
    abstract protected TMesh createSurfaceTypeView();

    /**
     * メッシュビューに関連付けられたPolygonViewのリストを取得します。
     * 
     * @return メッシュビューに関連付けられたPolygonViewのリスト
     */
    abstract public List<? extends PolygonView> getPolygons();
}
