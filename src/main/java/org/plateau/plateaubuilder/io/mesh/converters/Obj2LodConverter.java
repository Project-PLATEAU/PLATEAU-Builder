package org.plateau.plateaubuilder.io.mesh.converters;

import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.io.mesh.converters.model.TriangleModel;
import org.plateau.plateaubuilder.io.mesh.importers.ObjImporter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Obj2LodConverter extends AbstractLodConverter {

    private ObjImporter _objImporter;

    public Obj2LodConverter(CityModelView cityModelView, BuildingView buildingView, int lod, ConvertOption convertOption) {
        super(cityModelView, buildingView, lod, convertOption, false);
    }

    @Override protected void initialize(String fileUrl) throws IOException, URISyntaxException {
        _objImporter = new ObjImporter(Paths.get(fileUrl).toUri().toURL().toString());
        var meshKeys = _objImporter.getMeshes();
        if (meshKeys.size() == 0) {
            throw new IllegalArgumentException("No mesh found in the file.");
        }
    }

    @Override protected Map<String, ParameterizedTexture> createParameterizedTextures() throws IOException, URISyntaxException {
        Map<String, ParameterizedTexture> textureMap = new HashMap<>();
        var meshKeys = _objImporter.getMeshes();
        for(var meshKey : meshKeys) {
            Material material = _objImporter.getMaterial(meshKey);
            if (textureMap.containsKey(meshKey)) continue;
            ParameterizedTexture parameterizedTexture = createParameterizedTexture(meshKey, material);
            if (parameterizedTexture == null) continue;
            textureMap.put(meshKey, parameterizedTexture);
        }

        return textureMap;
    }

    @Override protected Map<String, List<TriangleModel>> createTriangleModelsMap() {
        Map<String, List<TriangleModel>> triangleModelsMap = new HashMap<>();
        var meshKeys = _objImporter.getMeshes();
        for(var meshKey : meshKeys) {
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
                var triangleModel = new TriangleModel(faces, i, vertices, coords, true);

                // 不正な三角形は除外する
                if (triangleModel.isValid()) {
                    triangleModels.add(triangleModel);
                }
            }
            triangleModelsMap.put(meshKey, triangleModels);
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

    private ParameterizedTexture createParameterizedTexture(String materialName, Material material) throws IOException, URISyntaxException {
        if (!(material instanceof PhongMaterial)) return null;
        PhongMaterial phongMaterial = (PhongMaterial) material;

        var image = phongMaterial.getDiffuseMap();
        if (image == null)  {
            return null;
        }

        var path = Paths.get(new URL(image.getUrl()).toURI());
        var fileName = path.getFileName().toString();
        var gmlFileName = Paths.get(getCityModelView().getGmlPath()).getFileName().toString();

        String texturePath = null;
        var codes = gmlFileName.split("_");
        if (codes.length == 5) {
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
