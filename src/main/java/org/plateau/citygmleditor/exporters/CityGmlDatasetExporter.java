package org.plateau.citygmleditor.exporters;

import javafx.scene.Group;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.utils.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CityGmlDatasetExporter {
    public void export(CityModelView cityModelView) {
        if (cityModelView == null || cityModelView.getGmlObject() == null)
            return;

        String rootDirName;// エクスポート先のルートフォルダの名前
        String udxDirName = "udx";
        String bldgDirName = "bldg";
        String defaultDirName;
        Optional<String> textDialogResult;
        TextInputDialog textDialog;
        String headerText = "■フォルダ名は以下形式で設定してください。 (3D 都市モデル標準製品仕様書 第 3.0 版に基づく)\n[都市コード]_[都市名英名]_[提供者区分]_[整備年度]_citygml_[更新回数]_[オプション]_[op(オープンデータ)]";

        var gmlPath = cityModelView.getGmlPath();
        var importGmlPathComponents = gmlPath.split("\\\\");
        var sourceRootDirPath = Paths.get(gmlPath).getParent().getParent().getParent();

        // インポートしたCityGMLのルートフォルダネームを_で分解
        String[] destRootDirComponents = importGmlPathComponents[importGmlPathComponents.length - 4].split("_");

        // ダイアログで表示される初期のフォルダ名を指定
        defaultDirName = importGmlPathComponents[importGmlPathComponents.length - 4];
        // テキスト入力ダイアログを表示し、ユーザーにフォルダ名を入力させる(元のフォルダ名を表示)
        textDialog = new TextInputDialog(defaultDirName);
        textDialog.setTitle("フォルダ名を入力してください");
        textDialog.setHeaderText(headerText);
        textDialog.setContentText("ルートフォルダ名：");
        textDialogResult = textDialog.showAndWait();
        rootDirName = textDialog.getResult();

        if (textDialogResult.isPresent()) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(Paths.get(gmlPath).getParent().toFile());// 初期ディレクトリ指定
            // 初期ディレクトリを設定（オプション）
            // directoryChooser.setInitialDirectory(new File("/path/to/initial/directory"));
            directoryChooser.setTitle("Export CityGML");
            var selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {

                // インポート元からエクスポート先のフォルダへコピー
                try {
                    String skipPath = sourceRootDirPath.toString().replace("\\", "\\\\") + "\\\\udx\\\\.*";
                    if (!FileUtils.copyDirectory(
                            sourceRootDirPath,
                            Paths.get(selectedDirectory.getAbsolutePath() + "\\\\" + rootDirName),skipPath) )
                        return;
                } catch (IOException e) {
                    System.out.println(e);
                }

                try {
                    // CityGMLのエクスポート
                    GmlExporter.export(
                            Paths.get(selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/" +
                                            udxDirName + "/"
                                            + bldgDirName
                                            + "/" + importGmlPathComponents[importGmlPathComponents.length - 1])
                                    .toString(),
                            cityModelView.getGmlObject(),
                            cityModelView.getSchemaHandler());
                    // Appearanceのエクスポート
                    TextureExporter.export(
                            selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/" + udxDirName + "/"
                                    + bldgDirName,
                            cityModelView);
                } catch (ADEException | CityGMLWriteException | CityGMLBuilderException e) {
                    throw new RuntimeException(e);
                }
                // エクスポート後にフォルダを開く
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(new File(selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
