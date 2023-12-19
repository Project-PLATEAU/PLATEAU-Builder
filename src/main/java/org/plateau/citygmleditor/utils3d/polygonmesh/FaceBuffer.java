package org.plateau.citygmleditor.utils3d.polygonmesh;

import java.util.ArrayList;

/**
 * ポリゴンメッシュの面の配列を保持しインデックス追加、削除、取得を行う機能を提供します。
 * 面データは内部的には{@code ArrayList<Integer>}で保持され、各面は3点の頂点インデックスとテクスチャ座標インデックスの計6要素によって定義されます。
 */
public class FaceBuffer {
    private final ArrayList<Integer> buffer = new ArrayList<>();

    /**
     * 内部保持されている面の生データを取得します。
     */
    public ArrayList<Integer> getBuffer() {
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
    public int getIndexCount() {
        return buffer.size() / 2;
    }

    /**
     * 頂点インデックスを取得します。
     * @return 頂点インデックス
     */
    public int getVertexIndex(int index) {
        return buffer.get(index * 2);
    }

    /**
     * テクスチャ座標(UV)インデックスを取得します。
     * @return テクスチャ座標インデックス
     */
    public int getTexCoordIndex(int index) {
        return buffer.get(index * 2 + 1);
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
    public void addFaces(ArrayList<Integer> faces) {
        buffer.addAll(faces);
    }
}
