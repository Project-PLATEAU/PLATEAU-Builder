package org.plateau.plateaubuilder.utils3d.polygonmesh;

import java.util.ArrayList;
import java.util.List;

/**
 * ポリゴンメッシュの面の配列を保持しインデックス追加、削除、取得を行う機能を提供します。
 * 面データは内部的には{@code ArrayList<Integer>}で保持され、各面は3点の頂点インデックス、法線インデックス、テクスチャ座標インデックスの計9要素によって定義されます。
 */
public class FaceBuffer {
    private final List<Integer> buffer = new ArrayList<>();

    /**
     * 内部保持されている面の生データを取得します。
     */
    public List<Integer> getBuffer() {
        return buffer;
    }

    /**
     * 内部保持されている面の生データをint配列に変換して取得します。
     */
    public int[] getBufferAsArray() {
        var result = new int[buffer.size()];
        var index = 0;
        for (var value : buffer) {
            result[index++] = value;
        }
        return result;
    }

    /**
     * {@code getVertexIndex}関数と{@code getTexCoordIndex}関数で扱えるインデックス数を取得します。
     */
    public int getPointCount() {
        return buffer.size() / 3;
    }

    /**
     * 面数を取得します。
     */
    public int getFaceCount() {
        return getPointCount() / 3;
    }

    /**
     * 頂点インデックスを取得します。
     * @return 頂点インデックス
     */
    public int getVertexIndex(int index) {
        return buffer.get(index * 3);
    }

    /**
     * テクスチャ座標(UV)インデックスを取得します。
     * @return テクスチャ座標インデックス
     */
    public int getTexCoordIndex(int index) {
        return buffer.get(index * 3 + 2);
    }

    /**
     * 法線インデックスを取得します。
     * @return 法線インデックス
     */
    public int getNormalIndex(int index) {
        return buffer.get(index * 3 + 1);
    }

    /**
     * 頂点インデックスを設定します。
     */
    public void setVertexIndex(int index, int vertexIndex) {
        buffer.set(index * 3, vertexIndex);
    }

    /**
     * テクスチャ座標(UV)インデックスを設定します。
     */
    public void setTexCoordIndex(int index, int texCoordIndex) {
        buffer.set(index * 3 + 2, texCoordIndex);
    }

    /**
     * テクスチャ座標(UV)インデックスを設定します。
     */
    public void setNormalIndex(int index, int normalIndex) {
        buffer.set(index * 3 + 1, normalIndex);
    }

    /**
     * 面を追加します。
     */
    public void addFaces(int[] faces) {
        for (var value : faces) {
            buffer.add(value);
        }
    }

    /**
     * 面を追加します。
     */
    public void addFaces(List<Integer> faces) {
        buffer.addAll(faces);
    }
}
