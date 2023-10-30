package org.plateau.citygmleditor.citymodel.factory;

import com.sun.j3d.utils.geometry.GeometryInfo;

import java.lang.reflect.Array;

public class Tessellator {
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
        var convertedIndices = new int[indices.length * 2];
        for (int i = 0; i < convertedIndices.length; i += 2) {
            convertedIndices[i] = indices[i / 2];
            // UVインデックスは0で埋める
            convertedIndices[i + 1] = indices[i / 2];
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
