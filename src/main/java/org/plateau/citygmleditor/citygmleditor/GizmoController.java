package org.plateau.citygmleditor.citygmleditor;

import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.world.SceneContent;
import org.plateau.citygmleditor.world.World;

/**
 * 
 */
public class GizmoController implements Initializable {
    private SceneContent sceneContent = CityGMLEditorApp.getSceneContent();

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
    private boolean mouseDragging;

    private Point3D vecIni, vecPos;
    private double distance;

    private ArrayList<Sphere> spheres;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneContent.getSubScene().addEventHandler(MouseEvent.ANY, mouseEventHandler);

        gizmoModel = new GizmoModel();
        World.getRoot3D().getChildren().add(gizmoModel);

        // TODO Debug
        spheres = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                var sphere = new Sphere(5);
                sphere.setTranslateX(j * 15);
                sphere.setTranslateY(150 + (i * 20));
                World.getRoot3D().getChildren().add(sphere);
                spheres.add(sphere);
            }
        }
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
            var result = event.getPickResult();
            var building = FindBuilding(result.getIntersectedNode());
            if (building != null) {
                gizmoModel.attachBuilding(building);
            }

            for (int i = 0; i < spheres.size(); i++) {
                if (result.getIntersectedNode().equals(spheres.get(i))) {
                    gizmoModel.snapTransform(i, 5);
                }
            }
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            var result = event.getPickResult();
            if(gizmoModel.isGizmoDragging(result.getIntersectedNode())) 
            {
                if (event.isPrimaryButtonDown()) {
                    CityGMLEditorApp.getCamera().setHookingMousePrimaryButtonEvent(true);

                    if (! mouseDragging)
                        gizmoModel.setCurrentGizmo(result.getIntersectedNode());
                            
                    vecIni = unProjectDirection(event.getSceneX(), event.getSceneY(), sceneContent.getSubScene().getWidth(), sceneContent.getSubScene().getHeight());//scene.getWidth(),scene.getHeight());
                    distance = result.getIntersectedDistance();
                    
                    mouseDragging = true;
                }
            }
        }
        else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouseDragging = false;
            CityGMLEditorApp.getCamera().setHookingMousePrimaryButtonEvent(false);

            gizmoModel.fixTransform();
        }

        if (mouseDragging) {
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

            vecPos = unProjectDirection(mousePosX, mousePosY, sceneContent.getSubScene().getWidth(),sceneContent.getSubScene().getHeight());
            Point3D delta = vecPos.subtract(vecIni).multiply(distance);
            //gizmoModel.getTransforms().add(new Translate(p.getX(),p.getY(),p.getZ()));
            gizmoModel.updateTransform(delta);
            vecIni=vecPos;
            distance=result.getIntersectedDistance();
        }
    };
    
    public Point3D unProjectDirection(double sceneX, double sceneY, double sWidth, double sHeight) {
        double tanHFov = Math.tan(Math.toRadians(CityGMLEditorApp.getCamera().getCamera().getFieldOfView()) * 0.5f);
        Point3D vMouse = new Point3D(tanHFov*(2*sceneX/sWidth-1), tanHFov*(2*sceneY/sWidth-sHeight/sWidth), 1);

        Point3D result = localToSceneDirection(vMouse);
        return result.normalize();
    }

    public Point3D localToScene(Point3D pt) {
        Point3D res = CityGMLEditorApp.getCamera().getCamera().localToParentTransformProperty().get().transform(pt);
        if (CityGMLEditorApp.getCamera().getCamera().getParent() != null) {
            res = CityGMLEditorApp.getCamera().getCamera().getParent().localToSceneTransformProperty().get().transform(res);
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
        if(node instanceof BuildingView)
            return node;

        //再帰検索
        return FindBuilding(node.getParent());
    }
}
