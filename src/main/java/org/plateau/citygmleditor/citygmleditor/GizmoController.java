package org.plateau.citygmleditor.citygmleditor;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import org.plateau.citygmleditor.citymodel.Building;

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

    private GizmoModel gizmoModel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        contentModel.getSubScene().addEventHandler(MouseEvent.ANY, mouseEventHandler);

        gizmoModel = new GizmoModel();
        contentModel.getRoot3D().getChildren().add(gizmoModel);
    }
    
    
    public void onSelect(ActionEvent actionEvent) {
        System.out.println("onSelect()");
        gizmoModel.setControlMode(GizmoModel.ControlMode.SELECT);
    }
    
    public void onMove(ActionEvent actionEvent) {
        System.out.println("onMove()");
        gizmoModel.setControlMode(GizmoModel.ControlMode.MOVE);
    }
    
    public void onRotation(ActionEvent actionEvent) {
        System.out.println("onRotation()");
        gizmoModel.setControlMode(GizmoModel.ControlMode.ROTATION);
    }
    
    public void onScale(ActionEvent actionEvent) {
        System.out.println("onScale()");
        gizmoModel.setControlMode(GizmoModel.ControlMode.SCALE);
    }

    
    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        // System.out.println("MouseEvent ...");
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            System.out.println("MouseEvent ...");
            var result = event.getPickResult();
            var building = FindBuilding(result.getIntersectedNode());
            if (building != null) {
                gizmoModel.selectBuilding(building);
            }
        }
    };
    
    private Node FindBuilding(Node node){
        if(node == null)
            return null;

        //Building型かチェック
        if(node instanceof Building)
            return node;

        //再帰検索
        return FindBuilding(node.getParent());
    }
}
