package org.plateau.citygmleditor.citygmleditor;

import java.io.IOException;
import org.plateau.citygmleditor.citymodel.Building;
import org.plateau.citygmleditor.importers.Importer3D;
import javafx.geometry.BoundingBox;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class GizmoModel extends Parent {
    
    public enum ControlMode {
        SELECT, MOVE, ROTATION, SCALE,
    }

    private ControlMode controlMode;

    private Group moveGizmo;
    private Group rotationGizmo;
    private Group scaleGizmo;

    private Node selectedBuilding;

    private Box debugBoundingBox;
    
    public GizmoModel() {
        moveGizmo = new Group();
        rotationGizmo = new Group();
        scaleGizmo = new Group();
        try{
            Node moveX = Importer3D.load(ContentModel.class.getResource("Locater_moveX.obj").toExternalForm());
            Node moveY = Importer3D.load(ContentModel.class.getResource("Locater_moveY.obj").toExternalForm());
            Node moveZ = Importer3D.load(ContentModel.class.getResource("Locater_moveZ.obj").toExternalForm());
            Node rotationX = Importer3D.load(ContentModel.class.getResource("Locater_rotateX.obj").toExternalForm());
            Node rotationY = Importer3D.load(ContentModel.class.getResource("Locater_rotateY.obj").toExternalForm());
            Node rotationZ = Importer3D.load(ContentModel.class.getResource("Locater_rotateZ.obj").toExternalForm());
            Node scaleX = Importer3D.load(ContentModel.class.getResource("Locater_scaleX.obj").toExternalForm());
            Node scaleY = Importer3D.load(ContentModel.class.getResource("Locater_scaleY.obj").toExternalForm());
            Node scaleZ = Importer3D.load(ContentModel.class.getResource("Locater_scaleZ.obj").toExternalForm());
            moveGizmo.getChildren().addAll(moveX, moveY, moveZ);
            rotationGizmo.getChildren().addAll(rotationX, rotationY, rotationZ);
            scaleGizmo.getChildren().addAll(scaleX, scaleY, scaleZ);
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
        if (selectedBuilding != null) {
            setVisibleGizmo();
        }
    }
    
    public void selectBuilding(Node building) {
        BoundingBox bb = (BoundingBox) building.getBoundsInParent();
        System.out.println(building.getId() + " x:" + bb.getCenterX() + " y:" + bb.getCenterY() + " z:" + bb.getCenterZ());
        setTranslateX(bb.getCenterX());
        setTranslateY(bb.getCenterY());
        setTranslateZ(bb.getCenterZ());

        if (debugBoundingBox != null) {
            getChildren().remove(debugBoundingBox);
        }
        debugBoundingBox = createBoundingBoxVisual(bb);
        getChildren().add(debugBoundingBox);
        debugBoundingBox.setTranslateX(0.0d);
        debugBoundingBox.setTranslateY(0.0d);
        debugBoundingBox.setTranslateZ(0.0d);

        selectedBuilding = building;

        setVisibleGizmo();
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
        boundingBoxVisual.setMaterial(new PhongMaterial(new Color(1.0d,0.0d,0.0d,0.3d)));

        return boundingBoxVisual;
    }

}
