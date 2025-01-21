package org.plateaubuilder.core.io.gml;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.utils.FileUtils;

import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;

public class CityGmlDatasetExporter {
    public void export(List<CityModelView> cityModelViews) {
        if (cityModelViews == null || cityModelViews.isEmpty() || cityModelViews.get(0).getGML() == null)
            return;

        String rootDirName;// エクスポート先のルートフォルダの名前
        String udxDirName = "udx";
        String defaultDirName;
        Optional<String> textDialogResult;
        TextInputDialog textDialog;
        String headerText = "■フォルダ名は以下形式で設定してください。 (3D 都市モデル標準製品仕様書 第 3.0 版に基づく)\n[都市コード]_[都市名英名]_[提供者区分]_[整備年度]_citygml_[更新回数]_[オプション]_[op(オープンデータ)]";

        var gmlPath = cityModelViews.get(0).getGmlPath();
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
                            Paths.get(selectedDirectory.getAbsolutePath() + "\\\\" + rootDirName), skipPath))
                        return;
                } catch (IOException e) {
                    System.out.println(e);
                }
                for (var cityModelView : cityModelViews) {
                    var path = cityModelView.getGmlPath().split("\\\\");
                    try {
                        // ファイル名からフィーチャータイプを抽出
                        String dirName = path[path.length - 1].split("_")[1];
                        // CityGMLのエクスポート
                        GmlExporter.export(
                                Paths.get(selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/" +
                                        udxDirName + "/"
                                        + dirName
                                        + "/" + path[path.length - 1])
                                        .toString(),
                                cityModelView.getGML(),
                                cityModelView.getSchemaHandler());
                        // Appearanceのエクスポート
                        TextureExporter.export(
                                selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/" + udxDirName + "/"
                                        + dirName,
                                cityModelView);
                    } catch (ADEException | CityGMLWriteException | CityGMLBuilderException e) {
                        throw new RuntimeException(e);
                    }
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
