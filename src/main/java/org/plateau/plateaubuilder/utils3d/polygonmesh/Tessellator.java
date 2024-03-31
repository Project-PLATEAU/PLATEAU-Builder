package org.plateau.plateaubuilder.utils3d.polygonmesh;

import com.sun.j3d.utils.geometry.GeometryInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Tessellator {
    /**
     * 輪郭点からポリゴンメッシュを生成します。
     * GMLの各輪郭は始点と終点が重複しているため、事前に終点を削除してからこの関数に入力してください。
     * @param exterior 外輪郭
     * @param interiors 内輪郭(空洞)の一覧
     * @param outVertexBuffer tessellate処理で生成されたメッシュの頂点
     * @param outFaceBuffer tessellate処理で生成されたメッシュの面情報
     */
    public static void tessellate(
            VertexBuffer exterior, List<VertexBuffer> interiors,
            VertexBuffer outVertexBuffer, FaceBuffer outFaceBuffer) {
        if (interiors == null || interiors.isEmpty()) {
            var faces = tessellate(
                    exterior.getBufferAsArray(),
                    null,
                    3);
            outFaceBuffer.addFaces(faces);

            if (outVertexBuffer != null)
                outVertexBuffer.addVertices(exterior.getBuffer());

            return;
        }

        var interiorArrays = new ArrayList<float[]>();
        for (var interior : interiors) {
            interiorArrays.add(interior.getBufferAsArray());
        }
        var faces = tessellate(
                exterior.getBufferAsArray(),
                interiorArrays,
                3);
        outFaceBuffer.addFaces(faces);

        if (outVertexBuffer == null)
            return;

        outVertexBuffer.addVertices(exterior.getBuffer());
        for (var interior : interiors) {
            outVertexBuffer.addVertices(interior.getBuffer());
        }
    }

    private static int[] tessellate(float[] exterior, List<float[]> interiors, int dim) {
        if (interiors == null || interiors.isEmpty()) {
            var vertices = new float[exterior.length];
            System.arraycopy(exterior, 0, vertices, 0, vertices.length);

            GeometryInfo ginfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
            ginfo.setCoordinates(vertices);
            ginfo.setContourCounts(new int[]{1});
            ginfo.setStripCounts(new int[]{vertices.length / 3});

            // Triangulate実行
            ginfo.convertToIndexedTriangles();

            var indices = ginfo.getCoordinateIndices();
            return convertToSubMeshIndices(indices);
        }

        int interiorArraySize = 0;
        for (var interior : interiors) {
            interiorArraySize += interior.length;
        }

        var vertices = new float[exterior.length + interiorArraySize];
        System.arraycopy(exterior, 0, vertices, 0, exterior.length);

        int arrayDestPosition = exterior.length;
        for (var interior : interiors) {
            System.arraycopy(interior, 0, vertices, arrayDestPosition, interior.length);
            arrayDestPosition += interior.length;
        }

        GeometryInfo ginfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        ginfo.setCoordinates(vertices);
        ginfo.setContourCounts(new int[]{interiors.size() + 1});

        var stripCounts = new int[interiors.size() + 1];
        stripCounts[0] = exterior.length / 3;
        for (int i = 0; i < interiors.size(); ++i) {
            stripCounts[i + 1] = interiors.get(i).length / 3;
        }
        ginfo.setStripCounts(stripCounts);

        // Triangulate実行
        ginfo.convertToIndexedTriangles();

        var indices = ginfo.getCoordinateIndices();
        return convertToSubMeshIndices(indices);
    }

    private static int[] convertToSubMeshIndices(int[] indices) {
        var convertedIndices = new int[indices.length * 3];
        for (int i = 0; i < convertedIndices.length; i += 3) {
            // 頂点インデックス
            convertedIndices[i] = indices[i / 3];
            // 法線は各面に固有なため法線インデックスは面ごとに設定
            convertedIndices[i + 1] = i / 3;
            // UVインデックスは頂点インデックスと同じ
            convertedIndices[i + 2] = indices[i / 3];
        }
        return convertedIndices;
    }

    private static <T> T[] concat(final T[] array1, final T... array2) {
        final Class<?> type1 = array1.getClass().getComponentType();
        final T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }
}
