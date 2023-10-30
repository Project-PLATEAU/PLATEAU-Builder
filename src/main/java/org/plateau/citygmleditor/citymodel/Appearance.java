package org.plateau.citygmleditor.citymodel;

import javafx.scene.paint.Material;

import java.util.ArrayList;
import java.util.HashMap;

public class Appearance extends GMLObject<org.citygml4j.model.citygml.appearance.Appearance> {
    private ArrayList<SurfaceData> surfaceData = new ArrayList<>();

    public Appearance(org.citygml4j.model.citygml.appearance.Appearance original) {
        super(original);
    }

    public ArrayList<SurfaceData> getSurfaceData() {
        return surfaceData;
    }

    public void setSurfaceData(ArrayList<SurfaceData> surfaceData) {
        this.surfaceData = surfaceData;
    }
}
