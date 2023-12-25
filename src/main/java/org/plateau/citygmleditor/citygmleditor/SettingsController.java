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
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.exporters.GltfExporter;
import org.plateau.citygmleditor.exporters.ObjExporter;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.transform.Transform;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.util.StringConverter;
import org.plateau.citygmleditor.world.*;

/**
 * Controller class for settings panel
 */
public class SettingsController implements Initializable {
    private final Camera camera = CityGMLEditorApp.getCamera();
    private final AntiAliasing antiAliasing = CityGMLEditorApp.getAntiAliasing();
    private final Light light = CityGMLEditorApp.getLight();
    private final SceneContent sceneContent = CityGMLEditorApp.getSceneContent();
    private final AxisGizmo axisGizmo = CityGMLEditorApp.getAxisGizmo();
    private final AutoScalingGroup autoScalingGroup = CityGMLEditorApp.getAutoScalingGroup();
    public Accordion settings;
    public ColorPicker ambientColorPicker;
    public CheckBox showAxisCheckBox;
    public CheckBox yUpCheckBox;
    public Slider fovSlider;
    public CheckBox msaaCheckBox;
    public ColorPicker light1ColorPicker;
    public CheckBox ambientEnableCheckbox;
    public CheckBox light1EnabledCheckBox;
    public CheckBox light1followCameraCheckBox;
    public ColorPicker backgroundColorPicker;
    public Slider light1x;
    public Slider light1y;
    public Slider light1z;
    public CheckBox light2EnabledCheckBox;
    public ColorPicker light2ColorPicker;
    public Slider light2x;
    public Slider light2y;
    public Slider light2z;
    public CheckBox light3EnabledCheckBox;
    public ColorPicker light3ColorPicker;
    public Slider light3x;
    public Slider light3y;
    public Slider light3z;
    public TreeTableView<Node> hierarchyTreeTable;
    public TreeTableColumn<Node, String> nodeColumn;
    public TreeTableColumn<Node, String> idColumn;
    public TreeTableColumn<Node, Boolean> visibilityColumn;
    public TreeTableColumn<Node, Double> widthColumn;
    public TreeTableColumn<Node, Double> heightColumn;
    public TreeTableColumn<Node, Double> depthColumn;
    public TitledPane x6;
    public Label selectedNodeLabel;
    public Slider nearClipSlider;
    public Slider farClipSlider;
    public Label nearClipLabel;
    public Label farClipLabel;
    public ContextMenu hierarchyContextMenu;
    public MenuItem exportGltfMenu;
    public MenuItem exportObjMenu;
    public ToggleButton rotateModeToggleButton; // Toggle button for rotation mode
    public ToggleButton moveModeToggleButton; // Toggle button for move mode
    private ToggleGroup modeToggleGroup; // Group of toggle buttons for mode selection

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // keep one pane open always
        settings.expandedPaneProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            if (settings.getExpandedPane() == null)
                settings.setExpandedPane(settings.getPanes().get(0));
        }));
        // wire up settings in OPTIONS
        antiAliasing.msaaProperty().bind(msaaCheckBox.selectedProperty());
        axisGizmo.showAxisProperty().bind(showAxisCheckBox.selectedProperty());
        camera.yUpProperty().bind(yUpCheckBox.selectedProperty());

        // Register state transition process for toggle buttons in rotation mode and
        // selection mode
        modeToggleGroup = new ToggleGroup();
        moveModeToggleButton.setToggleGroup(modeToggleGroup);
        rotateModeToggleButton.setToggleGroup(modeToggleGroup);
        camera.moveModeProperty().bind(moveModeToggleButton.selectedProperty());
        camera.rotateModeProperty().bind(rotateModeToggleButton.selectedProperty());

        backgroundColorPicker.setValue((Color) sceneContent.getSubScene().getFill());
        sceneContent.getSubScene().fillProperty().bind(backgroundColorPicker.valueProperty());
        // wire up settings in LIGHTS
        ambientEnableCheckbox.setSelected(light.getAmbientLightEnabled());
        light.ambientLightEnabledProperty().bind(ambientEnableCheckbox.selectedProperty());
        ambientColorPicker.setValue(light.getAmbientLight().getColor());
        light.getAmbientLight().colorProperty().bind(ambientColorPicker.valueProperty());

        // LIGHT 1
        light1EnabledCheckBox.setSelected(light.getLight1Enabled());
        light.light1EnabledProperty().bind(light1EnabledCheckBox.selectedProperty());
        light1ColorPicker.setValue(light.getLight1().getColor());
        light.getLight1().colorProperty().bind(light1ColorPicker.valueProperty());
        light1x.disableProperty().bind(light1followCameraCheckBox.selectedProperty());
        light1y.disableProperty().bind(light1followCameraCheckBox.selectedProperty());
        light1z.disableProperty().bind(light1followCameraCheckBox.selectedProperty());
        light1followCameraCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                light.getLight1().translateXProperty().bind(new DoubleBinding() {
                    {
                        bind(camera.getCamera().boundsInParentProperty());
                    }

                    @Override
                    protected double computeValue() {
                        return camera.getCamera().getBoundsInParent().getMinX();
                    }
                });
                light.getLight1().translateYProperty().bind(new DoubleBinding() {
                    {
                        bind(camera.getCamera().boundsInParentProperty());
                    }

                    @Override
                    protected double computeValue() {
                        return camera.getCamera().getBoundsInParent().getMinY();
                    }
                });
                light.getLight1().translateZProperty().bind(new DoubleBinding() {
                    {
                        bind(camera.getCamera().boundsInParentProperty());
                    }

                    @Override
                    protected double computeValue() {
                        return camera.getCamera().getBoundsInParent().getMinZ();
                    }
                });
            } else {
                light.getLight1().translateXProperty().bind(light1x.valueProperty());
                light.getLight1().translateYProperty().bind(light1y.valueProperty());
                light.getLight1().translateZProperty().bind(light1z.valueProperty());
            }
        });
        // LIGHT 2
        light2EnabledCheckBox.setSelected(light.getLight2Enabled());
        light.light2EnabledProperty().bind(light2EnabledCheckBox.selectedProperty());
        light2ColorPicker.setValue(light.getLight2().getColor());
        light.getLight2().colorProperty().bind(light2ColorPicker.valueProperty());
        light.getLight2().translateXProperty().bind(light2x.valueProperty());
        light.getLight2().translateYProperty().bind(light2y.valueProperty());
        light.getLight2().translateZProperty().bind(light2z.valueProperty());
        // LIGHT 3
        light3EnabledCheckBox.setSelected(light.getLight3Enabled());
        light.light3EnabledProperty().bind(light3EnabledCheckBox.selectedProperty());
        light3ColorPicker.setValue(light.getLight3().getColor());
        light.getLight3().colorProperty().bind(light3ColorPicker.valueProperty());
        light.getLight3().translateXProperty().bind(light3x.valueProperty());
        light.getLight3().translateYProperty().bind(light3y.valueProperty());
        light.getLight3().translateZProperty().bind(light3z.valueProperty());
        // wire up settings in CAMERA
        fovSlider.setValue(camera.getCamera().getFieldOfView());
        camera.getCamera().fieldOfViewProperty().bind(fovSlider.valueProperty());
        nearClipSlider.setValue(Math.log10(camera.getCamera().getNearClip()));
        farClipSlider.setValue(Math.log10(camera.getCamera().getFarClip()));
        nearClipLabel.textProperty()
                .bind(Bindings.format(nearClipLabel.getText(), camera.getCamera().nearClipProperty()));
        farClipLabel.textProperty().bind(Bindings.format(farClipLabel.getText(), camera.getCamera().farClipProperty()));
        camera.getCamera().nearClipProperty().bind(new Power10DoubleBinding(nearClipSlider.valueProperty()));
        camera.getCamera().farClipProperty().bind(new Power10DoubleBinding(farClipSlider.valueProperty()));

        hierarchyTreeTable.rootProperty().bind(new ObjectBinding<TreeItem<Node>>() {

            {
                bind(sceneContent.contentProperty());
            }

            @Override
            protected TreeItem<Node> computeValue() {
                Node content3D = sceneContent.getContent();
                if (content3D != null) {
                    return new TreeItemImpl(content3D);
                } else {
                    return null;
                }
            }
        });
        hierarchyTreeTable.setOnMouseClicked(t -> {
            if (t.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    var item = selectedItem.valueProperty().get();
                    exportGltfMenu.setDisable(!(item instanceof ILODSolidView));
                    exportObjMenu.setDisable(!(item instanceof ILODSolidView));
                }
            }
            if (t.getClickCount() == 2) {
                settings.setExpandedPane(x6);
                t.consume();
            }
        });
        hierarchyTreeTable.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.SPACE) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Node node = selectedItem.getValue();
                    node.setVisible(!node.isVisible());
                }
                t.consume();
            }
        });
        nodeColumn.setCellValueFactory(p -> p.getValue().valueProperty().asString());
        idColumn.setCellValueFactory(p -> p.getValue().getValue().idProperty());
        visibilityColumn.setCellValueFactory(p -> p.getValue().getValue().visibleProperty());
        visibilityColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(visibilityColumn));
        widthColumn.setCellValueFactory(p -> new ObjectBinding<Double>() {
            {
                bind(p.getValue().getValue().boundsInLocalProperty());
            }

            @Override
            protected Double computeValue() {
                return p.getValue().getValue().getBoundsInLocal().getWidth();
            }
        });
        StringConverter<Double> niceDoubleStringConverter = new StringConverter<Double>() {
            @Override
            public String toString(Double t) {
                return String.format("%.2f", t);
            }

            @Override
            public Double fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); // Not needed so far
            }
        };
        widthColumn.setCellFactory(TextFieldTreeTableCell.<Node, Double>forTreeTableColumn(niceDoubleStringConverter));
        heightColumn.setCellFactory(TextFieldTreeTableCell.<Node, Double>forTreeTableColumn(niceDoubleStringConverter));
        depthColumn.setCellFactory(TextFieldTreeTableCell.<Node, Double>forTreeTableColumn(niceDoubleStringConverter));
        heightColumn.setCellValueFactory(p -> new ObjectBinding<Double>() {
            {
                bind(p.getValue().getValue().boundsInLocalProperty());
            }

            @Override
            protected Double computeValue() {
                return p.getValue().getValue().getBoundsInLocal().getHeight();
            }
        });
        depthColumn.setCellValueFactory(p -> new ObjectBinding<Double>() {
            {
                bind(p.getValue().getValue().boundsInLocalProperty());
            }

            @Override
            protected Double computeValue() {
                return p.getValue().getValue().getBoundsInLocal().getDepth();
            }
        });

        SessionManager sessionManager = SessionManager.getSessionManager();

        sessionManager.bind(showAxisCheckBox.selectedProperty(), "showAxis");
        sessionManager.bind(yUpCheckBox.selectedProperty(), "yUp");
        sessionManager.bind(msaaCheckBox.selectedProperty(), "msaa");
        sessionManager.bind(backgroundColorPicker.valueProperty(), "backgroundColor");
        sessionManager.bind(fovSlider.valueProperty(), "fieldOfView");
        sessionManager.bind(light1ColorPicker.valueProperty(), "light1Color");
        sessionManager.bind(light1EnabledCheckBox.selectedProperty(), "light1Enabled");
        sessionManager.bind(light1followCameraCheckBox.selectedProperty(), "light1FollowCamera");
        sessionManager.bind(light1x.valueProperty(), "light1X");
        sessionManager.bind(light1y.valueProperty(), "light1Y");
        sessionManager.bind(light1z.valueProperty(), "light1Z");
        sessionManager.bind(light2ColorPicker.valueProperty(), "light2Color");
        sessionManager.bind(light2EnabledCheckBox.selectedProperty(), "light2Enabled");
        sessionManager.bind(light2x.valueProperty(), "light2X");
        sessionManager.bind(light2y.valueProperty(), "light2Y");
        sessionManager.bind(light2z.valueProperty(), "light2Z");
        sessionManager.bind(light3ColorPicker.valueProperty(), "light3Color");
        sessionManager.bind(light3EnabledCheckBox.selectedProperty(), "light3Enabled");
        sessionManager.bind(light3x.valueProperty(), "light3X");
        sessionManager.bind(light3y.valueProperty(), "light3Y");
        sessionManager.bind(light3z.valueProperty(), "light3Z");
        sessionManager.bind(ambientColorPicker.valueProperty(), "ambient");
        sessionManager.bind(ambientEnableCheckbox.selectedProperty(), "ambientEnable");
        sessionManager.bind(settings, "settingsPane");

        viewInitialize();
    }

    private void viewInitialize() {
        World.getRoot3D().getChildren().add(camera.getCameraXform());
        World.getRoot3D().getChildren().add(autoScalingGroup);
        sceneContent.rebuildSubScene();
    }

    /**
     * Export the selected solid to gLTF
     * @param actionEvent the event
     */
    public void ExportGltf(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("gLTF", "*.gltf", "*.glb")
        );
        chooser.setTitle("Export gLTF");
        File newFile = chooser.showSaveDialog(hierarchyTreeTable.getScene().getWindow());
        if (newFile == null)
            return;

        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        try {
            GltfExporter.export(newFile.toString(), solid);
        } catch (Exception ex) {
            Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Export the selected solid to OBJ
     * @param actionEvent the event
     */
    public void ExportObj(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OBJ", "*.obj")
        );
        chooser.setTitle("Export OBJ");
        File newFile = chooser.showSaveDialog(hierarchyTreeTable.getScene().getWindow());
        if (newFile == null)
            return;

        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        try {
            ObjExporter.export(newFile.toString(), solid);
        } catch (Exception ex) {
            Logger.getLogger(SettingsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class TreeItemImpl extends TreeItem<Node> {

        public TreeItemImpl(Node node) {
            super(node);
            if (node instanceof Parent) {
                for (Node n : ((Parent) node).getChildrenUnmodifiable()) {
                    getChildren().add(new TreeItemImpl(n));
                }
            }
            node.setOnMouseClicked(t -> {
                TreeItem<Node> parent = getParent();
                while (parent != null) {
                    parent.setExpanded(true);
                    parent = parent.getParent();
                }
                hierarchyTreeTable.getSelectionModel().select(TreeItemImpl.this);
                hierarchyTreeTable.scrollTo(hierarchyTreeTable.getSelectionModel().getSelectedIndex());
                t.consume();
            });
        }
    }

    private class Power10DoubleBinding extends DoubleBinding {

        private DoubleProperty prop;

        public Power10DoubleBinding(DoubleProperty prop) {
            this.prop = prop;
            bind(prop);
        }

        @Override
        protected double computeValue() {
            return Math.pow(10, prop.getValue());
        }
    }
}
