package org.plateau.plateaubuilder.io.gml;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.PhongMaterial;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.plateau.plateaubuilder.citymodel.AppearanceView;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.citymodel.SurfaceDataView;
import org.plateau.plateaubuilder.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TextureExporter {
    public static void export(String folderPath, CityModelView cityModel) {
        AppearanceView tempAppearance = cityModel.getRGBTextureAppearance();
        if(tempAppearance == null)
            return;
        ArrayList<SurfaceDataView> tempSurfaceDatas = tempAppearance.getSurfaceData();
        String appearanceDirName = "";
        int count = 0;

        for (SurfaceDataView surfaceData : tempSurfaceDatas) {
            if (surfaceData.getGML().getCityGMLClass() != CityGMLClass.PARAMETERIZED_TEXTURE)
                continue;

            var parameterizedTexture = (ParameterizedTexture) surfaceData.getGML();
            var imageURI = parameterizedTexture.getImageURI();

            if (count == 0) {
                var filePathComponents = imageURI.split("/");
                appearanceDirName = filePathComponents[0];
                try {
                    if (new File(folderPath + "/" + appearanceDirName).exists()) {
                        FileUtils.deleteDirectory(Paths.get(folderPath + "/" + appearanceDirName));
                    }
                    Files.createDirectory(Paths.get(folderPath + "/" + appearanceDirName));
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            PhongMaterial material = (PhongMaterial) surfaceData.getMaterial();
            var diffuseMap = material.getDiffuseMap();
            var textureImage = SwingFXUtils.fromFXImage(diffuseMap, null);
            try {
                File exportPath = new File(folderPath + "/" + imageURI);
                // ファイル名を抽出
                String fileName = exportPath.getAbsolutePath()
                        .substring(exportPath.getAbsolutePath().lastIndexOf("\\") + 1);
                // '.' 以降（ファイルの拡張子）を抽出
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (extension.matches("jpg") || extension.matches("jpeg")) {
                    ImageIO.write(textureImage, "jpg", exportPath);
                } else if (extension.matches("png")) {
                    ImageIO.write(textureImage, "png", exportPath);
                } else {
                    System.out.println(fileName);
                    ImageIO.write(textureImage, "png", exportPath);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            count++;
        }
    }
}