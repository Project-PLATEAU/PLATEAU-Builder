package org.plateau.citygmleditor.exporters;

import org.citygml4j.CityGMLContext;
import org.citygml4j.ade.iur.UrbanRevitalizationADEContext;
import org.citygml4j.builder.copy.CopyBuilder;
import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;
import org.citygml4j.util.gmlid.GMLIdManager;
import org.citygml4j.xml.io.CityGMLOutputFactory;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityGMLWriter;
import org.citygml4j.xml.io.writer.FeatureWriteMode;
import org.citygml4j.xml.schema.SchemaHandler;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceMember;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.Appearance;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.SurfaceData;
import org.plateau.citygmleditor.citymodel.factory.AppearanceFactory;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.PhongMaterial;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.awt.image.BufferedImage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TextureExporter {
    public static void export(String folderPath, CityModel cityModel) {
        // ImageURIの取得
        List<String> appearanceList = new ArrayList<>();
        org.citygml4j.model.citygml.core.CityModel gmlObject = cityModel.getGmlObject();
        for (var appearanceMember : gmlObject.getAppearanceMember()) {
            var surfaceDataMembers = appearanceMember.getAppearance().getSurfaceDataMember();
            for (var surfaceData : surfaceDataMembers) {
                ParameterizedTexture parameterizedTexture;
                if (surfaceData.getSurfaceData().getCityGMLClass() == CityGMLClass.PARAMETERIZED_TEXTURE) {
                    parameterizedTexture = (ParameterizedTexture) surfaceData.getSurfaceData();
                    var imageURI = parameterizedTexture.getImageURI();
                    appearanceList.add(imageURI);
                }
            }
        }

        // テクスチャのエクスポート
        Appearance tempAppearance = cityModel.getRGBTextureAppearance();
        ArrayList<SurfaceData> tempSurfaceDatas = tempAppearance.getSurfaceData();
        String appearanceDirName = "";
        int count = 0;
        for (SurfaceData surfaceData : tempSurfaceDatas) {
            if (count == 0) {
                var filePathComponents = appearanceList.get(0).split("/");
                appearanceDirName = filePathComponents[0];
                try {
                    Files.createDirectory(Paths.get(folderPath + "/" + appearanceDirName));
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            PhongMaterial material = (PhongMaterial) surfaceData.getMaterial();
            var diffuseMap = material.getDiffuseMap();
            var textureImage = SwingFXUtils.fromFXImage(diffuseMap, null);
            try {
                File exportPath = new File(folderPath + "/" + appearanceList.get(count));
                ImageIO.write(textureImage, "jpg", exportPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
    }
}