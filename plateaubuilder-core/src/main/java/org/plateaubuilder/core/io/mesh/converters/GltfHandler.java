package org.plateaubuilder.core.io.mesh.converters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.io.mesh.AxisTransformer;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;

import de.javagl.jgltf.impl.v1.Node;
import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.NodeModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;

/**
 * GltfHandlerは、GLTF形式のファイルを処理するためのハンドラクラスです。 このクラスは、GLTFファイルから三角形モデルのマップや表面データを作成する機能を提供します。
 */
public class GltfHandler extends Abstract3DFormatHandler {
    /**
     * GltfHandlerのコンストラクタです。
     * 
     * @param axisTransformer AxisTransformerオブジェクト
     */
    public GltfHandler(AxisTransformer axisTransformer) {
        super(axisTransformer);
    }

    private static Pattern BASE64_PATTERN = Pattern.compile("^data:.+;base64,(?<value>.+)$");

    private GltfModelReader _reader = new GltfModelReader();

    private Path _inputFile;

    private GltfModel _gltfModel;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String fileUrl) throws IOException, URISyntaxException {
        _inputFile = Paths.get(fileUrl);
        _gltfModel = _reader.read(_inputFile);
        var sceneModels = _gltfModel.getSceneModels();
        if (sceneModels.size() == 0) {
            throw new IllegalArgumentException("No scene found in the file.");
        }
        if (sceneModels.size() > 1) {
            throw new IllegalArgumentException("Multiple scenes found in the file.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<TriangleModel>> createTriangleModelsMap() {
        Map<String, List<TriangleModel>> triangleModelsMap = new HashMap<>();
        var sceneModel = _gltfModel.getSceneModels().get(0);
        for (var nodeModel : sceneModel.getNodeModels()) {
            createTriangleModelsMap(nodeModel, triangleModelsMap);
            for (var childNodeModel : nodeModel.getChildren()) {
                createTriangleModelsMap(childNodeModel, triangleModelsMap);
            }
        }

        return triangleModelsMap;
    }

    private void createTriangleModelsMap(NodeModel nodeModel, Map<String, List<TriangleModel>> triangleModelsMap) {
        for (var meshModel : nodeModel.getMeshModels()) {
            for (var meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                AccessorModel indices = meshPrimitiveModel.getIndices();

                var faces = createPolygonFaces(indices.getAccessorData());
                float[] vertices = null;
                float[] uvs = null;
                Map<String, AccessorModel> attributes = meshPrimitiveModel.getAttributes();
                for (Entry<String, AccessorModel> entry : attributes.entrySet()) {
                    AccessorModel accessorModel = entry.getValue();
                    AccessorData accessorData = accessorModel.getAccessorData();
                    switch (entry.getKey()) {
                    case "POSITION":
                        vertices = createPolygonVertices(accessorData);
                        break;
                    case "TEXCOORD_0":
                        uvs = createUVs(accessorData);
                        break;
                    default:
                        break;
                    }
                }

                for (int i = 0; i < vertices.length; i += 3) {
                    var convertedPoint = convertVertexAxis(vertices[i], vertices[i + 1], vertices[i + 2]);
                    vertices[i] = convertedPoint.x;
                    vertices[i + 1] = convertedPoint.y;
                    vertices[i + 2] = convertedPoint.z;
                }

                for (var i = 0; i < faces.length; i += 6) {
                    var triangleModel = new TriangleModel(faces, i, vertices, uvs, meshModel.getName());

                    // 不正な三角形は除外する
                    if (triangleModel.isValid()) {
                        var materialModel = meshPrimitiveModel.getMaterialModel();
                        var materialName = materialModel.getName();
                        if (!triangleModelsMap.containsKey(materialName)) {
                            triangleModelsMap.put(materialName, new LinkedList<>());
                        }
                        triangleModelsMap.get(materialName).add(triangleModel);
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AbstractSurfaceData> createSurfaceData() throws IOException, URISyntaxException {
        Map<String, AbstractSurfaceData> surfaceMap = new HashMap<>();
        var sceneModel = _gltfModel.getSceneModels().get(0);
        for (var nodeModel : sceneModel.getNodeModels()) {
            createSurfaceData(nodeModel, surfaceMap);
            for (var childNodeModel : nodeModel.getChildren()) {
                createSurfaceData(childNodeModel, surfaceMap);
            }
        }

        return surfaceMap;
    }

    private void createSurfaceData(NodeModel nodeModel, Map<String, AbstractSurfaceData> surfaceMap) {
        for (var meshModel : nodeModel.getMeshModels()) {
            for (var meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                var materialModel = meshPrimitiveModel.getMaterialModel();
                var materialName = materialModel.getName();
                if (surfaceMap.containsKey(materialName))
                    continue;
                var parameterizedTexture = createParameterizedTexture(materialModel, surfaceMap);
                if (parameterizedTexture == null)
                    continue;
                surfaceMap.put(materialName, parameterizedTexture);
            }
        }
    }

    private int[] createPolygonFaces(AccessorData accessorData) {
        ByteBuffer byteBuffer = accessorData.createByteBuffer();
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
        int[] faces = new int[shortBuffer.capacity() * 2];
        for (int i = 0; i < shortBuffer.capacity(); i += 3) {
            faces[i * 2] = shortBuffer.get(i);
            faces[i * 2 + 1] = shortBuffer.get(i);
            faces[i * 2 + 2] = shortBuffer.get(i + 1);
            faces[i * 2 + 3] = shortBuffer.get(i + 1);
            faces[i * 2 + 4] = shortBuffer.get(i + 2);
            faces[i * 2 + 5] = shortBuffer.get(i + 2);
        }

        return faces;
    }

    private float[] createPolygonVertices(AccessorData accessorData) {
        ByteBuffer byteBuffer = accessorData.createByteBuffer();
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] vertices = new float[floatBuffer.capacity()];
        for (int i = 0; i < floatBuffer.capacity(); i += 3) {
            vertices[i] = floatBuffer.get(i);
            vertices[i + 1] = floatBuffer.get(i + 1);
            vertices[i + 2] = floatBuffer.get(i + 2);
        }

        return vertices;
    }

    private float[] createUVs(AccessorData accessorData) {
        ByteBuffer byteBuffer = accessorData.createByteBuffer();
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] uvs = new float[floatBuffer.capacity()];
        for (int i = 0; i < floatBuffer.capacity(); i += 2) {
            uvs[i] = floatBuffer.get(i);
            uvs[i + 1] = 1 - floatBuffer.get(i + 1);
        }

        return uvs;
    }

    private ParameterizedTexture createParameterizedTexture(MaterialModel materialModel, Map<String, AbstractSurfaceData> surfaceMap) {
        var materialName = materialModel.getName();
        MaterialModelV2 materialModelV2 = (MaterialModelV2) materialModel;
        TextureModel textureModel = materialModelV2.getBaseColorTexture();
        if (textureModel == null)
            return null;

        ImageModel imageModel = textureModel.getImageModel();
        String imageURI = getAppearancePath(imageModel, materialName);

        ParameterizedTexture parameterizedTexture = new ParameterizedTexture();
        parameterizedTexture.setImageURI(imageURI);
        parameterizedTexture.setMimeType(new Code(imageModel.getMimeType()));
        surfaceMap.put(materialName, parameterizedTexture);

        return parameterizedTexture;
    }

    private String getAppearancePath(ImageModel imageModel, String materialName) {
        String imageURI = null;
        var data = imageModel.getUri().toString();
        var matcher = GltfHandler.BASE64_PATTERN.matcher(data);
        if (matcher.find()) {
            // 埋め込みだった場合は外部ファイルに出力する
            // TODO:ファイルの出力場所 (とりえあずgLTFと同じ場所に出力している)
            var appearanceFile = new File(_inputFile.getParent().toFile().getAbsolutePath(), materialName);
            try (FileOutputStream stream = new FileOutputStream(appearanceFile, false)) {
                stream.write(Base64.getDecoder().decode(matcher.group("value")));
                imageURI = appearanceFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            imageURI = data;
        }

        return imageURI;
    }
}
