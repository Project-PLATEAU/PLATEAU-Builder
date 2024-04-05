package org.plateaubuilder.gui.settings;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.SessionManager;
import org.plateaubuilder.core.world.Camera;

import java.net.URL;
import java.util.ResourceBundle;

public class CameraSettingsController implements Initializable {
    private final Camera camera = Editor.getCamera();
    public Slider fovSlider;
    public Slider nearClipSlider;
    public Slider farClipSlider;
    public Text nearClipText;
    public Text farClipText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // wire up settings in CAMERA
        fovSlider.setValue(camera.getCamera().getFieldOfView());
        camera.getCamera().fieldOfViewProperty().bind(fovSlider.valueProperty());
        nearClipSlider.setValue(Math.log10(camera.getCamera().getNearClip()));
        farClipSlider.setValue(Math.log10(camera.getCamera().getFarClip()));
        nearClipText.textProperty()
                .bind(Bindings.format(nearClipText.getText(), camera.getCamera().nearClipProperty()));
        farClipText.textProperty().bind(Bindings.format(farClipText.getText(), camera.getCamera().farClipProperty()));
        camera.getCamera().nearClipProperty().bind(new CameraSettingsController.Power10DoubleBinding(nearClipSlider.valueProperty()));
        camera.getCamera().farClipProperty().bind(new CameraSettingsController.Power10DoubleBinding(farClipSlider.valueProperty()));

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

        SessionManager sessionManager = SessionManager.getSessionManager();

        sessionManager.bind(fovSlider.valueProperty(), "fieldOfView");
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
