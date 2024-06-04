package org.plateaubuilder.gui.main;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.editor.transform.GizmoModel;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.world.SceneContent;
import org.plateaubuilder.core.world.World;

import java.net.URL;
import java.util.ResourceBundle;

public class ToolbarController implements Initializable {
    private SceneContent sceneContent = Editor.getSceneContent();

    private GizmoModel gizmoModel;

    private boolean mouseDragging;

    private Point3D vecIni;
    private double distance;

    /**
     * FXMLファイルがロードされた際に呼び出される初期化メソッドです。
     * 
     * @param location FXMLファイルのURL
     * @param resources ロケール固有のリソースバンドル
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sceneContent.getSubScene().addEventHandler(MouseEvent.ANY, mouseEventHandler);

        gizmoModel = new GizmoModel();
        World.getActiveInstance().setGizmo(gizmoModel);

        initializeLODToggle();

        var activeFeatureProperty = Editor.getFeatureSellection().getSelectElementProperty();
        activeFeatureProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                gizmoModel.attachManipulator(((ILODView) newValue).getTransformManipulator());
            }
        });
    }
    
    /**
     * フリーボタン選択時イベント
     * 
     * @param actionEvent イベントオブジェクト
     */
    public void onSelect(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.SELECT);
    }
    
    /**
     * 移動ボタン選択時イベント
     * 
     * @param actionEvent イベントオブジェクト
     */
    public void onMove(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.MOVE);
    }
    
    /**
     * 回転ボタン選択時イベント
     * 
     * @param actionEvent イベントオブジェクト
     */
    public void onRotation(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.ROTATION);
    }
    
    /**
     * スケールボタン選択時イベント
     * 
     * @param actionEvent イベントオブジェクト
     */
    public void onScale(ActionEvent actionEvent) {
        gizmoModel.setControlMode(GizmoModel.ControlMode.SCALE);
    }

    /**
     * マウスイベントを処理するハンドラ
     */
    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        if (event.isControlDown() || event.isShiftDown() || event.isAltDown())
            return;

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            if (event.isPrimaryButtonDown() && !mouseDragging) {
                var result = event.getPickResult();
                var lodView = findLODView(result.getIntersectedNode());
                if (lodView != null) {
                    gizmoModel.attachManipulator(((ILODView) lodView).getTransformManipulator());
                }
                if (gizmoModel.isNodeInGizmo(result.getIntersectedNode())) {
                    gizmoModel.setCurrentGizmo(result.getIntersectedNode());
                    gizmoModel.beginTransform();
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

            gizmoModel.fixTransform();
        }
    };
    
    /**
     * シーン座標系からローカル座標系への方向ベクトルを逆投影します。
     * 
     * @param sceneX シーン座標系のX座標
     * @param sceneY シーン座標系のY座標
     * @param sWidth シーンの幅
     * @param sHeight シーンの高さ
     * @return ローカル座標系への方向ベクトル
     */
    public Point3D unprojectDirection(double sceneX, double sceneY, double sWidth, double sHeight) {
        double tanHFov = Math.tan(Math.toRadians(Editor.getCamera().getCamera().getFieldOfView()) * 0.5f);
        Point3D vMouse = new Point3D(tanHFov*(2*sceneX/sWidth-1), tanHFov*(2*sceneY/sWidth-sHeight/sWidth), 1);

        Point3D result = localToSceneDirection(vMouse);
        return result.normalize();
    }

    /**
     * ローカル座標系からシーン座標系へ座標を変換します。
     * 
     * @param node ローカル座標系の基準となるノード
     * @param pt ローカル座標系での座標
     * @return シーン座標系での座標
     */
    public Point3D localToScene(Node node, Point3D pt) {
        Point3D res = node.localToParentTransformProperty().get().transform(pt);
        if (node.getParent() != null) {
            res = localToScene(node.getParent(), res);
        }
        return res;
    }

    /**
     * ローカル座標系からシーン座標系へ方向ベクトルを変換します。
     * 
     * @param dir ローカル座標系での方向ベクトル
     * @return シーン座標系での方向ベクトル
     */
    public Point3D localToSceneDirection(Point3D dir) {
        Point3D res = localToScene(Editor.getCamera().getCamera(), dir);
        return res.subtract(localToScene(Editor.getCamera().getCamera(), Point3D.ZERO));
    }

    /**
     * 子にギズモの操作対象があるか再帰的に検索します。
     * 
     * @param node 検索対象のノード
     * @return 操作対象のノード
     */
    private Node findLODView(Node node) {
        if(node == null)
            return null;

        // ILODViewインターフェースを実装しているか
        if (node instanceof ILODView)
            return node;

        // 再帰検索
        return findLODView(node.getParent());
    }

    /** サーフェス表示切り替え **/

    public ToggleButton surfaceViewToggle;

    public void onToggleSurfaceView() {
        Editor.getCityModelViewMode().toggleSurfaceViewMode(surfaceViewToggle.isSelected());
    }

    /** LOD表示切り替え **/
    public ToggleGroup lodToggleGroup;

    private void initializeLODToggle() {
        var cityModelViewMode = Editor.getCityModelViewMode();
        selectLODToggle(cityModelViewMode.getLOD());

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            selectLODToggle((int)newValue);
        });

        lodToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // 全てのトグルがオフになる状態は回避する
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }

            int lod = Integer.parseInt(newToggle.getUserData().toString());
            Editor.getCityModelViewMode().lodProperty().setValue(lod);
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
