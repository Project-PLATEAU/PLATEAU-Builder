package org.plateau.citygmleditor.converters;

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

import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.converters.model.TriangleModel;

import de.javagl.jgltf.model.AccessorData;
import de.javagl.jgltf.model.AccessorModel;
import de.javagl.jgltf.model.GltfModel;
import de.javagl.jgltf.model.ImageModel;
import de.javagl.jgltf.model.MaterialModel;
import de.javagl.jgltf.model.TextureModel;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;

public class Gltf2LodConverter extends AbstractLodConverter {
    private static Pattern BASE64_PATTERN = Pattern.compile("^data:.+;base64,(?<value>.+)$");

    private GltfModelReader _reader = new GltfModelReader();

    private Path _inputFile;

    private GltfModel _gltfModel;

    public Gltf2LodConverter(CityModelView cityModelView, ILODSolidView lodSolidView) {
        super(cityModelView, lodSolidView);
    }

    @Override protected void initialize(String fileUrl) throws IOException, URISyntaxException {
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

    @Override protected Map<String, ParameterizedTexture> createParameterizedTextures() throws IOException, URISyntaxException {
        Map<String, ParameterizedTexture> textureMap = new HashMap<>();
        var sceneModel = _gltfModel.getSceneModels().get(0);
        for(var nodeModel : sceneModel.getNodeModels()) {
            for (var meshModel : nodeModel.getMeshModels()) {
                for (var meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
                    var materialModel = meshPrimitiveModel.getMaterialModel();
                    var materialName = materialModel.getName();
                    if (textureMap.containsKey(materialName)) continue;
                    var parameterizedTexture = createParameterizedTexture(materialModel, textureMap);
                    if (parameterizedTexture == null) continue;
                    textureMap.put(materialName, parameterizedTexture);
                }
            }
        }

        return textureMap;
    }

    @Override protected Map<String, List<TriangleModel>> createTriangleModelsMap() {
        Map<String, List<TriangleModel>> triangleModelsMap = new HashMap<>();
        var sceneModel = _gltfModel.getSceneModels().get(0);
        for(var nodeModel : sceneModel.getNodeModels()) {
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

                    for (var i = 0; i < faces.length; i += 6) {
                        var triangleModel = new TriangleModel(faces, i, vertices, uvs);

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

        return triangleModelsMap;
    }

    @Override protected TriangleModel getGroundTriangle(Map<String, List<TriangleModel>> triangleModelsMap) {
        TriangleModel groundTriangle = null;
        var groundBaseTriangle = TriangleModel.CreateGroundTriangle();

        // 地面の基準になる三角形を特定する
        // 基準となる三角形の平面との角度が180±5度以下のもののうち、最もZ座標が小さいものを採用する(GroundSurfaceの法線は地面の方を向いている)
        for (var triangleModels : triangleModelsMap.values()) {
            for (var triangleModel : triangleModels) {
                double angle = Math.toDegrees(groundBaseTriangle.getNormal().angle(triangleModel.getNormal()));
                if (angle >= 175 && angle <= 185) {
                    if (groundTriangle == null) {
                        groundTriangle = triangleModel;
                    } else {
                        if (triangleModel.getMinZ() < groundTriangle.getMinZ()) {
                            groundTriangle = triangleModel;
                        }
                    }
                }
            }
        }

        return groundTriangle;
    }

    private ParameterizedTexture createParameterizedTexture(MaterialModel materialModel, Map<String, ParameterizedTexture> textureMap) {
        var materialName = materialModel.getName();
        MaterialModelV2 materialModelV2 = (MaterialModelV2) materialModel;
        TextureModel textureModel = materialModelV2.getBaseColorTexture();
        if (textureModel == null)
            return null;

        ImageModel imageModel = textureModel.getImageModel();
        String imageURI = null;
        var data = imageModel.getUri().toString();
        var matcher = BASE64_PATTERN.matcher(data);
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

        ParameterizedTexture parameterizedTexture = new ParameterizedTexture();
        parameterizedTexture.setImageURI(imageURI);
        parameterizedTexture.setMimeType(new Code(imageModel.getMimeType()));
        textureMap.put(materialName, parameterizedTexture);

        return parameterizedTexture;
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
            vertices[i] = floatBuffer.get(i + 2);
            vertices[i + 1] = floatBuffer.get(i);
            vertices[i + 2] = floatBuffer.get(i + 1);
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
}
