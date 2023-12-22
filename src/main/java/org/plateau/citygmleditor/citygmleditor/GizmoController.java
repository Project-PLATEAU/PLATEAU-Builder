package org.plateau.citygmleditor.citygmleditor;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Translate;
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

    private double dragStartX, dragStartY;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private boolean mouseDraggig;

    private Point3D vecIni, vecPos;
    private double distance;

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
                gizmoModel.attachBuilding(building);
            }
        }
        else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
            var result = event.getPickResult();
              
            if(gizmoModel.isGizmoDragging(result.getIntersectedNode())) 
            {
                if (event.isPrimaryButtonDown()) {
                    mouseDraggig = true;
                    contentModel.setHookingMousePrimaryButtonEvent(true);

                    gizmoModel.setCurrentNode(result.getIntersectedNode());
                            
                    vecIni = unProjectDirection(event.getSceneX(), event.getSceneY(), contentModel.getSubScene().getWidth(), contentModel.getSubScene().getHeight());//scene.getWidth(),scene.getHeight());
                    distance=result.getIntersectedDistance();
                }
            }
        }
        else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouseDraggig = false;
            contentModel.setHookingMousePrimaryButtonEvent(false);

            gizmoModel.fixTransform();
        }

        if (mouseDraggig) {
            var result = event.getPickResult();
            
            double modifier = 1.0;

            if (event.isControlDown()) {
                modifier = 0.1;
            }
            if (event.isShiftDown()) {
                modifier = 10.0;
            }

            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = event.getSceneX();
            mousePosY = event.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);
            
            System.out.println("Mouse Dragging ...");

            vecPos = unProjectDirection(mousePosX, mousePosY, contentModel.getSubScene().getWidth(),contentModel.getSubScene().getHeight());
            Point3D delta = vecPos.subtract(vecIni).multiply(distance);
            //gizmoModel.getTransforms().add(new Translate(p.getX(),p.getY(),p.getZ()));
            gizmoModel.updateTransform(delta);
            vecIni=vecPos;
            distance=result.getIntersectedDistance();
        }
    };
    
    public Point3D unProjectDirection(double sceneX, double sceneY, double sWidth, double sHeight) {
        double tanHFov = Math.tan(Math.toRadians(contentModel.getCamera().getFieldOfView()) * 0.5f);
        Point3D vMouse = new Point3D(tanHFov*(2*sceneX/sWidth-1), tanHFov*(2*sceneY/sWidth-sHeight/sWidth), 1);

        Point3D result = localToSceneDirection(vMouse);
        return result.normalize();
    }

    public Point3D localToScene(Point3D pt) {
        Point3D res = contentModel.getCamera().localToParentTransformProperty().get().transform(pt);
        if (contentModel.getCamera().getParent() != null) {
            res = contentModel.getCamera().getParent().localToSceneTransformProperty().get().transform(res);
        }
        return res;
    }

    public Point3D localToSceneDirection(Point3D dir) {
        Point3D res = localToScene(dir);
        return res.subtract(localToScene(new Point3D(0, 0, 0)));
    }

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
