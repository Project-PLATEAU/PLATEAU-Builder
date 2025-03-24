package org.plateaubuilder.gui.settings;

import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewSettingsController implements Initializable {
    public CheckBox showGridCheckBox;
    public CheckBox msaaCheckBox;
    public ColorPicker backgroundColorPicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var camera = Editor.getCamera();
        var antiAliasing = Editor.getAntiAliasing();
        var sceneContent = Editor.getSceneContent();
        var axisGizmo = Editor.getCoordinateGrid();

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
