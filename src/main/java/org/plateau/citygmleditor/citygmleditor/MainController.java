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
import java.util.ResourceBundle;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.layout.VBox;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.exporters.GmlExporter;
import org.plateau.citygmleditor.importers.gml.GmlImporter;
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

import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.BoundingBox;

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
    private final ContentModel contentModel = CityGMLEditorApp.getContentModel();
    private File loadedPath;
    private String loadedURL;
    private String[] supportedFormatRegex;
    private SessionManager sessionManager = SessionManager.getSessionManager();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // CREATE NAVIGATOR CONTROLS
            Parent navigationPanel = FXMLLoader.load(MainController.class.getResource("navigation.fxml"));
            // CREATE SETTINGS PANEL
            sidebar = FXMLLoader.load(MainController.class.getResource("sidebar.fxml"));
            // SETUP SPLIT PANE
            splitPane.getItems().addAll(new SubSceneResizer(contentModel.subSceneProperty(), navigationPanel), sidebar);
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
        contentModel.getSubScene().setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                boolean hasSupportedFile = false;
                fileLoop:
                for (File file : db.getFiles()) {
                    for (String format : supportedFormatRegex) {
                        if (file.getName().matches(format)) {
                            hasSupportedFile = true;
                            break fileLoop;
                        }
                    }
                }
                if (hasSupportedFile) event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        contentModel.getSubScene().setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File supportedFile = null;
                fileLoop:
                for (File file : db.getFiles()) {
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
        sessionManager.getProperties().setProperty(CityGMLEditorApp.FILE_URL_PROPERTY, fileUrl);
        try {
            var root = GmlImporter.loadGml(fileUrl);
            contentModel.setContent(root);
        } catch (Exception ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateStatus();
    }

    public void open(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", Importer3D.getSupportedFormatExtensionFilters()));
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
            contentModel.setContent(root);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        updateStatus();
    }

    private void updateStatus() {
        nodeCount = 0;
        meshCount = 0;
        triangleCount = 0;
        updateCount(contentModel.getRoot3D());
        Node content = contentModel.getContent();
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
                            new KeyValue(divider.positionProperty(), divPos, Interpolator.EASE_BOTH)
                    )
            ).play();
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
                new FileChooser.ExtensionFilter("CityGML", "*.gml")
        );
        chooser.setTitle("Export CityGML");
        File newFile = chooser.showSaveDialog(openMenuBtn.getScene().getWindow());

        var content = (Group) CityGMLEditorApp.getContentModel().getContent();
        var cityModelNode = content.getChildren().get(0);
        if (cityModelNode == null)
            return;

        var cityModel = (CityModel)cityModelNode;

        try {
            GmlExporter.export(newFile.toString(), cityModel.getGmlObject(), cityModel.getSchemaHandler());
        } catch (ADEException | CityGMLWriteException | CityGMLBuilderException e) {
            throw new RuntimeException(e);
        }
    }
}
