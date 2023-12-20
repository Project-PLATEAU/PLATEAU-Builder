package org.plateau.citygmleditor.citymodel;

import javafx.scene.Parent;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.*;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD3SolidView;

public class BuildingView extends Parent {
    private AbstractBuilding gmlObject;

    private LOD1SolidView lod1Solid;
    private LOD2SolidView lod2Solid;
    private LOD3SolidView lod3Solid;
    private BuildingInstallationView buildingInstallationView;

    public BuildingView(AbstractBuilding gmlObject) {
        this.gmlObject = gmlObject;
    }

    public AbstractBuilding getGMLObject() {
        return this.gmlObject;
    }

    public void setLOD1Solid(LOD1SolidView solid) {
        if (this.lod1Solid == null) {
            this.getChildren().remove(this.lod1Solid);
        }
        this.lod1Solid = solid;
        this.getChildren().add(solid);
    }

    public LOD1SolidView getLOD1Solid() {
        return this.lod1Solid;
    }

    public void setLOD2Solid(LOD2SolidView solid) {
        if (solid == null)
            return;

        if (this.lod2Solid == null) {
            this.getChildren().remove(this.lod2Solid);
        }
        this.lod2Solid = solid;
        this.getChildren().add(solid);
    }

    public LOD2SolidView getLOD2Solid() {
        return this.lod2Solid;
    }

    public void setLOD3Solid(LOD3SolidView solid) {
        if (solid == null)
            return;

        if (this.lod3Solid == null) {
            this.getChildren().remove(this.lod3Solid);
        }
        this.lod3Solid = solid;
        this.getChildren().add(solid);
    }

    public LOD3SolidView getLOD3Solid() {
        return this.lod3Solid;
    }

    public void setBuildingInstallationView(BuildingInstallationView buildingInstallationView) {
        if(buildingInstallationView == null)
            return;
        if(this.buildingInstallationView == null){
            this.getChildren().remove(this.buildingInstallationView);
        }
        this.buildingInstallationView = buildingInstallationView;
        this.getChildren().add(buildingInstallationView);
    }

    public Envelope getEnvelope() {
        return this.gmlObject.getBoundedBy().getEnvelope();
    }
}
