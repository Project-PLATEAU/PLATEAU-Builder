package org.plateau.citygmleditor.exporters;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import de.javagl.jgltf.model.io.GltfWriter;
import de.javagl.jgltf.model.io.v2.GltfAssetV2;
import de.javagl.jgltf.model.io.v2.GltfAssetsV2;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import de.javagl.jgltf.impl.v2.GlTF;
import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;


public class GltfExporter {
    public static void export(String fileUrl, CityModel cityModel, ILODSolid lodSolid) {
        // TODO: CityModel→GltfModel
        
        // Create a mesh primitive
        int indices[] = { 0, 1, 2 };
        float positions[] =
        {
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            0.5f, 1.0f, 0.0f
        };
        
        MeshPrimitiveBuilder meshPrimitiveBuilder = 
            MeshPrimitiveBuilder.create(); 
        meshPrimitiveBuilder.setIntIndicesAsShort(IntBuffer.wrap(indices));
        meshPrimitiveBuilder.addPositions3D(FloatBuffer.wrap(positions));
        DefaultMeshPrimitiveModel meshPrimitiveModel = 
            meshPrimitiveBuilder.build();
        
        // Create a material, and assign it to the mesh primitive
        MaterialBuilder materialBuilder =  MaterialBuilder.create();
        materialBuilder.setBaseColorFactor(1.0f, 0.9f, 0.9f, 1.0f);
        materialBuilder.setDoubleSided(true);
        MaterialModelV2 materialModel = materialBuilder.build();
        meshPrimitiveModel.setMaterialModel(materialModel);

        // Create a mesh with the mesh primitive
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.addMeshPrimitiveModel(meshPrimitiveModel);

        // Create a node with the mesh
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        nodeModel.addMeshModel(meshModel);
        
        // Create a scene with the node
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        sceneModel.addNode(nodeModel);

        // Pass the scene to the model builder. It will take care
        // of the other model elements that are contained in the scene.
        // (I.e. the mesh primitive and its accessors, and the material 
        // and its textures)
        GltfModelBuilder gltfModelBuilder = GltfModelBuilder.create();
        gltfModelBuilder.addSceneModel(sceneModel);
        DefaultGltfModel gltfModel = gltfModelBuilder.build();

        GltfAssetV2 asset = GltfAssetsV2.createEmbedded(gltfModel);
        GlTF gltf = asset.getGltf();

        try (FileOutputStream stream = new FileOutputStream(fileUrl)) {
            GltfWriter writer = new GltfWriter();
            writer.setIndenting(true);
            writer.write(gltf, stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
