package org.plateaubuilder.core.io.mesh.converters.model;

import javafx.collections.ObservableFloatArray;
import javafx.scene.shape.ObservableFaceArray;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class TriangleModel {
    private Point3f[] _vertices = new Point3f[3];
    private Point2f[] _uvs = new Point2f[3];
    private Vector3f _normal = new Vector3f();

    private TriangleModel() {
        _vertices[0] = new Point3f(0, 0, 0);
        _vertices[1] = new Point3f(1, 0, 0);
        _vertices[2] = new Point3f(0, 1, 0);

        initNormal();
    }

    public TriangleModel(int[] faces, int startIndex, float[] vertices, float[] uvs) {
        for (var i = 0; i < 3; i++) {
            var vertexIndex = faces[startIndex + i * 2];
            var uvIndex = faces[startIndex + i * 2 + 1];
            _vertices[i] = new Point3f(vertices[vertexIndex * 3], vertices[vertexIndex * 3 + 1], vertices[vertexIndex * 3 + 2]);
            if (uvs != null) {
                _uvs[i] = new Point2f(uvs[uvIndex * 2], uvs[uvIndex * 2 + 1]);
            } else {
                _uvs[i] = new Point2f(0, 1);
            }
        }
        initNormal();
    }

    public TriangleModel(ObservableFaceArray faces, int startIndex, ObservableFloatArray vertices, ObservableFloatArray uvs, boolean invertFace) {
        for (var i = 0; i < 3; i++) {
            var vertexIndex = faces.get(startIndex + (invertFace ? 2 - i : i) * 2);
            var uvIndex = faces.get(startIndex + (invertFace ? 2 - i : i) * 2 + 1);
            _vertices[i] = new Point3f(vertices.get(vertexIndex * 3), vertices.get(vertexIndex * 3 + 1), vertices.get(vertexIndex * 3 + 2));
            _uvs[i] = new Point2f(uvs.get(uvIndex * 2), 1 - uvs.get(uvIndex * 2 + 1));
        }

        initNormal();
    }

    private void initNormal() {
        Vector3f first = new Vector3f();
        first.sub(_vertices[2], _vertices[0]);
        Vector3f second = new Vector3f();
        second.sub(_vertices[2], _vertices[1]);
        _normal.cross(first, second);
        _normal.normalize();
    }

    public boolean isValid() {
        return !_vertices[0].equals(_vertices[1]) && !_vertices[1].equals(_vertices[2]) && !_vertices[2].equals(_vertices[0])
            && !Float.isNaN(_normal.x) && !Float.isNaN(_normal.y) && !Float.isNaN(_normal.z);
    }

    public Point3f[] getVertices() {
        return _vertices;
    }

    public Point2f[] getUVs() {
        return _uvs;
    }

    public Vector3f getNormal() {
        return _normal;
    }

    public Point3f getVertex(int index) {
        return _vertices[index];
    }

    public Vector3f getVertexAsVector3f(int index) {
        var p = _vertices[index];
        return new Vector3f(p.x, p.y, p.z);
    }

    public Point2f getUV(int index) {
        return _uvs[index];
    }

    public float getPlaneDistance(Vector3f a) {
        Vector3f pa = new Vector3f();
        pa.sub(a, getVertexAsVector3f(0));
        return Math.abs(_normal.dot(pa));
    }

    public boolean isSamePlane(TriangleModel triangleModel) {
        var d1 = getPlaneDistance(triangleModel.getVertexAsVector3f(0));
        var d2 = getPlaneDistance(triangleModel.getVertexAsVector3f(1));
        var d3 = getPlaneDistance(triangleModel.getVertexAsVector3f(2));

        // 単位がメートルのため、誤差を考慮して0.003m以下なら同一平面とみなす
        return d1 <= 0.003 && d2 <= 0.003 && d3 <= 0.003 ;
    }

    public float getMinZ() {
        var min = Float.MAX_VALUE;
        for (var i = 0; i < 3; i++) {
            min = Math.min(min, _vertices[i].z);
        }
        return min;
    }

    public static TriangleModel CreateGroundTriangle() {
        return new TriangleModel();
    }
}
