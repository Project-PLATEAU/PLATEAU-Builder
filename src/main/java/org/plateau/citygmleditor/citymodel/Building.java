package org.plateau.citygmleditor.citymodel;

import javafx.scene.Parent;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateau.citygmleditor.citymodel.factory.CityGMLFactory;
import org.citygml4j.model.gml.geometry.primitives.*;
import org.plateau.citygmleditor.citymodel.geometry.LOD1Solid;
import org.plateau.citygmleditor.citymodel.geometry.LOD2Solid;

public class Building extends Parent {
    private AbstractBuilding gmlObject;

    private LOD1Solid lod1Solid;
    private LOD2Solid lod2Solid;

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
}
