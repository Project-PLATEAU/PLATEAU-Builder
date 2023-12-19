/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle Corporation nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.FocusEvent;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.exporters.GmlExporter;
import org.plateau.citygmleditor.exporters.TextureExporter;
import org.plateau.citygmleditor.importers.gml.GmlImporter;
import org.plateau.citygmleditor.world.World;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.plateau.citygmleditor.importers.Importer3D;
import org.plateau.citygmleditor.importers.gltf.GltfImporter;

import org.plateau.citygmleditor.importers.Importer3D;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.FieldPosition;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.StyledEditorKit;

import javafx.stage.DirectoryChooser;
import javafx.geometry.BoundingBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ButtonType;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.plateau.citygmleditor.world.*;

/**
 * Controller class for main fxml file.
 */
public class MainController implements Initializable {
    public SplitMenuButton openMenuBtn;
    public Label status;
    public SplitPane splitPane;
    public ToggleButton settingsBtn;
    public CheckMenuItem loadAsPolygonsCheckBox;
    private VBox sidebar;
    private double settingsLastWidth = -1;
    private int nodeCount = 0;
    private int meshCount = 0;
    private int triangleCount = 0;
    private final SceneContent sceneContent = CityGMLEditorApp.getSceneContent();
    private File loadedPath;
    private String loadedURL;
    private String[] supportedFormatRegex;
    private SessionManager sessionManager = SessionManager.getSessionManager();
    private String sourceRootDirPath;
    private String[] importGmlPathComponents;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // CREATE NAVIGATOR CONTROLS
            Parent navigationPanel = FXMLLoader.load(MainController.class.getResource("navigation.fxml"));
            // CREATE SETTINGS PANEL
            sidebar = FXMLLoader.load(MainController.class.getResource("sidebar.fxml"));
            // SETUP SPLIT PANE
            splitPane.getItems().addAll(new SubSceneResizer(sceneContent.subSceneProperty(), navigationPanel),
                    sidebar);
            splitPane.getDividers().get(0).setPosition(1);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        // listen for drops
        supportedFormatRegex = Importer3D.getSupportedFormatExtensionFilters();
        for (int i = 0; i < supportedFormatRegex.length; i++) {
            supportedFormatRegex[i] = "." + supportedFormatRegex[i].replaceAll("\\.", "\\.");
        }
        sceneContent.getSubScene().setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                boolean hasSupportedFile = false;
                fileLoop: for (File file : db.getFiles()) {
                    for (String format : supportedFormatRegex) {
                        if (file.getName().matches(format)) {
                            hasSupportedFile = true;
                            break fileLoop;
                        }
                    }
                }
                if (hasSupportedFile)
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        sceneContent.getSubScene().setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File supportedFile = null;
                fileLoop: for (File file : db.getFiles()) {
                    for (String format : supportedFormatRegex) {
                        if (file.getName().matches(format)) {
                            supportedFile = file;
                            break fileLoop;
                        }
                    }
                }
                if (supportedFile != null) {
                    // workaround for RT-30195
                    if (supportedFile.getAbsolutePath().indexOf('%') != -1) {
                        supportedFile = new File(URLDecoder.decode(supportedFile.getAbsolutePath()));
                    }
                    load(supportedFile);
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        sessionManager.bind(settingsBtn.selectedProperty(), "settingsBtn");
        sessionManager.bind(splitPane.getDividers().get(0).positionProperty(), "settingsSplitPanePosition");
        sessionManager.bind(loadAsPolygonsCheckBox.selectedProperty(), "loadAsPolygons");

        // do initial status update
        updateStatus();
    }

    public void openGml(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", "*.gml"));
        if (loadedPath != null) {
            chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
        }
        chooser.setTitle("Select file to load");
        File newFile = chooser.showOpenDialog(openMenuBtn.getScene().getWindow());
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
        updateStatus();
    }

    public void open(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Supported files", Importer3D.getSupportedFormatExtensionFilters()));
        if (loadedPath != null) {
            chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
        }
        chooser.setTitle("Select file to load");
        File newFile = chooser.showOpenDialog(openMenuBtn.getScene().getWindow());
        if (newFile != null) {
            load(newFile);
        }
    }

    private void load(File file) {
        loadedPath = file;
        try {
            doLoad(file.toURI().toURL().toString());
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void load(String fileUrl) {
        try {
            try {
                loadedPath = new File(new URL(fileUrl).toURI()).getAbsoluteFile();
            } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ignored) {
                loadedPath = null;
            }
            doLoad(fileUrl);
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doLoad(String fileUrl) {
        loadedURL = fileUrl;
        sessionManager.getProperties().setProperty(CityGMLEditorApp.FILE_URL_PROPERTY, fileUrl);
        try {
            Node root = Importer3D.load(
                    fileUrl, loadAsPolygonsCheckBox.isSelected());
            sceneContent.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateStatus();
    }

    private void updateStatus() {
        nodeCount = 0;
        meshCount = 0;
        triangleCount = 0;
        updateCount(World.getRoot3D());
        Node content = sceneContent.getContent();
        final Bounds bounds = content == null ? new BoundingBox(0, 0, 0, 0) : content.getBoundsInLocal();
        status.setText(
                String.format("Nodes [%d] :: Meshes [%d] :: Triangles [%d] :: " +
                        "Bounds [w=%.2f,h=%.2f,d=%.2f]",
                        nodeCount, meshCount, triangleCount,
                        bounds.getWidth(), bounds.getHeight(), bounds.getDepth()));
    }

    private void updateCount(Node node) {
        nodeCount++;
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                updateCount(child);
            }
        } else if (node instanceof Box) {
            meshCount++;
            triangleCount += 6 * 2;
        } else if (node instanceof MeshView) {
            TriangleMesh mesh = (TriangleMesh) ((MeshView) node).getMesh();
            if (mesh != null) {
                meshCount++;
                triangleCount += mesh.getFaces().size() / mesh.getFaceElementSize();
            }
        }
    }

    public void toggleSettings(ActionEvent event) {
        final SplitPane.Divider divider = splitPane.getDividers().get(0);
        if (settingsBtn.isSelected()) {
            if (settingsLastWidth == -1) {
                settingsLastWidth = sidebar.prefWidth(-1);
            }
            final double divPos = 1 - (settingsLastWidth / splitPane.getWidth());
            new Timeline(
                    new KeyFrame(Duration.seconds(0.3),
                            new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    sidebar.setMinWidth(Region.USE_PREF_SIZE);
                                }
                            },
                            new KeyValue(divider.positionProperty(), divPos, Interpolator.EASE_BOTH)))
                    .play();
        } else {
            settingsLastWidth = sidebar.getWidth();
            sidebar.setMinWidth(0);
            new Timeline(new KeyFrame(Duration.seconds(0.3), new KeyValue(divider.positionProperty(), 1))).play();
        }
    }

    public void export(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        if (loadedPath != null) {
            chooser.setInitialDirectory(loadedPath.getAbsoluteFile().getParentFile());
        }
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CityGML", "*.gml"));
        chooser.setTitle("Export CityGML");
        File newFile = chooser.showSaveDialog(openMenuBtn.getScene().getWindow());
        if (newFile == null)
            return;

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

        var cityModel = (CityModel) cityModelNode;
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
        File newFile = chooser.showOpenDialog(openMenuBtn.getScene().getWindow());
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
                contentModel.setContent(root);
            }
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateStatus();
    }
}
