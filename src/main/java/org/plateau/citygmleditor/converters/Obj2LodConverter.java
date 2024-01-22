package org.plateau.citygmleditor.converters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.converters.model.TriangleModel;
import org.plateau.citygmleditor.importers.obj.ObjImporter;

import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;

public class Obj2LodConverter extends AbstractLodConverter {
    private ObjImporter _objImporter;

    private String _meshKey;

    public Obj2LodConverter(CityModelView cityModelView, ILODSolidView lodSolidView) {
        super(cityModelView, lodSolidView);
    }

    @Override protected void initialize(String fileUrl) throws IOException, URISyntaxException {
        _objImporter = new ObjImporter(Paths.get(fileUrl).toUri().toURL().toString());
        var meshKeys = _objImporter.getMeshes();
        if (meshKeys.size() == 0) {
            throw new IllegalArgumentException("No mesh found in the file.");
        }
        if (meshKeys.size() > 1) {
            throw new IllegalArgumentException("Multiple meshes found in the file.");
        }

        _meshKey = meshKeys.iterator().next();
    }

    @Override protected ParameterizedTexture createParameterizedTexture() throws IOException, URISyntaxException {
        Map<String, ParameterizedTexture> textureMap = new HashMap<>();
        Material material = _objImporter.getMaterial(_meshKey);

        return createOrGetParameterizedTexture(_meshKey, material, textureMap);
    }

    @Override protected List<TriangleModel> createTriangleModels() {
        TriangleMesh triangleMesh = _objImporter.getMesh(_meshKey);
        var faces = triangleMesh.getFaces();
        var vertices = triangleMesh.getPoints();
        var coords = triangleMesh.getTexCoords();
        List<TriangleModel> triangleModels = new LinkedList<>();
        for (var i = 0; i < faces.size(); i += 6) {
            var triangleModel = new TriangleModel(faces, i, vertices, coords);

            // 不正な三角形は除外する
            if (triangleModel.isValid()) {
                triangleModels.add(triangleModel);
            }
        }

        return triangleModels;
    }

    @Override protected TriangleModel getGroundTriangle(List<TriangleModel> triangleModels) {
        TriangleModel groundTriangle = null;
        var groundBaseTriangle = TriangleModel.CreateGroundTriangle();
        for (var triangleModel : triangleModels) {
            // 地面の基準になる三角形を特定する
            // 基準となる三角形の平面との角度が180±5度以下のもののうち、最もZ座標が小さいものを採用する(GroundSurfaceの法線は地面の方を向いている)
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

        return groundTriangle;
    }

    private ParameterizedTexture createOrGetParameterizedTexture(String materialName, Material material, Map<String, ParameterizedTexture> textureMap) throws IOException, URISyntaxException {
        if (textureMap.containsKey(materialName)) return textureMap.get(materialName);
        if (!(material instanceof PhongMaterial)) return null;
        PhongMaterial phongMaterial = (PhongMaterial) material;

        var image = phongMaterial.getDiffuseMap();
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
        textureMap.put(materialName, parameterizedTexture);

        return parameterizedTexture;
    }
}
