package org.plateau.plateaubuilder.validation.invalid;

import java.util.List;

public class BuildlingError {
    String buildingID;
    List<String> formatPolygons;

    public String getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(String buildingID) {
        this.buildingID = buildingID;
    }

    public List<String> getPolygons() {
        return formatPolygons;
    }

    public void setPolygons(List<String> polygons) {
        this.formatPolygons = polygons;
    }
}
