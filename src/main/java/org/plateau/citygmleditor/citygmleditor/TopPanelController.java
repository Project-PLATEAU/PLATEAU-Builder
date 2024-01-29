package org.plateau.citygmleditor.citygmleditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.exporters.GmlExporter;
import org.plateau.citygmleditor.exporters.TextureExporter;
import org.plateau.citygmleditor.importers.gml.GmlImporter;
import org.plateau.citygmleditor.world.World;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class TopPanelController {
    public void importGml(ActionEvent actionEvent) {
        var files = FileChooserService.showOpenDialog("*.gml", SessionManager.GML_FILE_PATH_PROPERTY);

        if (files == null)
            return;

        String epsgCode = World.getActiveInstance().getEPSGCode();
        
        if (epsgCode == null || epsgCode.isEmpty()) {
            CoordinateDialogController.createCoorinateDialog(files);
        }
        else {
            LoadGMLDialogController.createLoadGMLDialog(files);
        }
    }

    public void exportDataset(ActionEvent event) {
        var content = (Group)CityGMLEditorApp.getSceneContent().getContent();
        var cityModelNode = content.getChildren().get(0);

        if (cityModelNode == null)
            return;

        var cityModel = (CityModelView)cityModelNode;
        var gmlPath = cityModel.getGmlPath();

        var importGmlPathComponents = gmlPath.split("\\\\");

        // インポートしたCityGMLのルートフォルダネームを_で分解
        String[] destRootDirComponents = importGmlPathComponents[importGmlPathComponents.length - 4].split("_");
        String cityCode = destRootDirComponents[0];
        String cityName = destRootDirComponents[1];
        String developmentYear = destRootDirComponents[2];
        String updateCount = Integer.toString(Integer.parseInt(destRootDirComponents[4]) + 1);
        String options;
        String rootDirName;// エクスポート先のルートフォルダの名前
        String udxDirName = "udx";
        String bldgDirName = "bldg";

        // ダイアログで表示される初期のフォルダ名を指定
        String defaultDirName = "";
        if (destRootDirComponents[destRootDirComponents.length - 1].equals("op")) {
            if (destRootDirComponents.length == 7) {
                options = destRootDirComponents[5];
                defaultDirName = cityCode + "_" + cityName + "_" + developmentYear + "_" + "citygml" + "_" +
                        updateCount + "_" + options + "_" + "op";
            } else {
                defaultDirName = cityCode + "_" + cityName + "_" + developmentYear + "_" + "citygml" + "_" +
                        updateCount + "_" + "op";
            }
        } else {
            if (destRootDirComponents.length == 6) {
                options = destRootDirComponents[5];
                defaultDirName = cityCode + "_" + cityName + "_" + developmentYear + "_" + "citygml" + "_" +
                        updateCount + "_" + options;
            } else {
                defaultDirName = cityCode + "_" + cityName + "_" + developmentYear + "_" + "citygml" + "_" +
                        updateCount;
            }
        }
        // テキスト入力ダイアログを表示し、ユーザーにフォルダ名を入力させる(仮のフォルダ名は表示)
        TextInputDialog dialog = new TextInputDialog(defaultDirName);
        dialog.setTitle("Input Root Folder Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Folder Name:");

        Optional<String> result = dialog.showAndWait();
        rootDirName = dialog.getResult();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(Paths.get(gmlPath).getParent().toFile());// 初期ディレクトリ指定
        // 初期ディレクトリを設定（オプション）
        // directoryChooser.setInitialDirectory(new File("/path/to/initial/directory"));
        directoryChooser.setTitle("Export CityGML");
        var selectedDirectory = directoryChooser.showDialog(null);

        // インポート元からエクスポート先のフォルダへコピー
        try {
            folderCopy(
                    Paths.get(gmlPath).getParent().getParent().getParent(),
                    Paths.get(selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/"));
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
                    cityModel.getGmlObject(),
                    cityModel.getSchemaHandler());
            // Appearanceのエクスポート
            TextureExporter.export(
                    selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/" + udxDirName + "/" + bldgDirName,
                    cityModel);
        } catch (ADEException | CityGMLWriteException | CityGMLBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public void openValidationWindow(ActionEvent event) throws IOException {
        Stage newWindow = new Stage();
        newWindow.setTitle("品質検査");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/validation.fxml"));
        newWindow.setScene(new Scene(loader.load()));
        newWindow.showAndWait();
    }

    // フォルダコピーメソッド(udx以下は無視)
    public void folderCopy(Path sourcePath, Path destinationPath) throws IOException {
        String skipPattern = sourcePath.toString().replace("\\", "\\\\") + "\\\\udx\\\\.*";
        Files.walk(sourcePath).forEach(path -> {
            if (!path.toString().matches(skipPattern)) {
                try {
                    Files.copy(path, destinationPath.resolve(sourcePath.relativize(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
