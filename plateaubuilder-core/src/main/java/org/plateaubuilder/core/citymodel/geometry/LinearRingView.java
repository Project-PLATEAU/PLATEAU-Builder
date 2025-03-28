package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.plateaubuilder.core.citymodel.GMLView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * GMLでの輪郭形状(LinearRing)の可視化用オブジェクトを表します。
 */
public class LinearRingView extends GMLView<LinearRing> {
    private VertexBuffer sharedVertexBuffer;
    private TexCoordBuffer sharedTexCoordBuffer;
    private List<Integer> vertexIndices = new ArrayList<>();
    private int texCoordOffset;

    private SurfaceDataView surfaceData;

    private List<Double> originCoords;

    public LinearRingView(LinearRing original, VertexBuffer sharedVertexBuffer,
                          TexCoordBuffer sharedTexCoordBuffer, List<Integer> vertexIndices,
                          int texCoordOffset) {
        super(original);
        this.sharedVertexBuffer = sharedVertexBuffer;
        this.sharedTexCoordBuffer = sharedTexCoordBuffer;
        this.vertexIndices = vertexIndices;
        this.texCoordOffset = texCoordOffset;
    }

    /**
     * 輪郭点を取得します。ここで取得される点はGMLの輪郭点から終点が削除されています。
     */
    public VertexBuffer getRing() {
        var vertexBuffer = new VertexBuffer();
        for (var index : vertexIndices) {
            vertexBuffer.addVertex(sharedVertexBuffer.getVertex(index));
        }
        return vertexBuffer;
    }

    /**
     * テクスチャ座標を取得します。
     */
    public TexCoordBuffer getTexCoords() {
        var texCoordBuffer = new TexCoordBuffer();
        for (var index : vertexIndices) {
            texCoordBuffer.addTexCoord(sharedTexCoordBuffer.getTexCoord(index), true);
        }
        return texCoordBuffer;
    }

    /**
     * 輪郭点を{@code sharedVertexBuffer}内での頂点インデックスとして取得します。
     * @return 各要素iに対し輪郭点は{@code sharedVertexBuffer.getVertex(i)}で取得できます。
     */
    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public SurfaceDataView getSurfaceData() {
        return surfaceData;
    }

    public void setSurfaceData(SurfaceDataView surfaceData) {
        this.surfaceData = surfaceData;
    }

    public List<Double> getOriginCoords() {
        if(originCoords != null)
            return originCoords;
        
        return originCoords = new ArrayList<>(getGML().getPosList().toList3d());
    }
}
