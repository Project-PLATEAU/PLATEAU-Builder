package org.plateau.citygmleditor.citygmleditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.exporters.GmlExporter;
import org.plateau.citygmleditor.exporters.TextureExporter;
import org.plateau.citygmleditor.importers.gltf.GltfImporter;
import org.plateau.citygmleditor.importers.gml.GmlImporter;
import org.plateau.citygmleditor.world.SceneContent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TopPanelController {
    private File loadedPath;
    private String loadedURL;
    private String sourceRootDirPath;
    private String[] importGmlPathComponents;
    private SessionManager sessionManager = SessionManager.getSessionManager();
    private final SceneContent sceneContent = CityGMLEditorApp.getSceneContent();

    public void importGml(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", "*.gml"));
        if (loadedPath != null) {
            chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
        }
        chooser.setTitle("Select file to load");
        File newFile = chooser.showOpenDialog(CityGMLEditorApp.getScene().getWindow());
        if (newFile != null) {
            loadGml(newFile.toString());
        }
    }

    private void loadGml(String fileUrl) {
        try {
            try {
                loadedPath = new File(new URL(fileUrl).toURI()).getAbsoluteFile();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ignored) {
                loadedPath = null;
            }
            doLoadGml(fileUrl);
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doLoadGml(String fileUrl) {
        loadedURL = fileUrl;
        importGmlPathComponents = fileUrl.split("\\\\");
        sourceRootDirPath = new File(new File(new File(loadedURL).getParent()).getParent()).getParent();
        sessionManager.getProperties().setProperty(CityGMLEditorApp.FILE_URL_PROPERTY, fileUrl);
        try {
            var root = GmlImporter.loadGml(fileUrl);
            sceneContent.setContent(root);
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//
//    public void open(ActionEvent actionEvent) {
//        FileChooser chooser = new FileChooser();
//        chooser.getExtensionFilters().add(
//                new FileChooser.ExtensionFilter("Supported files", Importer3D.getSupportedFormatExtensionFilters()));
//        if (loadedPath != null) {
//            chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
//        }
//        chooser.setTitle("Select file to load");
//        File newFile = chooser.showOpenDialog(openMenuBtn.getScene().getWindow());
//        if (newFile != null) {
//            load(newFile);
//        }
//    }
//
//    private void load(File file) {
//        loadedPath = file;
//        try {
//            doLoad(file.toURI().toURL().toString());
//        } catch (Exception ex) {
//            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void load(String fileUrl) {
//        try {
//            try {
//                loadedPath = new File(new URL(fileUrl).toURI()).getAbsoluteFile();
//            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ignored) {
//                loadedPath = null;
//            }
//            doLoad(fileUrl);
//        } catch (Exception ex) {
//            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    private void doLoad(String fileUrl) {
//        loadedURL = fileUrl;
//        sessionManager.getProperties().setProperty(CityGMLEditorApp.FILE_URL_PROPERTY, fileUrl);
//        try {
//            Node root = Importer3D.load(
//                    fileUrl, loadAsPolygonsCheckBox.isSelected());
//            sceneContent.setContent(root);
//        } catch (IOException ex) {
//            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        updateStatus();
//    }

//    public void toggleSettings(ActionEvent event) {
//        final SplitPane.Divider divider = splitPane.getDividers().get(0);
//        if (settingsBtn.isSelected()) {
//            if (settingsLastWidth == -1) {
//                settingsLastWidth = sidebar.prefWidth(-1);
//            }
//            final double divPos = 1 - (settingsLastWidth / splitPane.getWidth());
//            new Timeline(
//                    new KeyFrame(Duration.seconds(0.3),
//                            new EventHandler<ActionEvent>() {
//                                @Override
//                                public void handle(ActionEvent event) {
//                                    sidebar.setMinWidth(Region.USE_PREF_SIZE);
//                                }
//                            },
//                            new KeyValue(divider.positionProperty(), divPos, Interpolator.EASE_BOTH)))
//                    .play();
//        } else {
//            settingsLastWidth = sidebar.getWidth();
//            sidebar.setMinWidth(0);
//            new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(divider.positionProperty(), 1))).play();
//        }
//    }

    public void export(ActionEvent event) {

        String[] destRootDirComponents = importGmlPathComponents[importGmlPathComponents.length - 4].split("_");// インポートしたcitygmlのルートフォルダネームを_で分解
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
        if (loadedPath != null) {
            directoryChooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());// 初期ディレクトリ指定
        }
        // 初期ディレクトリを設定（オプション）
        // directoryChooser.setInitialDirectory(new File("/path/to/initial/directory"));
        directoryChooser.setTitle("Export CityGML");
        var selectedDirectory = directoryChooser.showDialog(null);

        // インポート元からエクスポート先のフォルダへコピー
        try {
            folderCopy(Paths.get(sourceRootDirPath),
                    Paths.get(selectedDirectory.getAbsolutePath() + "/" + rootDirName + "/"));
        } catch (IOException e) {
            System.out.println(e);
        }

        var content = (Group) sceneContent.getContent();
        var cityModelNode = content.getChildren().get(0);

        if (cityModelNode == null)
            return;

        var cityModel = (CityModelView)cityModelNode;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("validation.fxml"));
        newWindow.setScene(new Scene(loader.load()));
        newWindow.showAndWait();
    }

    public void openGltf(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", "*.gltf"));
        if (loadedPath != null) {
            chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
        }
        chooser.setTitle("Select file to load");
        File newFile = chooser.showOpenDialog(CityGMLEditorApp.getScene().getWindow());
        if (newFile != null) {
            loadGltf(newFile.toString());
        }
    }


    private void loadGltf(String fileUrl) {
        try {
            try {
                loadedPath = new File(new URL(fileUrl).toURI()).getAbsoluteFile();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ignored) {
                loadedPath = null;
            }
            doLoadGltf(fileUrl);
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doLoadGltf(String fileUrl) {
        loadedURL = fileUrl;
        sessionManager.getProperties().setProperty(CityGMLEditorApp.FILE_URL_PROPERTY, fileUrl);
        try {
            var root = GltfImporter.loadGltf(fileUrl);
            if (root != null) {
                sceneContent.setContent(root);
            }
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // フォルダコピーメソッド(udx以下は無視)
    public void folderCopy(Path sourcePath, Path destinationPath) throws IOException {
        String skipPattern = sourceRootDirPath.replace("\\", "\\\\") + "\\\\udx\\\\.*";
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
