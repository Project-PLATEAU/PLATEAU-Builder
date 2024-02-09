package org.plateau.citygmleditor.citygmleditor;

import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewSettingsController implements Initializable {
    public Accordion settings;
    public CheckBox showGridCheckBox;
    public CheckBox yUpCheckBox;
    public CheckBox msaaCheckBox;
    public ColorPicker backgroundColorPicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var camera = CityGMLEditorApp.getCamera();
        var antiAliasing = CityGMLEditorApp.getAntiAliasing();
        var sceneContent = CityGMLEditorApp.getSceneContent();
        var axisGizmo = CityGMLEditorApp.getCoordinateGrid();

        antiAliasing.msaaProperty().bind(msaaCheckBox.selectedProperty());
        axisGizmo.showGridProperty().bind(showGridCheckBox.selectedProperty());
        camera.yUpProperty().bind(yUpCheckBox.selectedProperty());

        // Register state transition process for toggle buttons in rotation mode and
        // selection mode
//        modeToggleGroup = new ToggleGroup();
//        moveModeToggleButton.setToggleGroup(modeToggleGroup);
//        rotateModeToggleButton.setToggleGroup(modeToggleGroup);
//        camera.moveModeProperty().bind(moveModeToggleButton.selectedProperty());
//        camera.rotateModeProperty().bind(rotateModeToggleButton.selectedProperty());

        backgroundColorPicker.setValue((Color) sceneContent.getSubScene().getFill());
        sceneContent.getSubScene().fillProperty().bind(backgroundColorPicker.valueProperty());

        SessionManager sessionManager = SessionManager.getSessionManager();

        sessionManager.bind(showGridCheckBox.selectedProperty(), "showAxis");
        sessionManager.bind(yUpCheckBox.selectedProperty(), "yUp");
        sessionManager.bind(msaaCheckBox.selectedProperty(), "msaa");
        sessionManager.bind(backgroundColorPicker.valueProperty(), "backgroundColor");
    }
}
