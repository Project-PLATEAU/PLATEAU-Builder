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
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
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

    /**
     * 初期化
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneContent.getSubScene().addEventHandler(MouseEvent.ANY, mouseEventHandler);

        gizmoModel = new GizmoModel();
        World.getRoot3D().getChildren().add(gizmoModel);
    }
    
    /**
     * フリーボタン選択時イベント
     * @param actionEvent
     */
    public void onSelect(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.SELECT);
    }
    
    /**
     * 移動ボタン選択時イベント
     * @param actionEvent
     */
    public void onMove(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.MOVE);
    }
    
    /**
     * 回転ボタン選択時イベント
     * @param actionEvent
     */
    public void onRotation(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.ROTATION);
    }
    
    /**
     * スケールボタン選択時イベント
     * @param actionEvent
     */
    public void onScale(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.SCALE);
    }

    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            if (event.isPrimaryButtonDown()) {
                var result = event.getPickResult();
                var building = findLODSolidView(result.getIntersectedNode());
                if (building != null) {
                    gizmoModel.attachManipulator(((ILODSolidView)building).getTransformManipulator());
                }
                if (gizmoModel.isGizmoDragging(result.getIntersectedNode())) {
                    CityGMLEditorApp.getCamera().setHookingMousePrimaryButtonEvent(true);
                    gizmoModel.setCurrentGizmo(result.getIntersectedNode());
                    mouseDragging = true;
                    return;
                }
            }
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            var result = event.getPickResult();
            if(gizmoModel.isGizmoDragging(result.getIntersectedNode())) 
            {
                if (event.isPrimaryButtonDown()) {
                    vecIni = unprojectDirection(event.getSceneX(), event.getSceneY(), sceneContent.getSubScene().getWidth(), sceneContent.getSubScene().getHeight());//scene.getWidth(),scene.getHeight());
                    distance = result.getIntersectedDistance();
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
            
            vecPos = unprojectDirection(mousePosX, mousePosY, sceneContent.getSubScene().getWidth(),sceneContent.getSubScene().getHeight());
            Point3D delta = vecPos.subtract(vecIni).multiply(distance);
            gizmoModel.updateTransform(delta);
            vecIni=vecPos;
            distance=result.getIntersectedDistance();
        }
    };
    
    /**
     * マウス座標を3D座標に逆投影
     * @param sceneX
     * @param sceneY
     * @param sWidth
     * @param sHeight
     * @return
     */
    public Point3D unprojectDirection(double sceneX, double sceneY, double sWidth, double sHeight)
    {
        double tanHFov = Math.tan(Math.toRadians(CityGMLEditorApp.getCamera().getCamera().getFieldOfView()) * 0.5f);
        Point3D vMouse = new Point3D(tanHFov*(2*sceneX/sWidth-1), tanHFov*(2*sceneY/sWidth-sHeight/sWidth), 1);

        Point3D result = localToSceneDirection(vMouse);
        return result.normalize();
    }

    /**
     * ローカル座標系からシーン座標系へ座標を変換
     * @param pt
     * @return
     */
    public Point3D localToScene(Point3D pt) {
        Point3D res = CityGMLEditorApp.getCamera().getCamera().localToParentTransformProperty().get().transform(pt);
        if (CityGMLEditorApp.getCamera().getCamera().getParent() != null) {
            res = CityGMLEditorApp.getCamera().getCamera().getParent().localToSceneTransformProperty().get().transform(res);
        }
        return res;
    }

    /**
     * ローカル座標系からシーン座標系へ方向ベクトルを変換
     * @param dir
     * @return
     */
    public Point3D localToSceneDirection(Point3D dir) {
        Point3D res = localToScene(dir);
        return res.subtract(localToScene(new Point3D(0, 0, 0)));
    }

    /**
     * 子にギズモの操作対象があるか再帰的に検索
     * @param node
     * @return
     */
    private Node findLODSolidView(Node node){
        if(node == null)
            return null;

        // ILODSolidViewインターフェースを実装しているか
        if(node instanceof ILODSolidView)
            return node;

        // 再帰検索
        return findLODSolidView(node.getParent());
    }
}
