package org.plateaubuilder.core.io.gml;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.PhongMaterial;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.plateaubuilder.core.citymodel.AppearanceView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class TextureExporter {
    public static String export(String folderPath, CityModelView cityModel) {
        AppearanceView tempAppearance = cityModel.getAppearance();
        if(tempAppearance == null)
            return null;
        ArrayList<SurfaceDataView> tempSurfaceDatas = tempAppearance.getSurfaceData();
        String appearanceDirName = "";

        for (SurfaceDataView surfaceData : tempSurfaceDatas) {
            if (surfaceData.getGML().getCityGMLClass() != CityGMLClass.PARAMETERIZED_TEXTURE)
                continue;

            var parameterizedTexture = (ParameterizedTexture) surfaceData.getGML();
            var imageURI = parameterizedTexture.getImageURI();

            // 相対パスで指定されているものを利用してフォルダを特定する
            if (Paths.get(imageURI).isAbsolute())
                continue;

            var filePathComponents = imageURI.split("/");
            appearanceDirName = filePathComponents[0];
            try {
                if (new File(folderPath + "/" + appearanceDirName).exists()) {
                    FileUtils.deleteDirectory(Paths.get(folderPath + "/" + appearanceDirName));
                }
                Files.createDirectories(Paths.get(folderPath + "/" + appearanceDirName));
            } catch (IOException e) {
                System.out.println(e);
            }
            break;
        }

        for (SurfaceDataView surfaceData : tempSurfaceDatas) {
            if (surfaceData.getGML().getCityGMLClass() != CityGMLClass.PARAMETERIZED_TEXTURE)
                continue;

            var parameterizedTexture = (ParameterizedTexture) surfaceData.getGML();
            var imageURI = parameterizedTexture.getImageURI();

            // 相対パスで指定されているものは他のものと同じフォルダになるようにパスを差し替える
            if (Paths.get(imageURI).isAbsolute()) {
                var filePathComponents = imageURI.split("/");
                imageURI = appearanceDirName + "/" + filePathComponents[filePathComponents.length - 1];
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
        }

        return appearanceDirName;
    }
}