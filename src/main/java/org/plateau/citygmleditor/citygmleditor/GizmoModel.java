package org.plateau.citygmleditor.citygmleditor;

import java.io.IOException;
import org.plateau.citygmleditor.importers.Importer3D;
import javafx.animation.AnimationTimer;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;

/**
 * 
 */
public class GizmoModel extends Parent {
    public enum ControlMode {
        SELECT, MOVE, ROTATION, SCALE,
    }

    private ControlMode controlMode;

    private Node moveXGizmo;
    private Node moveYGizmo;
    private Node moveZGizmo;

    private Node rotationXGizmo;
    private Node rotationYGizmo;
    private Node rotationZGizmo;
    
    private Node scaleXGizmo;
    private Node scaleYGizmo;
    private Node scaleZGizmo;

    private Group moveGizmo;
    private Group rotationGizmo;
    private Group scaleGizmo;

    private TransformManipulator manipulator;

    private Node currentGizmo;

    // private Box debugBoundingBox;
    
    /**
     * コンストラクタ
     */
    public GizmoModel() {
        moveGizmo = new Group();
        rotationGizmo = new Group();
        scaleGizmo = new Group();
        try{
            moveXGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_moveX.obj").toExternalForm());
            moveYGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_moveY.obj").toExternalForm());
            moveZGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_moveZ.obj").toExternalForm());
            rotationXGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_rotateX.obj").toExternalForm());
            rotationYGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_rotateY.obj").toExternalForm());
            rotationZGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_rotateZ.obj").toExternalForm());
            scaleXGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_scaleX.obj").toExternalForm());
            scaleYGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_scaleY.obj").toExternalForm());
            scaleZGizmo = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_scaleZ.obj").toExternalForm());
            
            moveXGizmo.setDepthTest(DepthTest.DISABLE);
            moveYGizmo.setDepthTest(DepthTest.DISABLE);
            moveZGizmo.setDepthTest(DepthTest.DISABLE);
            rotationXGizmo.setDepthTest(DepthTest.DISABLE);
            rotationYGizmo.setDepthTest(DepthTest.DISABLE);
            rotationZGizmo.setDepthTest(DepthTest.DISABLE);
            scaleXGizmo.setDepthTest(DepthTest.DISABLE);
            scaleYGizmo.setDepthTest(DepthTest.DISABLE);
            scaleZGizmo.setDepthTest(DepthTest.DISABLE);

            // Z回転以外は無効
            rotationXGizmo.setVisible(false);
            rotationYGizmo.setVisible(false);

            moveGizmo.getChildren().addAll(moveXGizmo, moveYGizmo, moveZGizmo);
            rotationGizmo.getChildren().addAll(rotationXGizmo, rotationYGizmo, rotationZGizmo);
            scaleGizmo.getChildren().addAll(scaleXGizmo, scaleYGizmo, scaleZGizmo);
            
            // モデル回転方向補正
            var rot1 = new Rotate(90.0d, Rotate.X_AXIS);
            var rot2 = new Rotate(-90.0d, Rotate.Y_AXIS);
            moveGizmo.getTransforms().addAll(rot1, rot2);
            rotationGizmo.getTransforms().addAll(rot1, rot2);
            scaleGizmo.getTransforms().addAll(rot1, rot2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getChildren().addAll(moveGizmo, rotationGizmo, scaleGizmo);
        moveGizmo.setVisible(false);
        rotationGizmo.setVisible(false);
        scaleGizmo.setVisible(false);

        AnimationTimer animationTimer = new AnimationTimer() {
            public void handle(long now) {
                if (manipulator != null) {
                    // カメラからの距離でギズモのスケールを補正
                    var camera = CityGMLEditorApp.getCamera().getCameraPosition();
                    var distance = Math.abs(Math.sqrt(Math.pow(camera.getX() - manipulator.getOrigin().getX(), 2) + Math.pow(camera.getY() - manipulator.getOrigin().getY(), 2) + Math.pow(camera.getZ() - manipulator.getOrigin().getZ(), 2)));
                    var scale = distance * 0.0005;
                    // モデル回転方向補正
                    var rot1 = new Rotate(90.0d, Rotate.X_AXIS);
                    var rot2 = new Rotate(-90.0d, Rotate.Y_AXIS);
                    moveGizmo.getTransforms().clear();
                    moveGizmo.getTransforms().addAll(rot1, rot2);
                    moveGizmo.getTransforms().add(new Scale(scale, scale, scale));
                    rotationGizmo.getTransforms().clear();
                    rotationGizmo.getTransforms().addAll(rot1, rot2);
                    rotationGizmo.getTransforms().add(new Scale(scale, scale, scale));
                    scaleGizmo.getTransforms().clear();
                    scaleGizmo.getTransforms().addAll(rot1, rot2);
                    scaleGizmo.getTransforms().add(new Scale(scale, scale, scale));
                }
            }
        };
        animationTimer.start();
    }
    
    /**
     * ギズモの操作モードを返す
     * @return
     */
    public ControlMode getControlMode() {
        return controlMode;
    }

    /**
     * ギズモの操作モードを切り替え
     * @param controlMode
     */
    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;

        //選択中ならギズモ切り替え
        if (manipulator != null) {
            setVisibleGizmo();
        }
    }
    
    /**
     * ギズモを操作対象にセット
     * @param manipulator ギズモの操作対象
     */
    public void attachManipulator(TransformManipulator manipulator) {
        this.manipulator = manipulator;
        
        // ギズモの座標変換を初期化
        getTransforms().clear();
        // 建物の座標変換情報からギズモの座標変換を作成
        getTransforms().add(manipulator.getTransformCache());
        // ギズモの位置を建物のPivotに合わせて座標変換
        getTransforms().add(new Translate(manipulator.getOrigin().getX(), manipulator.getOrigin().getY(), manipulator.getOrigin().getZ()));

        setVisibleGizmo();

        var pivot = manipulator.getOrigin();
        
        // 選択中ワイヤーフレームにも適用
        MeshView outline = CityGMLEditorApp.getFeatureSellection().getOutLine();
        // 建物の座標変換を初期化
        outline.getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        outline.getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        outline.getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
        
        // デバッグ用バウンディングボックス表示
        // BoundingBox bb = (BoundingBox) attachedBuilding.getSolidView().getBoundsInParent();
        // if (debugBoundingBox != null) {
        //     getChildren().remove(debugBoundingBox);
        // }
        // debugBoundingBox = createBoundingBoxVisual(bb);
        // getChildren().add(debugBoundingBox);
        // debugBoundingBox.setTranslateX(0.0d);
        // debugBoundingBox.setTranslateY(0.0d);
        // debugBoundingBox.setTranslateZ(bb.getDepth() / 2);
    }
    
    /**
     * 操作中ギズモを設定
     * @param node
     */
    public void setCurrentGizmo(Node node) {
        currentGizmo = node;
    }

    /**
     * スナップによるギズモ操作
     * @param index
     * @param value
     */
    public void snapTransform(int index, double value) {
        double moveX = index == 0 ? value : 0;
        double moveY = index == 1 ? value : 0;
        double moveZ = index == 2 ? value : 0;
        double rotateX = index == 3 ? value : 0;
        double rotateY = index == 4 ? value : 0;
        double rotateZ = index == 5 ? value : 0;
        double scaleX = index == 6 ? value : 0;
        double scaleY = index == 7 ? value : 0;
        double scaleZ = index == 8 ? value : 0;
        
        var pivot = manipulator.getOrigin();

        manipulator.setLocation(new Point3D(moveX, moveY, moveZ));
        manipulator.addTransformCache(new Translate(moveX, moveY, moveZ));
        
        manipulator.setRotation(new Point3D(rotateX, rotateY, rotateZ));
        Transform rotate = new Rotate();
        rotate = rotate.createConcatenation(new Rotate(rotateX, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS));
        rotate = rotate.createConcatenation(new Rotate(rotateY, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS));
        rotate = rotate.createConcatenation(new Rotate(rotateZ, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS));
        manipulator.addTransformCache(rotate);

        manipulator.setScale(new Point3D(manipulator.getScale().getX() + scaleX, manipulator.getScale().getY() + scaleY, manipulator.getScale().getZ() + scaleZ));

        // 建物の座標変換を初期化
        manipulator.getSolidView().getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        manipulator.getSolidView().getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        manipulator.getSolidView().getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));

        // ギズモの座標変換を初期化
        getTransforms().clear();
        // 建物の座標変換情報からギズモの座標変換を作成
        getTransforms().add(manipulator.getTransformCache());
        // ギズモの位置を建物のPivotに合わせて座標変換
        getTransforms().add(new Translate(manipulator.getOrigin().getX(), manipulator.getOrigin().getY(), manipulator.getOrigin().getZ()));
    }

    /**
     * マウス操作によるギズモ操作
     * @param delta
     */
    public void updateTransform(Point3D delta) {
        // System.out.println("delta:" + delta);

        // 建物の座標変換情報を操作値をもとに更新
        Transform worldToLocalTransform = new Rotate(manipulator.getRotation().getX(), Rotate.X_AXIS);
        worldToLocalTransform = worldToLocalTransform.createConcatenation(new Rotate(manipulator.getRotation().getY(), Rotate.Y_AXIS));
        worldToLocalTransform = worldToLocalTransform.createConcatenation(new Rotate(manipulator.getRotation().getZ(), Rotate.Z_AXIS));
        
        var pivot = manipulator.getOrigin();
        
        // 移動ギズモ
        if (isNodeInTree(currentGizmo, moveGizmo)) {
            double transformFactorX = 0;
            double transformFactorY = 0;
            double transformFactorZ = 0;
            if (isNodeInTree(currentGizmo, moveXGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.X_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                transformFactorX = projection.getX();
            }
            if (isNodeInTree(currentGizmo, moveYGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.Y_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                transformFactorY = projection.getY();
            }
            if (isNodeInTree(currentGizmo, moveZGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.Z_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                transformFactorZ = projection.getZ();
            }
            manipulator.setLocation(new Point3D(manipulator.getLocation().getX() + transformFactorX, manipulator.getLocation().getY() + transformFactorY, manipulator.getLocation().getZ() + transformFactorZ));
            
            manipulator.addTransformCache(new Translate(transformFactorX, transformFactorY, transformFactorZ));
        }
        // 回転ギズモ
        else if (isNodeInTree(currentGizmo, rotationGizmo)) {
            double rotateFactorX = 0;
            double rotateFactorY = 0;
            double rotateFactorZ = 0;
            var rotate = new Rotate();
            if (isNodeInTree(currentGizmo, rotationXGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.X_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                rotateFactorX = projection.getX();

                rotate = new Rotate(projection.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS);
            }
            if (isNodeInTree(currentGizmo, rotationYGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.Y_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                rotateFactorY = projection.getY();

                rotate = new Rotate(projection.getY(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS);
            }
            if (isNodeInTree(currentGizmo, rotationZGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.Z_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                // Z回転以外は無効
                projection = new Point3D(0, 0, delta.getX());
                rotateFactorZ = projection.getZ();

                rotate = new Rotate(projection.getZ(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS);
            }
            manipulator.setRotation(new Point3D(manipulator.getRotation().getX() + rotateFactorX, manipulator.getRotation().getY() + rotateFactorY, manipulator.getRotation().getZ() + rotateFactorZ));
            
            manipulator.addTransformCache(rotate);
        }
        // スケールギズモ
        else if (isNodeInTree(currentGizmo, scaleGizmo)) {
            double scalingFactorX = 0;
            double scalingFactorY = 0;
            double scalingFactorZ = 0;
            if (isNodeInTree(currentGizmo, scaleXGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.X_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                scalingFactorX = projection.getX() / 100.0;
            }
            if (isNodeInTree(currentGizmo, scaleYGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.Y_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                scalingFactorY = projection.getY() / 100.0;
            }
            if (isNodeInTree(currentGizmo, scaleZGizmo)) {
                var axisVector = worldToLocalTransform.transform(Rotate.Z_AXIS).normalize();
                var projectionMagnitude = delta.dotProduct(axisVector);
                var projection = axisVector.multiply(projectionMagnitude);
                scalingFactorZ = projection.getZ() / 100.0;
            }
            manipulator.setScale(new Point3D(manipulator.getScale().getX() + scalingFactorX, manipulator.getScale().getY() + scalingFactorY, manipulator.getScale().getZ() + scalingFactorZ));
            // スケールは別途で毎回適用
            //attachedBuilding.addTransformCache(new Scale(attachedBuilding.getScale().getX(), attachedBuilding.getScale().getY(), attachedBuilding.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));
        }

        // 建物の座標変換を初期化
        manipulator.getSolidView().getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        manipulator.getSolidView().getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        manipulator.getSolidView().getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));

        // ギズモの座標変換を初期化
        getTransforms().clear();
        // 建物の座標変換情報からギズモの座標変換を作成
        getTransforms().add(manipulator.getTransformCache());
        // ギズモの位置を建物のPivotに合わせて座標変換
        getTransforms().add(new Translate(manipulator.getOrigin().getX(), manipulator.getOrigin().getY(), manipulator.getOrigin().getZ()));
        
        // 選択中ワイヤーフレームにも適用
        MeshView outline = CityGMLEditorApp.getFeatureSellection().getOutLine();
        // 建物の座標変換を初期化
        outline.getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        outline.getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        outline.getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));

    }
    
    /**
     * ギズモ操作を確定
     */
    public void fixTransform() {
        if (manipulator == null)
            return;

        // GMLに書き戻す
        ((ILODSolidView) manipulator.getSolidView()).refrectGML();
    }

    /**
     * ギズモの表示状態を更新
     */
    private void setVisibleGizmo() {
        moveGizmo.setVisible(this.controlMode == ControlMode.MOVE);
        rotationGizmo.setVisible(this.controlMode == ControlMode.ROTATION);
        scaleGizmo.setVisible(this.controlMode == ControlMode.SCALE);
    }

    /**
     * デバッグ用バウンディングボックス
     * @param boundingBox
     * @return
     */
    private Box createBoundingBoxVisual(BoundingBox boundingBox) {
        double width = boundingBox.getWidth();
        double height = boundingBox.getHeight();
        double depth = boundingBox.getDepth();

        Box boundingBoxVisual = new Box(width, height, depth);
        boundingBoxVisual.setTranslateX((boundingBox.getMinX() + boundingBox.getMaxX()) / 2);
        boundingBoxVisual.setTranslateY((boundingBox.getMinY() + boundingBox.getMaxY()) / 2);
        boundingBoxVisual.setTranslateZ((boundingBox.getMinZ() + boundingBox.getMaxZ()) / 2);
        boundingBoxVisual.setMaterial(new PhongMaterial(new Color(1.0d, 0.0d, 0.0d, 0.3d)));

        return boundingBoxVisual;
    }

    /**
     * ドラッグ操作中のギズモがあるか検索
     * @param node
     * @return
     */
    public boolean isGizmoDragging(Node node) {
        if (isNodeInTree(node, moveGizmo))
            return true;
        if (isNodeInTree(node, rotationGizmo))
            return true;
        if (isNodeInTree(node, scaleGizmo))
            return true;

        return false;
    }

    /**
     * ノードの子に特定のノードがあるか再帰検索
     * @param findNode
     * @param serchNode
     * @return
     */
    private boolean isNodeInTree(Node findNode, Node serchNode) {
        if (findNode == null || serchNode == null) {
            return false;
        }
        // 同じかチェック
        if (findNode.equals(serchNode)) {
            return true;
        }
        // Parentである場合、子ノードを再帰検索
        if (serchNode instanceof Parent) {
            Parent parentB = (Parent) serchNode;
            for (Node child : parentB.getChildrenUnmodifiable()) {
                if (isNodeInTree(findNode, child)) {
                    return true;
                }
            }
        }
        return false;
    }
}
