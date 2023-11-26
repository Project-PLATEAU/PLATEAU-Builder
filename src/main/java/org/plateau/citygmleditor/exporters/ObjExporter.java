package org.plateau.citygmleditor.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.plateau.citygmleditor.citymodel.Building;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;
import org.plateau.citygmleditor.citymodel.geometry.LOD2Solid;

public class ObjExporter {
    public static void export(String fileUrl, CityModel cityModel, Building building, ILODSolid lodSolid) {
        Envelope envelope = building.getEnvelope();

        // TODO:仮実装
        if (lodSolid instanceof LOD2Solid) {
            LOD2Solid lod2Solid = (LOD2Solid) lodSolid;
            var polygons = lod2Solid.getPolygons();

            // TODO:GeometryFactoryからパクってきた。共通化したい
            var indexCount = 0;
            for (var polygon : polygons) {
                indexCount += polygon.getFaces().length;
            }

            var faces = new int[indexCount];
            var verticesSize = 0;
            var uvsSize = 0;
            var faceIndex = 0;
            for (var polygon : polygons) {
                var polygonFaces = polygon.getFaces();
                for (var i = 0; i < polygonFaces.length; i += 2) {
                    // 頂点インデックス
                    faces[faceIndex++] = polygonFaces[i] + verticesSize / 3;
                    // UVインデックス
                    faces[faceIndex++] = polygonFaces[i + 1] + uvsSize / 2;
                }

                verticesSize += polygon.getAllVerticesSize();
                uvsSize += polygon.getAllUVsSize();
            }

            var vertices = new float[verticesSize];
            var vertexIndexOffset = 0;
            for (var polygon : polygons) {
                var subVertices = polygon.getAllVertices();
                for (int i = 0; i < subVertices.length; ++i) {
                    vertices[vertexIndexOffset + i] = (float) subVertices[i];
                }
                vertexIndexOffset += subVertices.length;
            }

            var uvs = new float[uvsSize];
            var uvIndexOffset = 0;
            for (var polygon : polygons) {
                var subUVs = polygon.getAllUVs();
                for (int i = 0; i < subUVs.length; i += 2) {
                    // x
                    uvs[uvIndexOffset + i] = (float) subUVs[i];
                    // y
                    uvs[uvIndexOffset + i + 1] = 1 - (float) subUVs[i + 1];
                }
                uvIndexOffset += subUVs.length;
            }

            // TODO: たぶん座標変換しないといけない

            File file = new File(fileUrl);
            try (var stream = new FileOutputStream(file);
                    var writer = new OutputStreamWriter(stream, "UTF-8")) {

                var origin = envelope.getLowerCorner().toList3d();
                var lowerCorner = envelope.getLowerCorner().toList3d();
                var upperCorner = envelope.getUpperCorner().toList3d();
                var fileName = file.getName();
                int dotIndex = fileName.lastIndexOf(".");
                writer.write(String.format("# Origin: %f %f %f\r\n", origin.get(0), origin.get(1), origin.get(2)));
                writer.write(String.format("# Lower : %f %f %f\r\n", lowerCorner.get(0), lowerCorner.get(1), lowerCorner.get(2)));
                writer.write(String.format("# Upper : %f %f %f\r\n", upperCorner.get(0), upperCorner.get(1), upperCorner.get(2)));
                writer.write(String.format("# mtllib %s.mtl", fileName.substring(0, dotIndex)));
                writer.write("g model\r\n");

                for (int i = 0; i < vertices.length; i += 3) {
                    writer.write(String.format("v %f %f %f\r\n", vertices[i], vertices[i + 1], vertices[i + 2]));
                }
                for (int i = 0; i < uvs.length; i += 2) {
                    writer.write(String.format("vt %f %f\r\n", uvs[i], uvs[i + 1]));
                }
                for (int i = 0; i < faces.length; i += 6) {
                    writer.write(String.format("f %d/%d %d/%d %d/%d\r\n", faces[i] + 1, faces[i + 1] + 1, faces[i + 2] + 1, faces[i + 3] + 1, faces[i + 4] + 1, faces[i + 5] + 1));
                }

                // TODO:テクスチャのエクスポート
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
