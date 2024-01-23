package org.plateau.citygmleditor.citygmleditor;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.world.SceneContent;
import org.plateau.citygmleditor.world.World;

/**
 * 
 */
public class ToolbarController implements Initializable {
    private SceneContent sceneContent = CityGMLEditorApp.getSceneContent();

    private GizmoModel gizmoModel;

    private boolean mouseDragging;

    private Point3D vecIni;
    private double distance;

    /**
     * 初期化
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneContent.getSubScene().addEventHandler(MouseEvent.ANY, mouseEventHandler);

        gizmoModel = new GizmoModel();
        World.getRoot3D().getChildren().add(gizmoModel);

        initializeLODToggle();
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
            if (event.isPrimaryButtonDown() && !mouseDragging) {
                var result = event.getPickResult();
                var building = findLODSolidView(result.getIntersectedNode());
                if (building != null) {
                    gizmoModel.attachManipulator(((ILODSolidView)building).getTransformManipulator());
                }
                if (gizmoModel.isGizmoDragging(result.getIntersectedNode())) {
                    CityGMLEditorApp.getCamera().setHookingMousePrimaryButtonEvent(true);
                    gizmoModel.setCurrentGizmo(result.getIntersectedNode());
                    vecIni = unprojectDirection(event.getSceneX(), event.getSceneY(), sceneContent.getSubScene().getWidth(), sceneContent.getSubScene().getHeight());
                    distance = result.getIntersectedDistance();
                    mouseDragging = true;
                }
            }
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (event.isPrimaryButtonDown() && mouseDragging) {
                var result = event.getPickResult();
                double mousePosX = event.getSceneX();
                double mousePosY = event.getSceneY();
                Point3D vecPos = unprojectDirection(mousePosX, mousePosY, sceneContent.getSubScene().getWidth(), sceneContent.getSubScene().getHeight());
                Point3D delta = vecPos.subtract(vecIni).multiply(distance);
                gizmoModel.updateTransform(delta);
                vecIni= vecPos;
                distance=result.getIntersectedDistance();
            }
        }
        else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouseDragging = false;
            CityGMLEditorApp.getCamera().setHookingMousePrimaryButtonEvent(false);

            gizmoModel.fixTransform();
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
    public Point3D localToScene(Node node, Point3D pt) {
        Point3D res = node.localToParentTransformProperty().get().transform(pt);
        if (node.getParent() != null) {
            res = localToScene(node.getParent(), res);
        }
        return res;
    }

    /**
     * ローカル座標系からシーン座標系へ方向ベクトルを変換
     * @param dir
     * @return
     */
    public Point3D localToSceneDirection(Point3D dir) {
        Point3D res = localToScene(CityGMLEditorApp.getCamera().getCamera(), dir);
        return res.subtract(localToScene(CityGMLEditorApp.getCamera().getCamera(), Point3D.ZERO));
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

    /** サーフェス表示切り替え **/

    public ToggleButton surfaceViewToggle;

    public void onToggleSurfaceView() {
        CityGMLEditorApp.getCityModelViewMode().toggleSurfaceViewMode(surfaceViewToggle.isSelected());
    }

    /** LOD表示切り替え **/
    public ToggleGroup lodToggleGroup;

    private void initializeLODToggle() {
        var cityModelViewMode = CityGMLEditorApp.getCityModelViewMode();
        selectLODToggle(cityModelViewMode.getLOD());

        CityGMLEditorApp.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            selectLODToggle((int)newValue);
        });

        lodToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // 全てのトグルがオフになる状態は回避する
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }

            int lod = Integer.parseInt(newToggle.getUserData().toString());
            CityGMLEditorApp.getCityModelViewMode().lodProperty().setValue(lod);
        });
    }

    private void selectLODToggle(int lod) {
        for (var toggle : lodToggleGroup.getToggles()) {
            int toggleLOD = Integer.parseInt(toggle.getUserData().toString());
            if (toggleLOD == (int)lod) {
                lodToggleGroup.selectToggle(toggle);
                return;
            }
        }
    }
}
