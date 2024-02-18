package org.plateau.citygmleditor.citygmleditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.plateau.citygmleditor.importers.Importer3D;
import javafx.animation.AnimationTimer;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
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

    private Node moveXGizmoHandle;
    private Node moveYGizmoHandle;
    private Node moveZGizmoHandle;

    private Node rotationXGizmoHandle;
    private Node rotationYGizmoHandle;
    private Node rotationZGizmoHandle;
    
    private Node scaleXGizmoHandle;
    private Node scaleYGizmoHandle;
    private Node scaleZGizmoHandle;

    private Node moveXGizmoView;
    private Node moveYGizmoView;
    private Node moveZGizmoView;

    private Node rotationXGizmoView;
    private Node rotationYGizmoView;
    private Node rotationZGizmoView;

    private Node scaleXGizmoView;
    private Node scaleYGizmoView;
    private Node scaleZGizmoView;
    
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
        controlMode = ControlMode.SELECT;
        moveGizmo = new Group();
        rotationGizmo = new Group();
        scaleGizmo = new Group();
        try{
            moveXGizmoHandle = new Cylinder(20,240);
            moveYGizmoHandle = new Cylinder(20, 240);
            moveZGizmoHandle = new Cylinder(20, 240);
            rotationXGizmoHandle = new Cylinder(260, 20);
            rotationYGizmoHandle = new Cylinder(260, 20);
            rotationZGizmoHandle = new Cylinder(260, 20);
            scaleXGizmoHandle = new Cylinder(20, 240);
            scaleYGizmoHandle = new Cylinder(20, 240);
            scaleZGizmoHandle = new Cylinder(20, 240);
            
            moveXGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_moveX.obj").toExternalForm());
            moveYGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_moveY.obj").toExternalForm());
            moveZGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_moveZ.obj").toExternalForm());
            rotationXGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_rotateX.obj").toExternalForm());
            rotationYGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_rotateY.obj").toExternalForm());
            rotationZGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_rotateZ.obj").toExternalForm());
            scaleXGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_scaleX.obj").toExternalForm());
            scaleYGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_scaleY.obj").toExternalForm());
            scaleZGizmoView = Importer3D.load(CityGMLEditorApp.class.getResource("Locater_scaleZ.obj").toExternalForm());
            
            setMaterialColor(moveXGizmoView);
            setMaterialColor(moveYGizmoView);
            setMaterialColor(moveZGizmoView);
            setMaterialColor(rotationXGizmoView);
            setMaterialColor(rotationYGizmoView);
            setMaterialColor(rotationZGizmoView);
            setMaterialColor(scaleXGizmoView);
            setMaterialColor(scaleYGizmoView);
            setMaterialColor(scaleZGizmoView);

            PhongMaterial transparentMaterial = new PhongMaterial();
            transparentMaterial.setDiffuseColor(Color.rgb(0, 0, 0, 0)); // ディフューズカラー（基本色）
            transparentMaterial.setSpecularColor(Color.rgb(0, 0, 0, 0)); // スペキュラカラー（光の反射）
            transparentMaterial.setSpecularPower(0);

            ((Cylinder) moveXGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) moveYGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) moveZGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) rotationXGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) rotationYGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) rotationZGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) scaleXGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) scaleYGizmoHandle).setMaterial(transparentMaterial);
            ((Cylinder) scaleZGizmoHandle).setMaterial(transparentMaterial);

            moveXGizmoHandle.setRotationAxis(Rotate.X_AXIS);
            moveXGizmoHandle.setRotate(90);
            moveXGizmoHandle.setTranslateZ(-120);
            moveZGizmoHandle.setTranslateY(120);
            moveYGizmoHandle.setRotationAxis(Rotate.Z_AXIS);
            moveYGizmoHandle.setRotate(90);
            moveYGizmoHandle.setTranslateX(-120);

            rotationXGizmoHandle.setRotationAxis(Rotate.X_AXIS);
            rotationXGizmoHandle.setRotate(90);
            rotationYGizmoHandle.setRotationAxis(Rotate.Z_AXIS);
            rotationYGizmoHandle.setRotate(90);

            scaleXGizmoHandle.setRotationAxis(Rotate.X_AXIS);
            scaleXGizmoHandle.setRotate(90);
            scaleXGizmoHandle.setTranslateZ(-120);
            scaleZGizmoHandle.setTranslateY(120);
            scaleYGizmoHandle.setRotationAxis(Rotate.Z_AXIS);
            scaleYGizmoHandle.setRotate(90);
            scaleYGizmoHandle.setTranslateX(-120);

            moveXGizmoHandle.setDepthTest(DepthTest.DISABLE);
            moveYGizmoHandle.setDepthTest(DepthTest.DISABLE);
            moveZGizmoHandle.setDepthTest(DepthTest.DISABLE);
            rotationXGizmoHandle.setDepthTest(DepthTest.DISABLE);
            rotationYGizmoHandle.setDepthTest(DepthTest.DISABLE);
            rotationZGizmoHandle.setDepthTest(DepthTest.DISABLE);
            scaleXGizmoHandle.setDepthTest(DepthTest.DISABLE);
            scaleYGizmoHandle.setDepthTest(DepthTest.DISABLE);
            scaleZGizmoHandle.setDepthTest(DepthTest.DISABLE);

            moveXGizmoView.setDepthTest(DepthTest.DISABLE);
            moveYGizmoView.setDepthTest(DepthTest.DISABLE);
            moveZGizmoView.setDepthTest(DepthTest.DISABLE);
            rotationXGizmoView.setDepthTest(DepthTest.DISABLE);
            rotationYGizmoView.setDepthTest(DepthTest.DISABLE);
            rotationZGizmoView.setDepthTest(DepthTest.DISABLE);
            scaleXGizmoView.setDepthTest(DepthTest.DISABLE);
            scaleYGizmoView.setDepthTest(DepthTest.DISABLE);
            scaleZGizmoView.setDepthTest(DepthTest.DISABLE);

            // Z回転以外は無効
            rotationXGizmoHandle.setVisible(false);
            rotationYGizmoHandle.setVisible(false);

            rotationXGizmoView.setVisible(false);
            rotationYGizmoView.setVisible(false);

            moveGizmo.getChildren().addAll(moveXGizmoHandle, moveYGizmoHandle, moveZGizmoHandle);
            rotationGizmo.getChildren().addAll(rotationXGizmoHandle, rotationYGizmoHandle, rotationZGizmoHandle);
            scaleGizmo.getChildren().addAll(scaleXGizmoHandle, scaleYGizmoHandle, scaleZGizmoHandle);
            
            moveGizmo.getChildren().addAll(moveXGizmoView, moveYGizmoView, moveZGizmoView);
            rotationGizmo.getChildren().addAll(rotationXGizmoView, rotationYGizmoView, rotationZGizmoView);
            scaleGizmo.getChildren().addAll(scaleXGizmoView, scaleYGizmoView, scaleZGizmoView);

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
                    var camera = localToScene(CityGMLEditorApp.getCamera().getCamera(), Point3D.ZERO);
                    var distance = manipulator.getOrigin().distance(camera);
                    var fov = CityGMLEditorApp.getCamera().getCamera().getFieldOfView();
                    var aspect = CityGMLEditorApp.getSceneContent().getSubScene().getWidth() / CityGMLEditorApp.getSceneContent().getSubScene().getHeight();
                    double fovRadians = Math.toRadians(fov);
                    double halfFovTan = Math.tan(fovRadians / 2.0);
                    var rate = aspect * halfFovTan * 2.0 / distance;
                    var scale = (distance * 0.0005);

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
    
    private List<MeshView> findMeshViews(Node node) {
        List<MeshView> meshViews = new ArrayList<>();
        if (node instanceof MeshView) {
            meshViews.add((MeshView) node);
        } else if (node instanceof Group) {
            Group group = (Group) node;
            for (Node child : group.getChildren()) {
                meshViews.addAll(findMeshViews(child));
            }
        }
        return meshViews;
    }

    private void setMaterialColor(Node node) {
        for (var meshView : findMeshViews(node)) {
            PhongMaterial material = (PhongMaterial)meshView.getMaterial();
            var color = material.getDiffuseColor();
            WritableImage image = new WritableImage(1, 1);
            PixelWriter writer = image.getPixelWriter();
            writer.setColor(0, 0, new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getOpacity()));
            material.setSelfIlluminationMap(image);
        }
    }

    public Point3D localToScene(Node node, Point3D pt) {
        Point3D res = node.localToParentTransformProperty().get().transform(pt);
        if (node.getParent() != null) {
            res = localToScene(node.getParent(), res);
        }
        return res;
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
     * マウス操作によるギズモ操作
     * @param delta
     */
    public void updateTransform(Point3D delta) {
        // 建物の座標変換情報を操作値をもとに更新
        Transform worldToLocalTransform = new Rotate(manipulator.getRotation().getX(), Rotate.X_AXIS);
        worldToLocalTransform = worldToLocalTransform.createConcatenation(new Rotate(manipulator.getRotation().getY(), Rotate.Y_AXIS));
        worldToLocalTransform = worldToLocalTransform.createConcatenation(new Rotate(manipulator.getRotation().getZ(), Rotate.Z_AXIS));
        
        // 逆変換行列を取得
        try{
            worldToLocalTransform = worldToLocalTransform.createInverse();
        } catch (Exception e) {
            e.printStackTrace();
        }

        var pivot = manipulator.getOrigin();
        
        // 移動ギズモ
        if (isNodeInTree(currentGizmo, moveGizmo)) {
            double transformFactorX = 0;
            double transformFactorY = 0;
            double transformFactorZ = 0;
            if (isNodeInTree(currentGizmo, moveXGizmoHandle) || isNodeInTree(currentGizmo, moveXGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                transformFactorX = localdelta.getX();
            }
            if (isNodeInTree(currentGizmo, moveYGizmoHandle) || isNodeInTree(currentGizmo, moveYGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                transformFactorY = localdelta.getY();
            }
            if (isNodeInTree(currentGizmo, moveZGizmoHandle) || isNodeInTree(currentGizmo, moveZGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                transformFactorZ = localdelta.getZ();
            }
            manipulator.addTransformCache(new Translate(transformFactorX, transformFactorY, transformFactorZ));
            
            var pivotNew = manipulator.getTransformCache().transform(pivot);
            var offset = pivotNew.subtract(pivot);
            manipulator.setLocation(offset);
        }
        // 回転ギズモ
        else if (isNodeInTree(currentGizmo, rotationGizmo)) {
            double rotateFactorX = 0;
            double rotateFactorY = 0;
            double rotateFactorZ = 0;
            var rotate = new Rotate();
            if (isNodeInTree(currentGizmo, rotationXGizmoHandle) || isNodeInTree(currentGizmo, rotationXGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                rotateFactorX = localdelta.getX();

                rotate = new Rotate(rotateFactorX, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS);
            }
            if (isNodeInTree(currentGizmo, rotationYGizmoHandle) || isNodeInTree(currentGizmo, rotationYGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                rotateFactorY = localdelta.getY();

                rotate = new Rotate(rotateFactorY, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS);
            }
            if (isNodeInTree(currentGizmo, rotationZGizmoHandle) || isNodeInTree(currentGizmo, rotationZGizmoView)) {
                // Z回転以外は無効
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                rotateFactorZ = localdelta.getX();

                rotate = new Rotate(rotateFactorZ, pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS);
            }
            manipulator.setRotation(new Point3D(manipulator.getRotation().getX() + rotateFactorX, manipulator.getRotation().getY() + rotateFactorY, manipulator.getRotation().getZ() + rotateFactorZ));
            
            manipulator.addTransformCache(rotate);
        }
        // スケールギズモ
        else if (isNodeInTree(currentGizmo, scaleGizmo)) {
            double scalingFactorX = 0;
            double scalingFactorY = 0;
            double scalingFactorZ = 0;
            if (isNodeInTree(currentGizmo, scaleXGizmoHandle) || isNodeInTree(currentGizmo, scaleXGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                scalingFactorX = localdelta.getX() / 100.0;
            }
            if (isNodeInTree(currentGizmo, scaleYGizmoHandle) || isNodeInTree(currentGizmo, scaleYGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                scalingFactorY = localdelta.getY() / 100.0;
            }
            if (isNodeInTree(currentGizmo, scaleZGizmoHandle) || isNodeInTree(currentGizmo, scaleZGizmoView)) {
                // 逆変換行列を使用してワールド座標系からローカル座標系へ変換
                var localdelta = worldToLocalTransform.transform(delta);
                scalingFactorZ = localdelta.getZ() / 100.0;
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
        ((ILODSolidView) manipulator.getSolidView()).reflectGML();
    }

    /**
     * ギズモの表示状態を更新
     */
    private void setVisibleGizmo() {
        moveGizmo.setVisible(this.controlMode == ControlMode.MOVE);
        rotationGizmo.setVisible(this.controlMode == ControlMode.ROTATION);
        scaleGizmo.setVisible(this.controlMode == ControlMode.SCALE);

        MeshView outline = CityGMLEditorApp.getFeatureSellection().getOutLine();
        outline.setVisible(this.controlMode == ControlMode.SELECT);
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
