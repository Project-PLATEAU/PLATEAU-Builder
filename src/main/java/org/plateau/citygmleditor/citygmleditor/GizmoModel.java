package org.plateau.citygmleditor.citygmleditor;

import java.io.IOException;
import java.util.ArrayList;
import org.plateau.citygmleditor.citymodel.Building;
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

    private Node moveX;
    private Node moveY;
    private Node moveZ;

    private Node rotationX;
    private Node rotationY;
    private Node rotationZ;
    
    private Node scaleX;
    private Node scaleY;
    private Node scaleZ;

    private Group moveGizmo;
    private Group rotationGizmo;
    private Group scaleGizmo;

    private Node attachedBuilding;

    private Node currentNode;

    private Box debugBoundingBox;
    
    public GizmoModel() {
        moveGizmo = new Group();
        rotationGizmo = new Group();
        scaleGizmo = new Group();
        try{
            moveX = Importer3D.load(ContentModel.class.getResource("Locater_moveX.obj").toExternalForm());
            moveY = Importer3D.load(ContentModel.class.getResource("Locater_moveY.obj").toExternalForm());
            moveZ = Importer3D.load(ContentModel.class.getResource("Locater_moveZ.obj").toExternalForm());
            rotationX = Importer3D.load(ContentModel.class.getResource("Locater_rotateX.obj").toExternalForm());
            rotationY = Importer3D.load(ContentModel.class.getResource("Locater_rotateY.obj").toExternalForm());
            rotationZ = Importer3D.load(ContentModel.class.getResource("Locater_rotateZ.obj").toExternalForm());
            scaleX = Importer3D.load(ContentModel.class.getResource("Locater_scaleX.obj").toExternalForm());
            scaleY = Importer3D.load(ContentModel.class.getResource("Locater_scaleY.obj").toExternalForm());
            scaleZ = Importer3D.load(ContentModel.class.getResource("Locater_scaleZ.obj").toExternalForm());
            
            moveGizmo.getChildren().addAll(moveX, moveY, moveZ);
            rotationGizmo.getChildren().addAll(rotationX, rotationY, rotationZ);
            scaleGizmo.getChildren().addAll(scaleX, scaleY, scaleZ);
            
            //モデル補正
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
        BoundingBox bb = (BoundingBox) building.getBoundsInParent();
        System.out.println(building.getId() + " x:" + bb.getCenterX() + " y:" + bb.getCenterY() + " z:" + bb.getCenterZ());
        // setTranslateX(bb.getCenterX());
        // setTranslateY(bb.getCenterY());
        // setTranslateZ(bb.getCenterZ());
        
        // 座標変換を初期化
        getTransforms().clear();
        
        // setTranslateX(((BuildingUnit) building).getOrigin().getX());
        // setTranslateY(((BuildingUnit) building).getOrigin().getY());
        // setTranslateZ(((BuildingUnit) building).getOrigin().getZ());
        getTransforms().add(new Translate(((BuildingUnit) building).getOrigin().getX(), ((BuildingUnit) building).getOrigin().getY(), ((BuildingUnit) building).getOrigin().getZ()));

        var transforms2 = createGizmoTransforms((BuildingUnit) building);
        
        getTransforms().addAll(transforms2);

        if (debugBoundingBox != null) {
            getChildren().remove(debugBoundingBox);
        }
        debugBoundingBox = createBoundingBoxVisual(bb);
        getChildren().add(debugBoundingBox);
        debugBoundingBox.setTranslateX(0.0d);
        debugBoundingBox.setTranslateY(0.0d);
        debugBoundingBox.setTranslateZ(bb.getDepth() / 2);

        attachedBuilding = building;

        setVisibleGizmo();
    }
    
    public void setCurrentNode(Node node) {
        currentNode = node;
    }

    public void updateTransform(Point3D delta) {
        System.err.println("gizmo:" + new Point3D(getTranslateX(), getTranslateY(), getTranslateZ()));
        System.err.println("building:" + new Point3D(attachedBuilding.getTranslateX(), attachedBuilding.getTranslateY(), attachedBuilding.getTranslateZ()));
        System.err.println("delta:" + delta);
        System.err.println("attach:" + attachedBuilding.getTranslateX() + " " + attachedBuilding.getTranslateX() + " " + attachedBuilding.getTranslateZ());
        // getTransforms().add(new Translate(delta.getX(), delta.getY(), delta.getZ()));
        
        if (isNodeInTree(currentNode, moveGizmo)) {
            //TODO  軸に沿って移動
            if (isNodeInTree(currentNode, moveX)) {
                // getTransforms().add(new Translate(delta.getX(), 0, 0));
                // attachedBuilding.getTransforms().add(new Translate(delta.getX(), 0, 0));
                updateTransforms((BuildingUnit)attachedBuilding, 0, new Point3D(delta.getX(), 0, 0));
            }
            if (isNodeInTree(currentNode, moveY)) {
                // getTransforms().add(new Translate(0, delta.getY(), 0));
                // attachedBuilding.getTransforms().add(new Translate(0, delta.getY(), 0));
                updateTransforms((BuildingUnit)attachedBuilding, 0, new Point3D(0, delta.getY(), 0));
            }
            if (isNodeInTree(currentNode, moveZ)) {
                // getTransforms().add(new Translate(0, 0, delta.getZ()));
                // attachedBuilding.getTransforms().add(new Translate(0, 0, delta.getZ()));
                updateTransforms((BuildingUnit)attachedBuilding, 0, new Point3D(0, 0, delta.getZ()));
            }
        }
        else if (isNodeInTree(currentNode, rotationGizmo)) {
            // var pivot = ((BuildingUnit) attachedBuilding).getLocation();

            if (isNodeInTree(currentNode, rotationX)) {
                // getTransforms().add(new Rotate(delta.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS));   
                // attachedBuilding.getTransforms().add(new Rotate(delta.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS));
                updateTransforms((BuildingUnit)attachedBuilding, 1, new Point3D(delta.getX(), 0, 0));
            }
            if (isNodeInTree(currentNode, rotationY)) {
                // getTransforms().add(new Rotate(delta.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS));
                // attachedBuilding.getTransforms().add(new Rotate(delta.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS));
                updateTransforms((BuildingUnit)attachedBuilding, 1, new Point3D(0, delta.getY(), 0));
            }
            if (isNodeInTree(currentNode, rotationZ)) {
                // getTransforms().add(new Rotate(delta.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS));
                // attachedBuilding.getTransforms().add(new Rotate(delta.getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS));
                updateTransforms((BuildingUnit)attachedBuilding, 1, new Point3D(0, 0, delta.getZ()));
            }
        }
        else if (isNodeInTree(currentNode, scaleGizmo)) {
            // var pivot = ((BuildingUnit) attachedBuilding).getLocation();
            
            if (isNodeInTree(currentNode, scaleX)) {
                // double scalingFactor = 1.0 + delta.getX() / 100.0;
                // attachedBuilding.getTransforms().add(new Scale(scalingFactor, 1.0, 1.0, pivot.getX(), pivot.getY(), pivot.getZ()));
                double scalingFactor = delta.getX() / 100.0;
                updateTransforms((BuildingUnit)attachedBuilding, 2, new Point3D(scalingFactor, 0, 0));
            }
            if (isNodeInTree(currentNode, scaleY)) {
                // double scalingFactor = 1.0 + delta.getY() / 100.0;
                // attachedBuilding.getTransforms().add(new Scale(1.0, scalingFactor, 1.0, pivot.getX(), pivot.getY(), pivot.getZ()));
                double scalingFactor = delta.getY() / 100.0;
                updateTransforms((BuildingUnit)attachedBuilding, 2, new Point3D(0, scalingFactor, 0));
            }
            if (isNodeInTree(currentNode, scaleZ)) {
                // double scalingFactor = 1.0 + delta.getZ() / 100.0;
                // attachedBuilding.getTransforms().add(new Scale(1.0, 1.0, scalingFactor, pivot.getX(), pivot.getY(), pivot.getZ()));
                double scalingFactor = delta.getZ() / 100.0;
                updateTransforms((BuildingUnit)attachedBuilding, 2, new Point3D(0, 0, scalingFactor));
            }
        }
        var transforms = createTransforms((BuildingUnit)attachedBuilding);

        attachedBuilding.getTransforms().clear();
        attachedBuilding.getTransforms().addAll(transforms);

        var transforms2 = createGizmoTransforms((BuildingUnit)attachedBuilding);
        getTransforms().clear();
        getTransforms().add(new Translate(((BuildingUnit) attachedBuilding).getOrigin().getX(), ((BuildingUnit) attachedBuilding).getOrigin().getY(), ((BuildingUnit) attachedBuilding).getOrigin().getZ()));
        getTransforms().addAll(transforms2);
    }
    
    public void fixTransform() {
        if (attachedBuilding == null)
            return;

        // TODO GMLへ反映
    }

    private void updateTransforms(BuildingUnit buildingUnit, int type, Point3D delta) {
        switch (type) {
            case 0:
                buildingUnit.setLocation(new Point3D(buildingUnit.getLocation().getX() + delta.getX(), buildingUnit.getLocation().getY() + delta.getY(), buildingUnit.getLocation().getZ() + delta.getZ()));
                break;
            case 1:
                buildingUnit.setRotation(new Point3D(buildingUnit.getRotation().getX() + delta.getX(), buildingUnit.getRotation().getY() + delta.getY(), buildingUnit.getRotation().getZ() + delta.getZ()));
                break;
            case 2:
                buildingUnit.setScale(new Point3D(buildingUnit.getScale().getX() + delta.getX(), buildingUnit.getScale().getY() + delta.getY(), buildingUnit.getScale().getZ() + delta.getZ()));
                break;
        }
    }
    
    private ArrayList<Transform> createTransforms(BuildingUnit buildingUnit) {
        var pivot = buildingUnit.getOrigin();//new Point3D(buildingUnit.getLocation().getX() + buildingUnit.getOrigin().getX(), buildingUnit.getLocation().getY() + buildingUnit.getOrigin().getY(), buildingUnit.getLocation().getZ() + buildingUnit.getOrigin().getZ());
        System.err.println("p0:"+pivot);
        Translate translate = new Translate(buildingUnit.getLocation().getX(), buildingUnit.getLocation().getY(), buildingUnit.getLocation().getZ());
        // pivot = translate.transform(pivot);System.err.println("pt:"+pivot);
        Rotate rotateX = new Rotate(buildingUnit.getRotation().getX(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.X_AXIS);
        // pivot = rotateX.transform(pivot);System.err.println("pr1:"+pivot);
        Rotate rotateY = new Rotate(buildingUnit.getRotation().getY(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Y_AXIS);
        // pivot = rotateY.transform(pivot);System.err.println("pr2:"+pivot);
        Rotate rotateZ = new Rotate(buildingUnit.getRotation().getZ(), pivot.getX(), pivot.getY(), pivot.getZ(), Rotate.Z_AXIS);
        // pivot = rotateZ.transform(pivot);System.err.println("pr3:"+pivot);
        Scale scale = new Scale(buildingUnit.getScale().getX(), buildingUnit.getScale().getY(), buildingUnit.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ());
        // pivot = scale.transform(pivot);System.err.println("ps:"+pivot);
        ArrayList<Transform> ret = new ArrayList<Transform>();
        ret.add(translate);
        ret.add(rotateX);
        ret.add(rotateY);
        ret.add(rotateZ);
        ret.add(scale);
        return ret;
    }

    private ArrayList<Transform> createGizmoTransforms(BuildingUnit buildingUnit) {
        var pivot = buildingUnit.getOrigin();//new Point3D(buildingUnit.getLocation().getX() + buildingUnit.getOrigin().getX(), buildingUnit.getLocation().getY() + buildingUnit.getOrigin().getY(), buildingUnit.getLocation().getZ() + buildingUnit.getOrigin().getZ());
        System.err.println("p0:"+pivot);
        Translate translate = new Translate(buildingUnit.getLocation().getX(), buildingUnit.getLocation().getY(), buildingUnit.getLocation().getZ());
        // pivot = translate.transform(pivot);System.err.println("pt:"+pivot);
        Rotate rotateX = new Rotate(buildingUnit.getRotation().getX(), 0, 0, 0, Rotate.X_AXIS);
        // pivot = rotateX.transform(pivot);System.err.println("pr1:"+pivot);
        Rotate rotateY = new Rotate(buildingUnit.getRotation().getY(), 0, 0, 0, Rotate.Y_AXIS);
        // pivot = rotateY.transform(pivot);System.err.println("pr2:"+pivot);
        Rotate rotateZ = new Rotate(buildingUnit.getRotation().getZ(), 0, 0, 0, Rotate.Z_AXIS);
        // pivot = rotateZ.transform(pivot);System.err.println("pr3:"+pivot);
        Scale scale = new Scale(buildingUnit.getScale().getX(), buildingUnit.getScale().getY(), buildingUnit.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ());
        // pivot = scale.transform(pivot);System.err.println("ps:"+pivot);
        ArrayList<Transform> ret = new ArrayList<Transform>();
        ret.add(translate);
        ret.add(rotateX);
        ret.add(rotateY);
        ret.add(rotateZ);
        //ギズモにスケールは適用しない
        //ret.add(scale);
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
