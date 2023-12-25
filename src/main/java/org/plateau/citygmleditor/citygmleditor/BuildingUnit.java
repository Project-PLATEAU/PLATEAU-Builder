package org.plateau.citygmleditor.citygmleditor;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point3D;
import javafx.scene.Parent;

public class BuildingUnit extends Parent {
    private Point3D location;
    private Point3D rotation;
    private Point3D scale;

    private Point3D origin;

    public BuildingUnit() {
        this.location = new Point3D(0, 0, 0);
        this.rotation = new Point3D(0, 0, 0);
        this.scale = new Point3D(1, 1, 1);
    }

    public Point3D getLocation() {
        return location;
    }

    public void setLocation(Point3D locate) {
        this.location = locate;
    }
    
    public Point3D getRotation() {
        return rotation;
    }

    public void setRotation(Point3D rotate) {
        this.rotation = rotate;
    }
    
    public Point3D getScale() {
        return scale;
    }

    public void setScale(Point3D scale) {
        this.scale = scale;
    }

    public void updateOrigin() {
        BoundingBox bb = (BoundingBox) getBoundsInParent();
        origin = new Point3D(bb.getCenterX(), bb.getCenterY(), bb.getMinZ());
    }

    public Point3D getOrigin() {
        return origin;
    }
}
