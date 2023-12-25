package org.plateau.citygmleditor.citygmleditor;

import java.io.IOException;
import java.util.ArrayList;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.importers.Importer3D;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Axis;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

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

    private BuildingUnit attachedBuilding;

    private Node currentGizmo;

    private Box debugBoundingBox;
    
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
    }
    
    public ControlMode getControlMode() {
        return controlMode;
    }

    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;

        //選択中ならギズモ切り替え
        if (attachedBuilding != null) {
            setVisibleGizmo();
        }
    }
    
    public void attachBuilding(Node building) {
        attachedBuilding = (BuildingUnit) building;
        
        // ギズモの座標変換を初期化
        getTransforms().clear();
        // ギズモの位置を建物のPivotに合わせて座標変換
        getTransforms().add(new Translate(attachedBuilding.getOrigin().getX(), attachedBuilding.getOrigin().getY(), attachedBuilding.getOrigin().getZ()));
        // 建物の座標変換情報からギズモの座標変換を作成
        getTransforms().addAll(createGizmoTransforms(attachedBuilding));

        setVisibleGizmo();

        // デバッグ用バウンディングボックス表示
        BoundingBox bb = (BoundingBox) building.getBoundsInParent();
        System.out.println(building.getId() + " x:" + bb.getCenterX() + " y:" + bb.getCenterY() + " z:" + bb.getCenterZ());
        if (debugBoundingBox != null) {
            getChildren().remove(debugBoundingBox);
        }
        debugBoundingBox = createBoundingBoxVisual(bb);
        getChildren().add(debugBoundingBox);
        debugBoundingBox.setTranslateX(0.0d);
        debugBoundingBox.setTranslateY(0.0d);
        debugBoundingBox.setTranslateZ(bb.getDepth() / 2);
    }
    
    public void setCurrentGizmo(Node node) {
        currentGizmo = node;
    }

    public void updateTransform(Point3D delta) {
        System.out.println("gizmo:" + new Point3D(getTranslateX(), getTranslateY(), getTranslateZ()));
        System.out.println("building:" + new Point3D(attachedBuilding.getTranslateX(), attachedBuilding.getTranslateY(), attachedBuilding.getTranslateZ()));
        System.out.println("delta:" + delta);
        
        // 建物の座標変換情報を操作値をもとに更新

        // 移動ギズモ
        if (isNodeInTree(currentGizmo, moveGizmo)) {
            //TODO  軸に沿って移動
            if (isNodeInTree(currentGizmo, moveXGizmo)) {
                attachedBuilding.setLocation(new Point3D(attachedBuilding.getLocation().getX() + delta.getX(), attachedBuilding.getLocation().getY(), attachedBuilding.getLocation().getZ()));
            }
            if (isNodeInTree(currentGizmo, moveYGizmo)) {
                attachedBuilding.setLocation(new Point3D(attachedBuilding.getLocation().getX(), attachedBuilding.getLocation().getY() + delta.getY(), attachedBuilding.getLocation().getZ()));
            }
            if (isNodeInTree(currentGizmo, moveZGizmo)) {
                attachedBuilding.setLocation(new Point3D(attachedBuilding.getLocation().getX(), attachedBuilding.getLocation().getY(), attachedBuilding.getLocation().getZ() + delta.getZ()));
            }
        }
        // 回転ギズモ
        else if (isNodeInTree(currentGizmo, rotationGizmo)) {
            if (isNodeInTree(currentGizmo, rotationXGizmo)) {
                attachedBuilding.setRotation(new Point3D(attachedBuilding.getRotation().getX() + delta.getX(), attachedBuilding.getRotation().getY(), attachedBuilding.getRotation().getZ()));
            }
            if (isNodeInTree(currentGizmo, rotationYGizmo)) {
                attachedBuilding.setRotation(new Point3D(attachedBuilding.getRotation().getX(), attachedBuilding.getRotation().getY() + delta.getY(), attachedBuilding.getRotation().getZ()));
            }
            if (isNodeInTree(currentGizmo, rotationZGizmo)) {
                attachedBuilding.setRotation(new Point3D(attachedBuilding.getRotation().getX(), attachedBuilding.getRotation().getY(), attachedBuilding.getRotation().getZ() + delta.getZ()));
            }
        }
        // スケールギズモ
        else if (isNodeInTree(currentGizmo, scaleGizmo)) {
            if (isNodeInTree(currentGizmo, scaleXGizmo)) {
                double scalingFactor = delta.getX() / 100.0;
                attachedBuilding.setScale(new Point3D(attachedBuilding.getScale().getX() + scalingFactor, attachedBuilding.getScale().getY(), attachedBuilding.getScale().getZ()));
            }
            if (isNodeInTree(currentGizmo, scaleYGizmo)) {
                double scalingFactor = delta.getY() / 100.0;
                attachedBuilding.setScale(new Point3D(attachedBuilding.getScale().getX(), attachedBuilding.getScale().getY() + scalingFactor, attachedBuilding.getScale().getZ()));
            }
            if (isNodeInTree(currentGizmo, scaleZGizmo)) {
                double scalingFactor = delta.getZ() / 100.0;
                attachedBuilding.setScale(new Point3D(attachedBuilding.getScale().getX(), attachedBuilding.getScale().getY(), attachedBuilding.getScale().getZ() + scalingFactor));
            }
        }
        
        // 建物の座標変換を初期化
        attachedBuilding.getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        attachedBuilding.getTransforms().addAll(createTransforms(attachedBuilding));

        // ギズモの座標変換を初期化
        getTransforms().clear();
        // ギズモの位置を建物のPivotに合わせて座標変換
        getTransforms().add(new Translate(attachedBuilding.getOrigin().getX(), attachedBuilding.getOrigin().getY(), attachedBuilding.getOrigin().getZ()));
        // 建物の座標変換情報からギズモの座標変換を作成
        getTransforms().addAll(createGizmoTransforms(attachedBuilding));
    }
    
    public void fixTransform() {
        if (attachedBuilding == null)
            return;

        attachedBuilding.refrectGML();
    }

    private ArrayList<Transform> createTransforms(BuildingUnit buildingUnit) {
        var pivot = buildingUnit.getOrigin();
        Translate translate = new Translate(buildingUnit.getLocation().getX(), buildingUnit.getLocation().getY(), buildingUnit.getLocation().getZ());
        Rotate rotateX = new Rotate(buildingUnit.getRotation().getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS);
        Rotate rotateY = new Rotate(buildingUnit.getRotation().getY(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(buildingUnit.getRotation().getZ(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS);
        Scale scale = new Scale(buildingUnit.getScale().getX(), buildingUnit.getScale().getY(), buildingUnit.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ());
        
        ArrayList<Transform> ret = new ArrayList<Transform>();
        ret.add(translate);
        ret.add(rotateX);
        ret.add(rotateY);
        ret.add(rotateZ);
        ret.add(scale);
        return ret;
    }

    private ArrayList<Transform> createGizmoTransforms(BuildingUnit buildingUnit) {
        // var pivot = buildingUnit.getOrigin();
        Translate translate = new Translate(buildingUnit.getLocation().getX(), buildingUnit.getLocation().getY(), buildingUnit.getLocation().getZ());
        Rotate rotateX = new Rotate(buildingUnit.getRotation().getX(), 0, 0, 0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(buildingUnit.getRotation().getY(), 0, 0, 0, Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(buildingUnit.getRotation().getZ(), 0, 0, 0, Rotate.Z_AXIS);
        // Scale scale = new Scale(buildingUnit.getScale().getX(), buildingUnit.getScale().getY(), buildingUnit.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ());
        
        ArrayList<Transform> ret = new ArrayList<Transform>();
        ret.add(translate);
        ret.add(rotateX);
        ret.add(rotateY);
        ret.add(rotateZ);
        // ギズモにスケールは適用しない
        // ret.add(scale);
        return ret;
    }
    private void setVisibleGizmo() {
        moveGizmo.setVisible(this.controlMode == ControlMode.MOVE);
        rotationGizmo.setVisible(this.controlMode == ControlMode.ROTATION);
        scaleGizmo.setVisible(this.controlMode == ControlMode.SCALE);
    }

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

    public boolean isGizmoDragging(Node node) {
        if(isNodeInTree(node, moveGizmo))
            return true;
        if(isNodeInTree(node, rotationGizmo))
            return true;
        if(isNodeInTree(node, scaleGizmo))
            return true;

        return false;
    }

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
