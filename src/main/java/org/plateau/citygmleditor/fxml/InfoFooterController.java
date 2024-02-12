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
        var geoReference = World.getActiveInstance().getGeoReference();
        World.getActiveInstance().getGeoReferenceProperty().addListener((ov, oldGeoReference, newGeoReference) -> {
            offsetValue.setText(String.format("(%f, %f, %f)", newGeoReference.getOrigin().x, newGeoReference.getOrigin().y, newGeoReference.getOrigin().z));
            epsgCodeValue.textProperty().bind(newGeoReference.getEPSGCodeProperty());
        });
    }
}