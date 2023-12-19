package org.plateau.citygmleditor.citymodel;

import javafx.scene.Parent;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateau.citygmleditor.citymodel.factory.CityGMLFactory;
import org.citygml4j.model.gml.geometry.primitives.*;
import org.plateau.citygmleditor.citymodel.geometry.LOD1Solid;
import org.plateau.citygmleditor.citymodel.geometry.LOD2Solid;
import org.plateau.citygmleditor.citymodel.geometry.LOD3Solid;

public class Building extends Parent {
    private AbstractBuilding gmlObject;

    private LOD1Solid lod1Solid;
    private LOD2Solid lod2Solid;
    private LOD3Solid lod3Solid;
    private BuildingInstallationView buildingInstallationView;

    public Building(AbstractBuilding gmlObject) {
        this.gmlObject = gmlObject;
    }

    public AbstractBuilding getGMLObject() {
        return this.gmlObject;
    }

    public void setLOD1Solid(LOD1Solid solid) {
        if (this.lod1Solid == null) {
            this.getChildren().remove(this.lod1Solid);
        }
        this.lod1Solid = solid;
        this.getChildren().add(solid);
    }

    public LOD1Solid getLOD1Solid() {
        return this.lod1Solid;
    }

    public void setLOD2Solid(LOD2Solid solid) {
        if (solid == null)
            return;

        if (this.lod2Solid == null) {
            this.getChildren().remove(this.lod2Solid);
        }
        this.lod2Solid = solid;
        this.getChildren().add(solid);
    }

    public LOD2Solid getLOD2Solid() {
        return this.lod2Solid;
    }

    public void setLOD3Solid(LOD3Solid solid) {
        if (solid == null)
            return;

        if (this.lod3Solid == null) {
            this.getChildren().remove(this.lod3Solid);
        }
        this.lod3Solid = solid;
        this.getChildren().add(solid);
    }

    public LOD3Solid getLOD3Solid() {
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
}
