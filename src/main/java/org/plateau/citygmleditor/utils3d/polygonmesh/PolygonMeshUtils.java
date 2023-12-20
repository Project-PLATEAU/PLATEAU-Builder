package org.plateau.citygmleditor.utils3d.polygonmesh;

import java.util.HashMap;

/**
 * ポリゴンメッシュを扱う汎用関数を提供します。
 */
public class PolygonMeshUtils {
    /**
     * ポリゴンメッシュ内の距離が0.01mより近い頂点を接合します。重複する頂点を削減し最適化するのに使用してください。
     * @param vertexBuffer 入力頂点バッファ
     * @param faceBuffer 入力面バッファ
     * @param outVertexBuffer 出力頂点バッファ。空のバッファが渡されることを想定しています。
     * @param outFaceBuffer 出力面バッファ。空のバッファが渡されることを想定しています。
     */
    public static void WeldVertices(VertexBuffer vertexBuffer, FaceBuffer faceBuffer,
                                    VertexBuffer outVertexBuffer, FaceBuffer outFaceBuffer) {
        WeldVertices(vertexBuffer, faceBuffer, outVertexBuffer, outFaceBuffer, 0.01f);
    }

    /**
     * ポリゴンメッシュ内の距離が近い頂点を接合します。重複する頂点を削減し最適化するのに使用してください。
     * @param vertexBuffer 入力頂点バッファ
     * @param faceBuffer 入力面バッファ
     * @param outVertexBuffer 出力頂点バッファ。空のバッファが渡されることを想定しています。
     * @param outFaceBuffer 出力面バッファ。空のバッファが渡されることを想定しています。
     * @param weldDistance 接合する頂点の最大距離
     */
    public static void WeldVertices(VertexBuffer vertexBuffer, FaceBuffer faceBuffer,
                                    VertexBuffer outVertexBuffer, FaceBuffer outFaceBuffer,
                                    float weldDistance) {
        // TODO: Octree等使って高速化

        // 接合前のインデックスから接合後のインデックスへのマップ
        var indexMap = new HashMap<Integer, Integer>();

        for (int i = 0; i < vertexBuffer.getVertexCount(); ++i) {
            var vertex = vertexBuffer.getVertex(i);

            // 重複する頂点がないかチェック
            boolean isWelded = false;
            for (int j = 0; j < outVertexBuffer.getVertexCount(); ++j) {
                var otherVertex = outVertexBuffer.getVertex(j);

                // 重複する場合は出力先に頂点追加せずインデックス情報を保持
                if (vertex.distance(otherVertex) <= weldDistance) {
                    isWelded = true;
                    indexMap.put(i, j);
                    break;
                }
            }
            if (!isWelded) {
                indexMap.put(i, outVertexBuffer.getVertexCount());
                outVertexBuffer.addVertex(vertex);
            }
        }

        for (int i = 0; i < faceBuffer.getIndexCount(); ++i) {
            var originalVertexIndex = faceBuffer.getVertexIndex(i);
            if (!indexMap.containsKey(originalVertexIndex)) {
                System.err.print("Invalid index.");
                continue;
            }

            outFaceBuffer.getBuffer().add(indexMap.get(originalVertexIndex));
            outFaceBuffer.getBuffer().add(faceBuffer.getTexCoordIndex(i));
        }
    }
}
