package org.plateau.plateaubuilder.citymodel;

import javafx.scene.Parent;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.plateau.plateaubuilder.citymodel.geometry.GeometryView;

public class BuildingInstallationView extends Parent {
    private BuildingInstallation gmlObject;
    private GeometryView lod2GeometryView;
    private GeometryView lod3GeometryView;

    public BuildingInstallationView(BuildingInstallation gmlObject) {
        this.gmlObject = gmlObject;
    }

    public BuildingInstallation getGMLObject() {
        return this.gmlObject;
    }

    public void setGeometryView(int lod, GeometryView geometryView) {
        switch (lod) {
            case 2:
                lod2GeometryView = geometryView;
                this.getChildren().add(geometryView);
                break;
            case 3:
                lod3GeometryView = geometryView;
                this.getChildren().add(geometryView);
                break;
        }
    }

    public GeometryView getGeometryView(int lod) {
        switch (lod) {
            case 2:
                return lod2GeometryView;
            case 3:
                return lod3GeometryView;
        }
        return null;
    }
}
