package org.plateau.citygmleditor.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.plateau.citygmleditor.citygmleditor.AxisDirection;
import org.plateau.citygmleditor.citygmleditor.AxisTransformer;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;
import org.plateau.citygmleditor.utils3d.geom.Vec2f;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;

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

    private ExportOption exportOption;

    /**
     * Export the {@link ILODSolidView} to a gLTF file
     * @param fileUrl the file url
     * @param lodSolid the {@link ILODSolidView}
     * @param buildingId the building id
     * @param exportOption the exportOption
     */
    public void export(String fileUrl, ILODSolidView lodSolid, String buildingId, ExportOption exportOption) {
        this.exportOption = exportOption;
        DefaultSceneModel sceneModel = null;
        if (lodSolid instanceof LOD1SolidView) {
            sceneModel = createSceneModel((LOD1SolidView) lodSolid, buildingId);
        } else if (lodSolid instanceof LOD2SolidView) {
            sceneModel = createSceneModel((LOD2SolidView) lodSolid, buildingId);
        } else {
            throw new IllegalArgumentException("LOD1SolidView or LOD2SolidView is required.");
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
                //writer.write(gltfModel, fileUrl);
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

    private DefaultSceneModel createSceneModel(LOD1SolidView lod1Solid, String buildingId) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(buildingId);
        for (var polygon : lod1Solid.getPolygons()) {
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod1Solid, polygon, defaultMaterialModel);
            meshPrimitiveModel.setMaterialModel(defaultMaterialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD2SolidView lod2Solid, String buildingId) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(buildingId);

        for (var boundary : lod2Solid.getBoundaries()) {
            for (var polygon : boundary.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod2Solid, polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultMeshPrimitiveModel createMeshPrimitive(ILODSolidView lodSolid, PolygonView polygon, MaterialModelV2 materialModel) {
        var faceBuffer = polygon.getFaceBuffer();
        var pointCount = faceBuffer.getPointCount();
        var faces = new int[pointCount];
        var vertexBuffer = lodSolid.getVertexBuffer();
        var texCoordBuffer = lodSolid.getTexCoordBuffer();
        List<Vec3f> vertexList = new ArrayList<>();
        List<Vec2f> texCoordList = new ArrayList<>();
        Map<Integer, Integer> vertexIndexMap = new HashMap<>();
        for (int i = 0; i < pointCount; i++) {
            var vertexIndex = faceBuffer.getVertexIndex(i);
            if (!vertexIndexMap.containsKey(vertexIndex))  {
                vertexIndexMap.put(vertexIndex, vertexIndexMap.size());
                vertexList.add(vertexBuffer.getVertex(vertexIndex));
                if (materialModel != defaultMaterialModel) {
                    var texCoordIndex = faceBuffer.getTexCoordIndex(i);
                    texCoordList.add(texCoordBuffer.getTexCoord(texCoordIndex, true));
                }
            }
            faces[i] = vertexIndexMap.get(vertexIndex);
        }

        MeshPrimitiveBuilder meshPrimitiveBuilder = MeshPrimitiveBuilder.create();
        meshPrimitiveBuilder.setIntIndicesAsShort(IntBuffer.wrap(faces));

        var axisTransformer = new AxisTransformer(AxisDirection.TOOL_AXIS_DIRECTION, new AxisDirection(exportOption.getAxisEast(), exportOption.getAxisTop(), true));
        var offset = exportOption.getOffset();
        var positions = new float[vertexList.size() * 3];
        for (int i = 0; i < vertexList.size(); i++) {
            var vertex = vertexList.get(i);
            var vec3f = axisTransformer.transform((float)(vertex.x + offset.x), (float)(vertex.y + offset.y), (float)(vertex.z + offset.z));
            positions[i * 3] = vec3f.x;
            positions[i * 3 + 1] = vec3f.y;
            positions[i * 3 + 2] = vec3f.z;
        }
        meshPrimitiveBuilder.addPositions3D(FloatBuffer.wrap(positions));

        if (texCoordList.size() > 0) {
            var uvs = new float[texCoordList.size() * 2];
            for (int i = 0; i < texCoordList.size(); i++) {
                var texCoord = texCoordList.get(i);
                uvs[i * 2] = texCoord.x;
                uvs[i * 2 + 1] = 1 - texCoord.y;
            }
            meshPrimitiveBuilder.addTexCoords02D(FloatBuffer.wrap(uvs));
        }

        return meshPrimitiveBuilder.build();
    }

    private MaterialModelV2 createOrGetMaterialModel(Map<String, MaterialModelV2> materialMap, PolygonView polygon) {
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

    private MaterialModelV2 createMaterial(PhongMaterial material) {
        File materialFile = null;
        try {
            materialFile = new File(new URI(material.getDiffuseMap().getUrl()).getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
