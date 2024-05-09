package org.plateaubuilder.core.io.mesh.exporters;

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

import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.io.mesh.AxisDirection;
import org.plateaubuilder.core.io.mesh.AxisTransformer;
import org.plateaubuilder.core.utils3d.geom.Vec2f;
import org.plateaubuilder.core.utils3d.geom.Vec3f;

import de.javagl.jgltf.model.creation.GltfModelBuilder;
import de.javagl.jgltf.model.creation.ImageModels;
import de.javagl.jgltf.model.creation.MaterialBuilder;
import de.javagl.jgltf.model.creation.MeshPrimitiveBuilder;
import de.javagl.jgltf.model.impl.DefaultGltfModel;
import de.javagl.jgltf.model.impl.DefaultImageModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.impl.DefaultTextureModel;
import de.javagl.jgltf.model.io.GltfModelWriter;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import javafx.scene.paint.PhongMaterial;

/**
 * 抽象的なGltfLODExporterの基底クラスです。 GltfLODExporterは、ILODViewをGltf形式でエクスポートするための機能を提供します。
 */
abstract public class AbstractGltfLODExporter<T extends ILODView> extends AbstractLODExporter<T> {
    private static final MaterialModelV2 defaultMaterialModel;

    static {
        MaterialBuilder materialBuilder = MaterialBuilder.create();
        materialBuilder.setBaseColorFactor(0.9f, 0.9f, 0.9f, 1.0f);
        materialBuilder.setDoubleSided(true);
        defaultMaterialModel = materialBuilder.build();
        defaultMaterialModel.setName("defaultMaterialModel");
    }

    protected MaterialModelV2 GetDefaultMaterialModel() {
        return defaultMaterialModel;
    }

    /**
     * AbstractGltfLODExporterクラスのコンストラクターです。
     * 
     * @param lodView      {@link ILODView}
     * @param featureId    Feature ID
     * @param exportOption エクスポートオプション
     */
    public AbstractGltfLODExporter(T lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(String fileUrl) {
        T lodView = getLODView();
        DefaultSceneModel sceneModel = createSceneModel(lodView);

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

    protected DefaultMeshPrimitiveModel createMeshPrimitive(ILODView lodView, PolygonView polygon, MaterialModelV2 materialModel) {
        var faceBuffer = polygon.getFaceBuffer();
        var pointCount = faceBuffer.getPointCount();
        var faces = new int[pointCount];
        var vertexBuffer = lodView.getVertexBuffer();
        var texCoordBuffer = lodView.getTexCoordBuffer();
        List<Vec3f> vertexList = new ArrayList<>();
        List<Vec2f> texCoordList = new ArrayList<>();
        Map<Integer, Integer> vertexIndexMap = new HashMap<>();
        for (int i = 0; i < pointCount; i++) {
            var vertexIndex = faceBuffer.getVertexIndex(i);
            if (!vertexIndexMap.containsKey(vertexIndex)) {
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

        var exportOption = getExportOption();
        var axisTransformer = new AxisTransformer(AxisDirection.TOOL_AXIS_DIRECTION,
                new AxisDirection(exportOption.getAxisEast(), exportOption.getAxisTop(), true));
        var offset = exportOption.getOffset();
        var positions = new float[vertexList.size() * 3];
        for (int i = 0; i < vertexList.size(); i++) {
            var vertex = vertexList.get(i);
            var vec3f = axisTransformer.transform((float) (vertex.x + offset.x), (float) (vertex.y + offset.y), (float) (vertex.z + offset.z));
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

    protected MaterialModelV2 createOrGetMaterialModel(Map<String, MaterialModelV2> materialMap, PolygonView polygon) {
        MaterialModelV2 materialModel = null;
        var surfaceData = polygon.getSurfaceData();
        if (surfaceData == null)
            return defaultMaterialModel;

        var material = surfaceData.getMaterial();
        if (!(material instanceof PhongMaterial))
            return defaultMaterialModel;

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

    protected MaterialModelV2 createMaterial(PhongMaterial material) {
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

    abstract protected DefaultSceneModel createSceneModel(T lodView);
}
