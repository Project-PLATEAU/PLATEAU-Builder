package org.plateau.citygmleditor.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;

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
     */
    public static void export(String fileUrl, ILODSolidView lodSolid) {
        Map<String, MaterialModel> materialMap = new HashMap<>();
        materialMap.put("defaultMaterialModel", defaultMaterialModel);
        ArrayList<ObjectModel> objectModels = new ArrayList<>();
        if (lodSolid instanceof LOD1SolidView) {
            objectModels.add(createObjectModel((LOD1SolidView) lodSolid, materialMap));
        } else if (lodSolid instanceof LOD2SolidView) {
            objectModels.addAll(createObjectModels((LOD2SolidView) lodSolid, materialMap));
        } else {
            throw new IllegalArgumentException("LOD1SolidView or LOD2SolidView is required.");
        }

        File file = new File(fileUrl);
        var fileName = file.getName();
        var mtlFileName = String.format("%s.mtl", fileName.substring(0, fileName.lastIndexOf(".")));
        try (var stream = new FileOutputStream(file, false);
                var writer = new OutputStreamWriter(stream, "UTF-8")) {
            writer.write("# org.plateau.citygmleditor\r\n");
            writer.write(String.format("mtllib %s\r\n", mtlFileName));

            var vertexIndexOffset = 0;
            var uvIndexOffset = 0;
            for (var objectModel : objectModels) {
                writer.write(String.format("o %s\r\n", objectModel.getName()));
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
                    writer.write(String.format("f %d/%d %d/%d %d/%d\r\n", faces[i] + vertexIndexOffset + 1, faces[i + 1] + uvIndexOffset + 1,
                            faces[i + 2] + vertexIndexOffset + 1, faces[i + 3] + uvIndexOffset + 1, faces[i + 4] + vertexIndexOffset + 1, faces[i + 5] + uvIndexOffset + 1));
                }
                writer.write("\r\n");
                writer.flush();

                vertexIndexOffset += vertices.length / 3;
                uvIndexOffset += uvs.length / 2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var stream = new FileOutputStream(new File(file.getParent(), mtlFileName), false);
                var writer = new OutputStreamWriter(stream, "UTF-8")) {

            for (var material : materialMap.values()) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ObjectModel createObjectModel(LOD1SolidView lod1Solid, Map<String, MaterialModel> materialMap) {
        return createObjectModel("model", lod1Solid.getPolygons(), materialMap);
    }

    private static ArrayList<ObjectModel> createObjectModels(LOD2SolidView lod2Solid, Map<String, MaterialModel> materialMap) {
        var objectModels = new ArrayList<ObjectModel>();
        for (var boundary : lod2Solid.getBoundaries()) {
            var polygons = boundary.getPolygons();
            objectModels.add(createObjectModel(boundary.getId(), polygons, materialMap));
        }

        return objectModels;
    }

    private static ObjectModel createObjectModel(String name, ArrayList<PolygonView> polygons, Map<String, MaterialModel> materialMap) {
        var indexCount = 0;
        for (var polygon : polygons) {
            indexCount += polygon.getFaces().length;
        }

        var faces = new int[indexCount];
        var faceIndex = 0;
        for (var polygon : polygons) {
            var polygonFaceBuffer = polygon.getFaceBuffer();
            for (var i = 0; i < polygonFaceBuffer.getFaceCount(); ++i) {
                // 頂点インデックス
                faces[faceIndex++] = polygonFaceBuffer.getVertexIndex(i);
                // UVインデックス
                faces[faceIndex++] = polygonFaceBuffer.getTexCoordIndex(i);
            }
        }

        var materialModel = createOrGetMaterial(polygons.get(0), materialMap);

        // ダミー
        var vertices = new float[0];
        return new ObjectModel(name, faces, , uvs, materialModel);
    }

    private static MaterialModel createOrGetMaterial(PolygonView polygon, Map<String, MaterialModel> materialMap) {
        MaterialModel materialModel = null;
        var surfaceData = polygon.getSurfaceData();
        if (surfaceData == null) return defaultMaterialModel;

        var material = surfaceData.getMaterial();
        if (!(material instanceof PhongMaterial))  return defaultMaterialModel;

        PhongMaterial phongMaterial = (PhongMaterial) material;
        var url = phongMaterial.getDiffuseMap().getUrl();
        if (materialMap.containsKey(url)) {
            materialModel = materialMap.get(url);
        } else {
            materialModel = createMaterialModel(phongMaterial);
            materialMap.put(url, materialModel);
        }

        return materialModel;
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
