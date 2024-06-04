package org.plateaubuilder.core.io.mesh.converters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateaubuilder.core.io.mesh.AxisTransformer;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;
import org.plateaubuilder.core.io.mesh.importers.ObjImporter;

import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;

/**
 * ObjHandlerは、OBJ形式のファイルを処理するためのハンドラクラスです。 このクラスは、OBJファイルから三角形モデルのマップや表面データを作成する機能を提供します。
 */
public class ObjHandler extends Abstract3DFormatHandler {
    private ObjImporter _objImporter;

    private String _gmlFileName;

    /**
     * ObjHandlerのコンストラクタです。
     * 
     * @param axisTransformer AxisTransformerオブジェクト
     */
    public ObjHandler(AxisTransformer axisTransformer, String gmlFileName) {
        super(axisTransformer);
        _gmlFileName = gmlFileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String fileUrl) throws IOException, URISyntaxException {
        _objImporter = new ObjImporter(Paths.get(fileUrl).toUri().toURL().toString());
        var meshKeys = _objImporter.getMeshes();
        if (meshKeys.size() == 0) {
            throw new IllegalArgumentException("No mesh found in the file.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<TriangleModel>> createTriangleModelsMap() {
        Map<String, List<TriangleModel>> triangleModelsMap = new HashMap<>();
        var meshKeys = _objImporter.getMeshes();
        for (var meshKey : meshKeys) {
            TriangleMesh triangleMesh = _objImporter.getMesh(meshKey);
            var faces = triangleMesh.getFaces();
            var vertices = triangleMesh.getPoints();

            for (int i = 0; i < vertices.size(); i += 3) {
                var convertedPoint = convertVertexAxis(vertices.get(i), vertices.get(i + 1), vertices.get(i + 2));
                vertices.set(i, convertedPoint.x);
                vertices.set(i + 1, convertedPoint.y);
                vertices.set(i + 2, convertedPoint.z);
            }

            var coords = triangleMesh.getTexCoords();
            List<TriangleModel> triangleModels = new LinkedList<>();
            for (var i = 0; i < faces.size(); i += 6) {
                var triangleModel = new TriangleModel(faces, i, vertices, coords, true, meshKey);

                // 不正な三角形は除外する
                if (triangleModel.isValid()) {
                    triangleModels.add(triangleModel);
                }
            }
            triangleModelsMap.put(meshKey, triangleModels);
        }

        return triangleModelsMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, AbstractSurfaceData> createSurfaceData() throws IOException, URISyntaxException {
        Map<String, AbstractSurfaceData> surfaceMap = new HashMap<>();
        var meshKeys = _objImporter.getMeshes();
        for (var meshKey : meshKeys) {
            Material material = _objImporter.getMaterial(meshKey);
            if (surfaceMap.containsKey(meshKey))
                continue;
            ParameterizedTexture parameterizedTexture = createParameterizedTexture(meshKey, material);
            if (parameterizedTexture == null)
                continue;
            surfaceMap.put(meshKey, parameterizedTexture);
        }

        return surfaceMap;
    }

    private ParameterizedTexture createParameterizedTexture(String materialName, Material material) throws IOException, URISyntaxException {
        if (!(material instanceof PhongMaterial))
            return null;
        PhongMaterial phongMaterial = (PhongMaterial) material;

        var image = phongMaterial.getDiffuseMap();
        if (image == null) {
            return null;
        }

        var path = Paths.get(new URL(image.getUrl()).toURI());
        var fileName = path.getFileName().toString();

        String texturePath = null;
        var codes = _gmlFileName.split("_");
        if (codes.length >= 4) {
            texturePath = String.format("%s_%s_%s_appearance/%s", codes[0], codes[1], codes[2], fileName);
        } else {
            texturePath = fileName;
        }

        ParameterizedTexture parameterizedTexture = new ParameterizedTexture();
        parameterizedTexture.setImageURI(texturePath.toString());
        parameterizedTexture.setMimeType(new Code(Files.probeContentType(path)));

        return parameterizedTexture;
    }
}
