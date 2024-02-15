package org.plateau.citygmleditor.validation;

import javafx.geometry.Point3D;

import java.util.List;

public class Solid {
    String buildingPartID;
    List<List<Point3D>> faces;

    public String getBuildingPartID() {
        return buildingPartID;
    }

    public void setBuildingPartID(String buildingPartID) {
        this.buildingPartID = buildingPartID;
    }

    public List<List<Point3D>> getFaces() {
        return faces;
    }

    public void setFaces(List<List<Point3D>> faces) {
        this.faces = faces;
    }
}
