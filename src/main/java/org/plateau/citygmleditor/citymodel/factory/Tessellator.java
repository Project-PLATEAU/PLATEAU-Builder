package org.plateau.citygmleditor.citymodel.factory;

import com.sun.j3d.utils.geometry.GeometryInfo;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;

import java.lang.reflect.Array;

public class Tessellator {
    public static void tessellate(VertexBuffer exterior, VertexBuffer interior, FaceBuffer outFaceBuffer) {
        var faces = tessellate(
                exterior.getBufferAsArray(),
                interior == null ? null : interior.getBufferAsArray(),
                3);
        outFaceBuffer.addFaces(faces);
    }

    public static int[] tessellate(float[] exterior, float[] interior, int dim) {
        if (interior == null) {
            // 最後の頂点は重複しているので削除
            var vertices = new float[exterior.length - 1];
            System.arraycopy(exterior, 0, vertices, 0, exterior.length - 1);

            GeometryInfo ginfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
            ginfo.setCoordinates(vertices);
            ginfo.setContourCounts(new int[]{1});
            ginfo.setStripCounts(new int[]{vertices.length / 3});

            // Triangulate実行
            ginfo.convertToIndexedTriangles();

            var indices = ginfo.getCoordinateIndices();
            return convertToSubMeshIndices(indices);
        }

        var vertices = new float[exterior.length + interior.length];
        System.arraycopy(exterior, 0, vertices, 0, exterior.length);
        System.arraycopy(interior, 0, vertices, exterior.length, interior.length);

        GeometryInfo ginfo = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
        ginfo.setCoordinates(vertices);
        ginfo.setContourCounts(new int[]{2});
        ginfo.setStripCounts(new int[]{exterior.length / 3, interior.length / 3});

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
