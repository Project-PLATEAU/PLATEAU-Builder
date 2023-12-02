package org.plateau.citygmleditor.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;

import javafx.scene.paint.PhongMaterial;

public class ObjExporter {
    public static void export(String fileUrl, ILODSolid lodSolid) {
        var polygons = lodSolid.getPolygons();

        String materialUrl = null;
        var indexCount = 0;
        for (var polygon : polygons) {
            indexCount += polygon.getFaces().length;
            if (materialUrl == null) {
                var surfaceData = polygon.getSurfaceData();
                if (surfaceData != null) {
                    var material = surfaceData.getMaterial();
                    if (material instanceof PhongMaterial) {
                        PhongMaterial phongMaterial = (PhongMaterial) material;
                        materialUrl = phongMaterial.getDiffuseMap().getUrl();
                    }
                }
            }
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
                uvs[uvIndexOffset + i + 1] = (float) subUVs[i + 1];
            }
            uvIndexOffset += subUVs.length;
        }

        File file = new File(fileUrl);
        var fileName = file.getName();
        var mtlFileName = String.format("%s.mtl", fileName.substring(0, fileName.lastIndexOf(".")));
        try (var stream = new FileOutputStream(file, false);
                var writer = new OutputStreamWriter(stream, "UTF-8")) {
            writer.write(String.format("mtllib %s\r\n", mtlFileName));
            writer.write("g model\r\n");

            for (int i = 0; i < vertices.length; i += 3) {
                writer.write(String.format("v %f %f %f\r\n", vertices[i], vertices[i + 1], vertices[i + 2]));
            }
            for (int i = 0; i < uvs.length; i += 2) {
                writer.write(String.format("vt %f %f\r\n", uvs[i], uvs[i + 1]));
            }
            writer.write("usemtl Material\r\n");
            for (int i = 0; i < faces.length; i += 6) {
                writer.write(String.format("f %d/%d %d/%d %d/%d\r\n", faces[i] + 1, faces[i + 1] + 1,
                        faces[i + 2] + 1, faces[i + 3] + 1, faces[i + 4] + 1, faces[i + 5] + 1));
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var stream = new FileOutputStream(new File(file.getParent(), mtlFileName), false);
                var writer = new OutputStreamWriter(stream, "UTF-8")) {
            writer.write("newmtl Material\r\n");
            writer.write("Ka 0.000000 0.000000 0.000000\r\n");
            if (materialUrl != null) {
                writer.write("Kd 1.0 1.0 1.0\r\n");
            } else {
                writer.write("Kd 0.45 0.5 0.5\r\n");
            }
            writer.write("Ks 0.000000 0.000000 0.000000\r\n");
            writer.write("Ns 2.000000\r\n");
            writer.write("d 1.000000\r\n");
            writer.write("Tr 0.000000\r\n");
            writer.write("Pr 0.333333\r\n");
            writer.write("Pm 0.080000\r\n");
            if (materialUrl != null) {
                File materialFile = new File(materialUrl);
                var materialFileName = materialFile.getName();
                writer.write(String.format("map_Kd %s\r\n", materialFileName));

                var copyPath = new File(file.getParent(), materialFileName);
                Files.copy(Paths.get(materialFile.getAbsolutePath()), Paths.get(copyPath.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
