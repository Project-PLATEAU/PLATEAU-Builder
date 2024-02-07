package org.plateau.citygmleditor.fxml;

import java.net.URL;
import java.util.ResourceBundle;
import org.plateau.citygmleditor.world.World;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class InfoFooterController implements Initializable {
    @FXML
    private Label epsgCodeValue;
    @FXML
    private Label offsetValue;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        epsgCodeValue.textProperty().bind(World.getActiveInstance().getEPSGCodeProperty());
        offsetValue.textProperty().bind(World.getActiveInstance().getOriginProperty());
    }
}