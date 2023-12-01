package org.plateau.citygmleditor.exporters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;
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

public class GltfExporter {
    public static void export(String fileUrl, CityModel cityModel, ILODSolid lodSolid) {
        // TODO:仮実装
        if (lodSolid instanceof LOD2Solid) {
            LOD2Solid lod2Solid = (LOD2Solid) lodSolid;

            DefaultMeshModel meshModel = new DefaultMeshModel();

            MaterialBuilder materialBuilder = MaterialBuilder.create();
            materialBuilder.setBaseColorFactor(0.9f, 0.9f, 0.9f, 1.0f);
            materialBuilder.setDoubleSided(true);
            MaterialModelV2 defaultMaterialModel = materialBuilder.build();
            MaterialModelV2 textureMaterialModel = null;

            var polygons = lod2Solid.getPolygons();
            for (var polygon : polygons) {
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
                MaterialModelV2 materialModel = null;

                var surfaceData = polygon.getSurfaceData();
                if (surfaceData != null) {
                    var material = surfaceData.getMaterial();
                    if (material instanceof PhongMaterial) {
                        if (textureMaterialModel == null) {
                            PhongMaterial phongMaterial = (PhongMaterial) material;
                            var materialFile = new File(phongMaterial.getDiffuseMap().getUrl());
                            var inputUri = Paths.get(materialFile.getAbsolutePath()).toUri().normalize().toString();
                            DefaultImageModel imageModel = ImageModels.create(inputUri, materialFile.getName());
                            var textureModel = new DefaultTextureModel();
                            textureModel.setImageModel(imageModel);
                            materialBuilder.setBaseColorTexture(textureModel, 0);
                            textureMaterialModel = materialBuilder.build();
                        }
                        materialModel = textureMaterialModel;
                    }
                } else {
                    materialModel = defaultMaterialModel;
                }

                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }

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
    }
}
