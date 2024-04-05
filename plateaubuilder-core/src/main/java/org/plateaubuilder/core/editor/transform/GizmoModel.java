package org.plateaubuilder.core.editor.transform;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.io.mesh.importers.Importer3D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

            var resourceDirectory = "/org/plateaubuilder/core/";
            moveXGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_move.obj").toExternalForm());
            moveYGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_move.obj").toExternalForm());
            moveZGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_move.obj").toExternalForm());
            rotationXGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_rotate.obj").toExternalForm());
            rotationYGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_rotate.obj").toExternalForm());
            rotationZGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_rotate.obj").toExternalForm());
            scaleXGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_scale.obj").toExternalForm());
            scaleYGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_scale.obj").toExternalForm());
            scaleZGizmoView = Importer3D.load(getClass().getResource(resourceDirectory + "Locater_scale.obj").toExternalForm());
            
            moveXGizmoView.setRotationAxis(Rotate.X_AXIS);
            moveXGizmoView.setRotate(270);
            moveXGizmoView.setTranslateZ(-120);
            moveXGizmoView.setTranslateY(-120);
            moveYGizmoView.setRotationAxis(Rotate.Z_AXIS);
            moveYGizmoView.setRotate(90);
            moveYGizmoView.setTranslateX(-120);
            moveYGizmoView.setTranslateY(-120);

            rotationXGizmoView.setRotationAxis(Rotate.X_AXIS);
            rotationXGizmoView.setRotate(90);
            rotationYGizmoView.setRotationAxis(Rotate.Z_AXIS);
            rotationYGizmoView.setRotate(270);

            scaleXGizmoView.setRotationAxis(Rotate.X_AXIS);
            scaleXGizmoView.setRotate(270);
            scaleXGizmoView.setTranslateZ(-110);
            scaleXGizmoView.setTranslateY(-110);
            scaleYGizmoView.setRotationAxis(Rotate.Z_AXIS);
            scaleYGizmoView.setRotate(90);
            scaleYGizmoView.setTranslateX(-110);
            scaleYGizmoView.setTranslateY(-110);

            setMaterialColor(moveXGizmoView, Color.RED);
            setMaterialColor(moveYGizmoView, Color.rgb(0, 255, 0));
            setMaterialColor(moveZGizmoView, Color.BLUE);
            setMaterialColor(rotationXGizmoView, Color.RED);
            setMaterialColor(rotationYGizmoView, Color.rgb(0, 255, 0));
            setMaterialColor(rotationZGizmoView, Color.BLUE);
            setMaterialColor(scaleXGizmoView, Color.RED);
            setMaterialColor(scaleYGizmoView, Color.rgb(0, 255, 0));
            setMaterialColor(scaleZGizmoView, Color.BLUE);

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
                    var camera = localToScene(Editor.getCamera().getCamera(), Point3D.ZERO);
                    var distance = manipulator.getOrigin().distance(camera);
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
    
    /**
     * 指定されたノード以下に含まれるすべての MeshView を検索します。
     *
     * @param node 検索対象のノード
     * @return 検索された MeshView のリスト
     */
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

    /**
     * ギズモ用にマテリアルを調整します。
     *
     * @param node 色を調整するノード
     */
    private void setMaterialColor(Node node, Color color) {
        for (var meshView : findMeshViews(node)) {
            PhongMaterial material = (PhongMaterial)meshView.getMaterial();
            WritableImage image = new WritableImage(1, 1);
            PixelWriter writer = image.getPixelWriter();
            writer.setColor(0, 0, color);
            material.setSelfIlluminationMap(image);
            material.setDiffuseColor(color); // ディフューズカラー（基本色）
            material.setSpecularColor(Color.rgb(0, 0, 0, 0)); // スペキュラカラー（光の反射）
            material.setSpecularPower(0);
        }
    }

    /**
     * ローカル座標系からシーン座標系へ座標を変換します。
     *
     * @param node 座標を変換するノード
     * @param pt 変換する座標
     * @return 変換後の座標
     */
    public Point3D localToScene(Node node, Point3D pt) {
        Point3D res = node.localToParentTransformProperty().get().transform(pt);
        if (node.getParent() != null) {
            res = localToScene(node.getParent(), res);
        }
        return res;
    }
    
    /**
     * ギズモの操作モードを返します。
     * 
     * @return 操作モード
     */
    public ControlMode getControlMode() {
        return controlMode;
    }

    /**
     * ギズモの操作モードを切り替えます。
     * 
     * @param controlMode 操作モード
     */
    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;

        //選択中ならギズモ切り替え
        if (manipulator != null) {
            setVisibleGizmo();
        }
    }
    
    /**
     * ギズモの操作対象を返します。
     * 
     * @return ギズモの操作対象
     */
    public Node getAttachNode() {
        if (manipulator == null)
            return null;
        return manipulator.getSolidView();
    }

    /**
     * ギズモを操作対象に設定します。
     * このメソッドは、操作対象に合わせてギズモ、アウトラインの表示も更新します。
     * 
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
    }
    
    /**
     * 操作ギズモを設定します。
     * 
     * @param node 操作ギズモのノード
     */
    public void setCurrentGizmo(Node node) {
        currentGizmo = node;
    }

    /**
     * マウス操作によりギズモを更新します。
     * このメソッドは、ギズモの操作量をギズモの操作対象へ反映します。
     * 
     * @param delta ワールド上のマウス移動量
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
    }
    
    /**
     * ギズモ操作を確定します。
     * このメソッドは、ギズモの操作値をGMLへ反映します。
     */
    public void fixTransform() {
        if (manipulator == null)
            return;

        // GMLに書き戻す
        ((ILODSolidView) manipulator.getSolidView()).reflectGML();
    }

    /**
     * ギズモの表示状態を更新します。
     */
    private void setVisibleGizmo() {
        moveGizmo.setVisible(this.controlMode == ControlMode.MOVE);
        rotationGizmo.setVisible(this.controlMode == ControlMode.ROTATION);
        scaleGizmo.setVisible(this.controlMode == ControlMode.SCALE);

        Editor.getFeatureSellection().outlineVisibleProperty().set(this.controlMode == ControlMode.SELECT);
    }

    /**
     * 指定されたノードがギズモ内に存在するかどうかを判定します。
     * 
     * @param node 検索対象のノード
     * @return ノードがギズモ内に存在する場合はtrue、それ以外の場合はfalse
     */
    public boolean isNodeInGizmo(Node node) {
        if (isNodeInTree(node, moveGizmo))
            return true;
        if (isNodeInTree(node, rotationGizmo))
            return true;
        if (isNodeInTree(node, scaleGizmo))
            return true;

        return false;
    }

    /**
     * 指定されたノードが木構造内に存在するかどうかを判定します。
     * 
     * @param findNode 検索するノード
     * @param serchNode 木構造のルートノード
     * @return ノードが子に存在する場合はtrue、それ以外の場合はfalse
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
