package org.plateaubuilder.core.citymodel.factory;

import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.plateaubuilder.core.citymodel.AppearanceView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.SurfaceType;

import java.nio.file.Paths;

public class AppearanceFactory extends CityGMLFactory {

    public AppearanceFactory(CityModelView target) {
        super(target);
    }

    public AppearanceView createAppearance(AppearanceMember gmlObject) {
        var appearance = new AppearanceView(gmlObject.getAppearance());

        var surfaceDataMembers = gmlObject.getAppearance().getSurfaceDataMember();
        for (var surfaceData : surfaceDataMembers) {
            if (surfaceData.getSurfaceData().getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
                appearance.getSurfaceData().add(createParameterizedTexture((ParameterizedTexture) surfaceData.getSurfaceData()));
            } else if (surfaceData.getSurfaceData().getCityGMLClass() == CityGMLClass.X3D_MATERIAL) {
                appearance.getSurfaceData().add(createX3DMaterial((X3DMaterial) surfaceData.getSurfaceData()));
            }
        }

        return appearance;
    }

    private SurfaceDataView createParameterizedTexture(ParameterizedTexture parameterizedTexture) {
        var imageRelativePath = Paths.get(parameterizedTexture.getImageURI());
        var imageAbsolutePath = Paths.get(getTarget().getGmlPath()).getParent().resolve(imageRelativePath);

        // サイズが大きすぎるテクスチャがあると極端にパフォーマンス落ちるためリサイズ
        // TODO: 元の解像度で表示
        var image = new Image(imageAbsolutePath.toUri().toString(), 256, 256, true, false);
        var material = new PhongMaterial();
        material.setDiffuseMap(image);

        var surfaceData = new SurfaceDataView(parameterizedTexture, SurfaceType.Texture);
        surfaceData.setMaterial(material);
        for (var target : parameterizedTexture.getTarget()) {
            if (target.getTextureParameterization().getCityGMLClass() == CityGMLClass.TEX_COORD_LIST) {
                var textureParameter = (TexCoordList) target.getTextureParameterization();
                for (var texCoords :textureParameter.getTextureCoordinates()) {
                    var coords = new float[texCoords.getValue().size()];
                    for (int i = 0; i < coords.length; ++i) {
                        coords[i] = texCoords.getValue().get(i).floatValue();
                    }

                    surfaceData.getTextureCoordinatesByRing().put(texCoords.getRing(), coords);
                }
            }
        }

        return surfaceData;
    }

    private SurfaceDataView createX3DMaterial(X3DMaterial x3dMaterial) {
        var material = new PhongMaterial();
        if (x3dMaterial.isSetDiffuseColor()) {
            var diffuseColor = x3dMaterial.getDiffuseColor();
            material.setDiffuseColor(javafx.scene.paint.Color.color(diffuseColor.getRed(), diffuseColor.getGreen(), diffuseColor.getBlue()));
        }
        if (x3dMaterial.isSetSpecularColor()) {
            var specularColor = x3dMaterial.getSpecularColor();
            material.setSpecularColor(javafx.scene.paint.Color.color(specularColor.getRed(), specularColor.getGreen(), specularColor.getBlue()));
        }
        if (x3dMaterial.isSetEmissiveColor()) {
            var emissiveColor = x3dMaterial.getEmissiveColor();
            // TODO: 何に設定したらよいか不明
        }

        var surfaceData = new SurfaceDataView(x3dMaterial, SurfaceType.X3D);
        surfaceData.setMaterial(material);
        for (var target : x3dMaterial.getTarget()) {
            surfaceData.getTargetSet().add(target);
        }

        return surfaceData;
    }
}
