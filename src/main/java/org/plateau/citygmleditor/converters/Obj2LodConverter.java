package org.plateau.citygmleditor.converters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateau.citygmleditor.citymodel.geometry.BoundarySurface;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolid;
import org.plateau.citygmleditor.citymodel.geometry.LOD2Solid;
import org.plateau.citygmleditor.importers.obj.ObjImporter;

import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;

public class Obj2LodConverter {
    public static ILODSolid convert(String fileUrl) throws Exception {
        var reader = new ObjImporter(Paths.get(fileUrl).toUri().toURL().toString());
        var meshKeys = reader.getMeshes();

        CompositeSurface compositeSurface = new CompositeSurface();
        ArrayList<BoundarySurface> boundedBy = new ArrayList<BoundarySurface>();

        // TODO:ObjModel→CityModel変換
        Map<String, ParameterizedTexture> textureMap = new HashMap<>();
        for (var key : meshKeys) {
            TriangleMesh triangleMesh = reader.getMesh(key);

            // TODO:Triangle → Polygon

            Material material = reader.getMaterial(key);
            ParameterizedTexture parameterizedTexture = createOrGetParameterizedTexture(key, material, textureMap);
            if (parameterizedTexture == null) continue;

            // TODO:Triangle → 画像の座標
        }

        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(compositeSurface));
        LOD2Solid lod2Solid = new LOD2Solid(solid);
        lod2Solid.setBoundaries(boundedBy);

        return lod2Solid;
    }

    private static ParameterizedTexture createOrGetParameterizedTexture(String materialName, Material material, Map<String, ParameterizedTexture> textureMap) throws IOException, URISyntaxException {
        if (textureMap.containsKey(materialName)) return textureMap.get(materialName);
        if (!(material instanceof PhongMaterial)) return null;
        PhongMaterial phongMaterial = (PhongMaterial) material;

        var image = phongMaterial.getDiffuseMap();
        var texturePath = Paths.get(new URL(image.getUrl()).toURI());
        ParameterizedTexture parameterizedTexture = new ParameterizedTexture();
        parameterizedTexture.setImageURI(texturePath.toString());
        parameterizedTexture.setMimeType(new Code(Files.probeContentType(texturePath)));
        textureMap.put(materialName, parameterizedTexture);

        return parameterizedTexture;
    }
}
