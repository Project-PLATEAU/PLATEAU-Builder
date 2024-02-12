package org.plateau.citygmleditor.exporters;

import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.CityGMLClass;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.AppearanceView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.utils.*;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.PhongMaterial;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import java.nio.file.Paths;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class TextureExporter {
    public static void export(String folderPath, CityModelView cityModel) {
        // ImageURIの取得
        List<String> appearanceList = new ArrayList<>();
        org.citygml4j.model.citygml.core.CityModel gmlObject = cityModel.getGmlObject();
        // appearanceがない場合は終了
        if (gmlObject.getAppearanceMember().size() == 0) {
            return;
        }
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
        AppearanceView tempAppearance = cityModel.getRGBTextureAppearance();
        if(tempAppearance == null)
            return;
        ArrayList<SurfaceDataView> tempSurfaceDatas = tempAppearance.getSurfaceData();
        String appearanceDirName = "";
        int count = 0;

        for (SurfaceDataView surfaceData : tempSurfaceDatas) {
            if (count == 0) {
                var filePathComponents = appearanceList.get(0).split("/");
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
                File exportPath = new File(folderPath + "/" + appearanceList.get(count));
                // 最後の '\' 以降を抽出
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