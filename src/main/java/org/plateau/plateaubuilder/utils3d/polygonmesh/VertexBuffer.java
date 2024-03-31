package org.plateau.plateaubuilder.utils3d.polygonmesh;

import org.plateau.plateaubuilder.utils3d.geom.Vec3f;

import java.util.ArrayList;
import java.util.List;

/**
 * ポリゴンメッシュの頂点の配列を保持し頂点追加、削除、取得を行う機能を提供します。
 * 頂点データは内部的には{@code ArrayList<Float>}で保持され、各頂点はx, y, zの3要素で定義されます。
 */
public class VertexBuffer {
    private final List<Float> buffer = new ArrayList<>();

    /**
     * 内部保持されている頂点の生データを取得します。
     */
    public List<Float> getBuffer() {
        return buffer;
    }

    /**
     * 内部保持されている頂点の生データを配列に変換して取得します。
     */
    public float[] getBufferAsArray() {
        var result = new float[buffer.size()];
        var index = 0;
        for (var value : buffer) {
            result[index++] = value;
        }
        return result;
    }

    /**
     * 内部保持されている頂点の生データをVec3fのリストに変換して取得します。
     */
    public List<Vec3f> getVertices() {
        var result = new ArrayList<Vec3f>();
        for (int i = 0; i < getVertexCount(); ++i) {
            result.add(getVertex(i));
        }
        return result;
    }

    /**
     * {@code getVertex}関数で扱えるインデックス数を取得します。
     */
    public int getVertexCount() {
        return buffer.size() / 3;
    }

    public Vec3f getVertex(int index) {
        return new Vec3f(buffer.get(index * 3), buffer.get(index * 3 + 1), buffer.get(index * 3 + 2));
    }

    /**
     * 頂点を追加します。
     */
    public void addVertex(Vec3f vertex) {
        buffer.add(vertex.x);
        buffer.add(vertex.y);
        buffer.add(vertex.z);
    }

    /**
     * 頂点を追加します。
     */
    public void addVertices(float[] vertices) {
        for (var value : vertices) {
            buffer.add(value);
        }
    }

    /**
     * 頂点を追加します。
     */
    public void addVertices(List<Float> vertices) {
        buffer.addAll(vertices);
    }
}
