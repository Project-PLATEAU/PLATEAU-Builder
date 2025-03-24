package org.plateaubuilder.gui.featureinfo;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.plateaubuilder.core.editor.Editor;

import java.net.URL;
import java.util.ResourceBundle;

public class BaseMapController implements Initializable {

    @FXML
    private Spinner<Integer> positionZ;

    @FXML
    private TextField tileServerUrl;
    
    @FXML
    private Button btnApply;

    @FXML
    private TitledPane titledPane;

    private PauseTransition pause;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        titledPane.setExpanded(true);
        positionZ.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-300, 300, 0));
        positionZ.getEditor().textProperty().addListener((observableValue, oldValue, newValue) -> {
            int parsedValue = parseZValue(newValue, oldValue);
            if (parsedValue != -1) {
                positionZ.getValueFactory().setValue(parsedValue);
                Editor.getXyzTile().updateBaseMapPositionZ(parsedValue);
                if (pause == null) {
                    pause = new PauseTransition(Duration.millis(400));
                    pause.setOnFinished(e -> {
                        Editor.getXyzTile().loadImagesAfterCameraMove();
                    });
                }
                pause.playFromStart();
            }
        });

        TextFormatter<String> formatter = createStringFormatter();
        tileServerUrl.setTextFormatter(formatter);
        formatter.setValue(Editor.getXyzTile().getTileServerUrl());
    }
    
    @FXML
    private void handleButtonApply() {
        btnApply.setDisable(true);
        Editor.getXyzTile().updateTileServerUrl(tileServerUrl.getText());
        btnApply.setDisable(false);
    }

    private TextFormatter<String> createStringFormatter() {
        return new TextFormatter<>(new StringConverter<>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        });
    }

    private int parseZValue(String newValue, String oldValue) {
        try {
            return Integer.parseInt(newValue);
        } catch (NumberFormatException exception) {
            positionZ.getEditor().setText(oldValue);
            return -1;
        }
    }
}
