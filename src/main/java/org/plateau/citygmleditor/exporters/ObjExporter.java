package org.plateau.citygmleditor.exporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;

import javafx.scene.paint.PhongMaterial;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;

/**
 * A class for exporting a {@link ILODSolidView} to a OBJ file
 */
public class ObjExporter {
    private static final MaterialModel defaultMaterialModel = new MaterialModel("defaultMaterialModel");

    /**
     * Export the {@link ILODSolidView} to a OBJ file
     * @param fileUrl the file url
     * @param lodSolid the {@link ILODSolidView}
     * @param buildingId the building id
     */
    public static void export(String fileUrl, ILODSolidView lodSolid, String buildingId) {
        ObjectModel objectModel = createObjectModel(buildingId, lodSolid);

        File file = new File(fileUrl);
        var fileName = file.getName();
        var mtlFileName = String.format("%s.mtl", fileName.substring(0, fileName.lastIndexOf(".")));
        try (var stream = new FileOutputStream(file, false);
                var writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {
            writer.write("# org.plateau.citygmleditor\r\n");
            writer.write(String.format("mtllib %s\r\n", mtlFileName));

            writer.write(String.format("g %s\r\n", objectModel.getName()));
            writer.write(String.format("usemtl %s\r\n", objectModel.getMaterial().getName()));

            var vertices = objectModel.getVertices();
            for (int i = 0; i < vertices.length; i += 3) {
                writer.write(String.format("v %f %f %f\r\n", vertices[i], vertices[i + 1], vertices[i + 2]));
            }

            var uvs = objectModel.getUVs();
            for (int i = 0; i < uvs.length; i += 2) {
                writer.write(String.format("vt %f %f\r\n", uvs[i], uvs[i + 1]));
            }

            var faces = objectModel.getFaces();
            for (int i = 0; i < faces.length; i += 6) {
                 writer.write(String.format("f %d/%d %d/%d %d/%d\r\n", faces[i] + 1, faces[i + 1] + 1,
                         faces[i + 2] + 1, faces[i + 3] + 1, faces[i + 4] + 1, faces[i + 5] + 1));
            }
            writer.write("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var stream = new FileOutputStream(new File(file.getParent(), mtlFileName), false);
                var writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {

            var material = objectModel.getMaterial();
            writer.write(String.format("newmtl %s\r\n", material.getName()));
            writer.write("Ka 0.000000 0.000000 0.000000\r\n");
            if (material.hasFileName()) {
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
            if (material.hasFileName()) {
                var materialFileName = material.getFileName();
                writer.write(String.format("map_Kd %s\r\n", materialFileName));

                var copyPath = new File(file.getParent(), materialFileName);
                Files.copy(Paths.get(new File(material.getMaterialUrl()).getAbsolutePath()), Paths.get(copyPath.getAbsolutePath()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            writer.write("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ObjectModel createObjectModel(String name, ILODSolidView lodSolid) {
        var polygons = lodSolid.getPolygons();
        var indexCount = 0;
        for (var polygon : polygons) {
            indexCount += polygon.getFaceBuffer().getPointCount() * 2;
        }

        var faces = new int[indexCount];
        var faceIndex = 0;
        for (var polygon : polygons) {
            var polygonFaceBuffer = polygon.getFaceBuffer();
            for (var i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
                // 頂点インデックス
                faces[faceIndex++] = polygonFaceBuffer.getVertexIndex(i);
                // UVインデックス
                faces[faceIndex++] = polygonFaceBuffer.getTexCoordIndex(i);
            }
        }

        var materialModel = createOrGetMaterial(polygons);

        return new ObjectModel(name, faces, lodSolid.getVertexBuffer().getBufferAsArray(), lodSolid.getVertexBuffer().getBufferAsArray(), materialModel);
    }

    private static MaterialModel createOrGetMaterial(ArrayList<PolygonView> polygons) {
        for (var polygon : polygons) {
            var surfaceData = polygon.getSurfaceData();
            if (surfaceData == null) continue;

            var material = surfaceData.getMaterial();
            if (!(material instanceof PhongMaterial)) continue;

            return createMaterialModel((PhongMaterial)material);
        }

        return defaultMaterialModel;
    }

    private static MaterialModel createMaterialModel(PhongMaterial material) {
        var materialUrl = new File(material.getDiffuseMap().getUrl());
        var fileName = materialUrl.getName();
        var name = fileName.substring(0, fileName.lastIndexOf("."));

        return new MaterialModel(name, fileName, materialUrl.getAbsolutePath());
    }

    private static class ObjectModel {
        private String name;
        private MaterialModel materialModel;
        private int[] faces;
        private float[] vertices;
        private float[] uvs;

        public ObjectModel(String name, int[] faces, float[] vertices, float[] uvs, MaterialModel materialModel) {
            this.name = name;
            this.faces = faces;
            this.vertices = vertices;
            this.uvs = uvs;
            this.materialModel = materialModel;
        }

        public String getName() {
            return name;
        }

        public MaterialModel getMaterial() {
            return materialModel;
        }

        public int[] getFaces() {
            return faces;
        }

        public float[] getVertices() {
            return vertices;
        }

        public float[] getUVs() {
            return uvs;
        }
    }

    private static class MaterialModel {
        private String name;
        private String fileName;
        private String materialUrl;

        public MaterialModel(String name) {
            this(name, null, null);
        }

        public MaterialModel(String name, String fileName, String materialUrl) {
            this.name = name;
            this.fileName = fileName;
            this.materialUrl = materialUrl;
        }

        public String getName() {
            return name;
        }

        public String getFileName() {
            return fileName;
        }

        public String getMaterialUrl() {
            return materialUrl;
        }

        public boolean hasFileName() {
            return fileName != null;
        }
    }
}
