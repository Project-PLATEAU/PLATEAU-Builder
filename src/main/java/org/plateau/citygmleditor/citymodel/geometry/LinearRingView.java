package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.plateau.citygmleditor.citymodel.GMLObjectView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

public class LinearRingView extends GMLObjectView<LinearRing> {
    private VertexBuffer sharedVertexBuffer;
    private TexCoordBuffer sharedTexCoordBuffer;
    private List<Integer> vertexIndices = new ArrayList<>();
    private int texCoordOffset;

    private SurfaceDataView surfaceData;

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
     * 輪郭点を取得します。ここで取得される点は始点と終点が重複しています。
     */
    public VertexBuffer getRing() {
        var vertexBuffer = new VertexBuffer();
        for (var index : vertexIndices) {
            vertexBuffer.addVertex(sharedVertexBuffer.getVertex(index));
        }
        return vertexBuffer;
    }

    public TexCoordBuffer getTexCoords() {
        var texCoordBuffer = new TexCoordBuffer();
        for (var index : vertexIndices) {
            texCoordBuffer.addTexCoord(sharedTexCoordBuffer.getTexCoord(index), true);
        }
        return texCoordBuffer;
    }

    public List<Integer> getVertexIndices() {
        return vertexIndices;
    }

    public SurfaceDataView getSurfaceData() {
        return surfaceData;
    }

    public void setSurfaceData(SurfaceDataView surfaceData) {
        this.surfaceData = surfaceData;
    }
}
