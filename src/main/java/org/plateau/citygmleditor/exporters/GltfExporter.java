package org.plateau.citygmleditor.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.plateau.citygmleditor.citymodel.geometry.BoundarySurface;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;
import org.plateau.citygmleditor.citymodel.geometry.LOD1Solid;
import org.plateau.citygmleditor.citymodel.geometry.LOD2Solid;

import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.ImageModels;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultImageModel;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultTextureModel;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import javafx.scene.paint.PhongMaterial;

/**
 * A class for exporting a {@link DefaultGltfModel} to a gLTF file
 */
public class GltfExporter {
    private static final MaterialModelV2 defaultMaterialModel;

    static {
        MaterialBuilder materialBuilder = MaterialBuilder.create();
        materialBuilder.setBaseColorFactor(0.9f, 0.9f, 0.9f, 1.0f);
        materialBuilder.setDoubleSided(true);
        defaultMaterialModel = materialBuilder.build();
        defaultMaterialModel.setName("defaultMaterialModel");
    }

    /**
     * Export the {@link ILODSolid} to a gLTF file
     * 
     * @param fileUrl    the file url
     * @param lodSolid   the {@link ILODSolid}
     * @param buildingId the building id
     */
    public static void export(String fileUrl, ILODSolid lodSolid, String buildingId) {
        DefaultSceneModel sceneModel = null;
        if (lodSolid instanceof LOD1Solid) {
            sceneModel = createSceneModel((LOD1Solid) lodSolid, buildingId);
        } else if (lodSolid instanceof LOD2Solid) {
            sceneModel = createSceneModel((LOD2Solid) lodSolid, buildingId);
        } else {
            throw new IllegalArgumentException("LOD1Solid or LOD2Solid is required.");
        }

        GltfModelBuilder gltfModelBuilder = GltfModelBuilder.create();
        gltfModelBuilder.addSceneModel(sceneModel);
        DefaultGltfModel gltfModel = gltfModelBuilder.build();

        GltfModelWriter writer = new GltfModelWriter();

        File file = new File(fileUrl);
        String fileName = file.getName();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

        if (ext.toLowerCase().equals("gltf")) {
            try {
                // writer.write(gltfModel, fileUrl);
                writer.writeEmbedded(gltfModel, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (ext.toLowerCase().equals("glb")) {
            try (FileOutputStream stream = new FileOutputStream(fileUrl)) {
                writer.writeBinary(gltfModel, stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static DefaultSceneModel createSceneModel(LOD1Solid lod1Solid, String buildingId) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(buildingId);
        for (var polygon : lod1Solid.getPolygons()) {
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(polygon, null);
            meshPrimitiveModel.setMaterialModel(defaultMaterialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private static DefaultSceneModel createSceneModel(LOD2Solid lod2Solid, String buildingId) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(buildingId);
        for (var boundary : lod2Solid.getBoundaries()) {
            for (var polygon : boundary.getPolygons()) {
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(polygon, boundary);
                meshPrimitiveModel.setMaterialModel(createOrGetMaterialModel(materialMap, polygon));
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private static DefaultMeshPrimitiveModel createMeshPrimitive(org.plateau.citygmleditor.citymodel.geometry.Polygon polygon, BoundarySurface boundary) {
        var polygonFaces = polygon.getFaces();
        var faces = new int[polygonFaces.length / 2];
        for (var i = 0; i < faces.length; i += 3) {
            faces[i] = polygonFaces[i * 2];
            faces[i + 1] = polygonFaces[i * 2 + 2];
            faces[i + 2] = polygonFaces[i * 2 + 4];
        }

        // 右手系Y-up
        var subVertices = polygon.getAllVertices();
        float positions[] = new float[subVertices.length];
        for (int i = 0; i < subVertices.length; i += 3) {
            positions[i] = (float) subVertices[i + 1];
            positions[i + 1] = (float) subVertices[i + 2];
            positions[i + 2] = (float) subVertices[i + 0];
        }

        var subUVs = polygon.getAllUVs();
        var uvs = new float[subUVs.length];
        for (int i = 0; i < subUVs.length; i += 2) {
            uvs[i] = (float) subUVs[i];
            uvs[i + 1] = 1 - (float) subUVs[i + 1];
        }

        MeshPrimitiveBuilder meshPrimitiveBuilder = MeshPrimitiveBuilder.create();
        meshPrimitiveBuilder.setIntIndicesAsShort(IntBuffer.wrap(faces));
        meshPrimitiveBuilder.addPositions3D(FloatBuffer.wrap(positions));
        meshPrimitiveBuilder.addTexCoords02D(FloatBuffer.wrap(uvs));

        DefaultMeshPrimitiveModel meshPrimitiveModel = meshPrimitiveBuilder.build();
        if (boundary == null) return meshPrimitiveModel;

        Map<String, String> extrasMap = new HashMap<>();
        var cityGmlClass = boundary.getCityGMLClass();
        if (cityGmlClass != null) {
            extrasMap.put("type", cityGmlClass.toString());
        }
        var boundaryId = boundary.getId();
        if (boundary.getId() != null) {
            extrasMap.put("surfaceId", boundaryId);
        }
        var polygonId = polygon.getGMLID();
        if (polygonId != null) {
            extrasMap.put("polygonId", polygonId);
        }
        var linearRingId = polygon.getExteriorRing().getGMLID();
        if (linearRingId != null) {
            extrasMap.put("linearRingId", linearRingId);
        }
        if (extrasMap.size() > 0) {
            meshPrimitiveModel.setExtras(extrasMap);
        }

        return meshPrimitiveModel;
    }

    private static MaterialModelV2 createOrGetMaterialModel(Map<String, MaterialModelV2> materialMap, org.plateau.citygmleditor.citymodel.geometry.Polygon polygon) {
        MaterialModelV2 materialModel = null;
        var surfaceData = polygon.getSurfaceData();
        if (surfaceData == null) return defaultMaterialModel;

        var material = surfaceData.getMaterial();
        if (!(material instanceof PhongMaterial)) return defaultMaterialModel;

        PhongMaterial phongMaterial = (PhongMaterial) material;
        var url = phongMaterial.getDiffuseMap().getUrl();
        if (materialMap.containsKey(url)) {
            materialModel = materialMap.get(url);
        } else {
            materialModel = createMaterial(phongMaterial);
            materialMap.put(url, materialModel);
        }

        return materialModel;
    }

    private static MaterialModelV2 createMaterial(PhongMaterial material) {
        var materialFile = new File(material.getDiffuseMap().getUrl());
        var inputUri = Paths.get(materialFile.getAbsolutePath()).toUri().normalize().toString();
        DefaultImageModel imageModel = ImageModels.create(inputUri, materialFile.getName());
        var textureModel = new DefaultTextureModel();
        textureModel.setImageModel(imageModel);

        MaterialBuilder materialBuilder = MaterialBuilder.create();
        materialBuilder.setBaseColorFactor(0.9f, 0.9f, 0.9f, 1.0f);
        materialBuilder.setDoubleSided(true);
        materialBuilder.setBaseColorTexture(textureModel, 0);

        MaterialModelV2 materialModelV2 = materialBuilder.build();
        materialModelV2.setName(materialFile.getName());

        return materialModelV2;
    }
}
