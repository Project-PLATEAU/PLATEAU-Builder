package org.plateaubuilder.gui.main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import org.plateaubuilder.core.world.World;

import java.net.URL;
import java.util.ResourceBundle;

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