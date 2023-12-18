package org.plateau.citygmleditor.citygmleditor;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

/**
 * 
 */
public class GizmoController implements Initializable {
    
    private ContentModel contentModel = CityGMLEditorApp.getContentModel();

    public RadioButton selectButton;
    public RadioButton moveButton;
    public RadioButton rotationButton;
    public RadioButton scaleButton;
    public ToggleGroup modeGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    
    
    public void onSelect(ActionEvent actionEvent) {
        System.out.println("onSelect()");
    }
    
    public void onMove(ActionEvent actionEvent) {
        System.out.println("onMove()");
    }
    
    public void onRotation(ActionEvent actionEvent) {
        System.out.println("onRotation()");
    }
    
    public void onScale(ActionEvent actionEvent) {
        System.out.println("onScale()");
    }
}
