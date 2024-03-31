package org.plateau.plateaubuilder.plateaubuilder.fxml.settings;

import javafx.beans.binding.DoubleBinding;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.plateaubuilder.SessionManager;
import org.plateau.plateaubuilder.world.Camera;
import org.plateau.plateaubuilder.world.Light;

import java.net.URL;
import java.util.ResourceBundle;

public class LightSettingsController implements Initializable {
    private final Light light = PLATEAUBuilderApp.getLight();
    private final Camera camera = PLATEAUBuilderApp.getCamera();

    public ColorPicker ambientColorPicker;
    public ColorPicker light1ColorPicker;
    public CheckBox ambientEnableCheckbox;
    public CheckBox light1EnabledCheckBox;
    public CheckBox light1followCameraCheckBox;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        SessionManager sessionManager = SessionManager.getSessionManager();

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
    }
}
