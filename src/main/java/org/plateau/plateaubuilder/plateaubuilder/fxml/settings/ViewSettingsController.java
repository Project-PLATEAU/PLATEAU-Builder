package org.plateau.plateaubuilder.plateaubuilder.fxml.settings;

import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.plateaubuilder.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewSettingsController implements Initializable {
    public CheckBox showGridCheckBox;
    public CheckBox msaaCheckBox;
    public ColorPicker backgroundColorPicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var camera = PLATEAUBuilderApp.getCamera();
        var antiAliasing = PLATEAUBuilderApp.getAntiAliasing();
        var sceneContent = PLATEAUBuilderApp.getSceneContent();
        var axisGizmo = PLATEAUBuilderApp.getCoordinateGrid();

        antiAliasing.msaaProperty().bind(msaaCheckBox.selectedProperty());
        axisGizmo.showGridProperty().bind(showGridCheckBox.selectedProperty());

        backgroundColorPicker.setValue((Color) sceneContent.getSubScene().getFill());
        sceneContent.getSubScene().fillProperty().bind(backgroundColorPicker.valueProperty());

        SessionManager sessionManager = SessionManager.getSessionManager();

        sessionManager.bind(showGridCheckBox.selectedProperty(), "showAxis");
        sessionManager.bind(msaaCheckBox.selectedProperty(), "msaa");
        sessionManager.bind(backgroundColorPicker.valueProperty(), "backgroundColor");
    }
}
