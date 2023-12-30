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
package org.plateau.citygmleditor.utils;

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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModelView;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.nio.file.attribute.BasicFileAttributes;
import java.awt.Desktop;

/**
 * Class for file operations
 */
public class FileUtils {
    // ディレクトリコピー
    public static boolean copyDirectory(Path sourcePath, Path destinationPath,String skipPath) throws IOException {
        ButtonType buttonResult = null;
        if (Files.exists(destinationPath)) {
            Alert alert = new Alert(AlertType.CONFIRMATION, "同一名称のフォルダが存在します。上書きしますか？", ButtonType.YES, ButtonType.NO);
            alert.setTitle("上書き確認");
            alert.setHeaderText(null);
            alert.showAndWait();
            buttonResult = alert.getResult();
        }
        if (buttonResult == null) {
            Files.walk(sourcePath).forEach(path -> {
                if (!path.toString().matches(skipPath)) {
                    try {
                        Files.copy(path, destinationPath.resolve(sourcePath.relativize(path)),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return true;
        } else if (buttonResult == ButtonType.YES) {
            Path destinationPathTmp = Paths.get(destinationPath.toString() + "_tmp");
            Files.walk(sourcePath).forEach(path -> {
                if (!path.toString().matches(skipPath)) {
                    try {
                        Files.copy(path, destinationPathTmp.resolve(sourcePath.relativize(path)),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            // 元々あったフォルダを削除し、tmpフォルダの名称を修正
            deleteDirectory(destinationPath);
            Files.move(destinationPathTmp, destinationPath);
            return true;
        } else {
            return false;
        }
    }

    // ディレクトリ削除
    public static void deleteDirectory(Path directoryPath) throws IOException {
        Files.walkFileTree(directoryPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
