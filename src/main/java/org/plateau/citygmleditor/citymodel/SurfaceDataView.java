package org.plateau.citygmleditor.citymodel;

import javafx.scene.paint.Material;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;

import java.util.HashMap;

public class SurfaceDataView extends GMLObjectView<AbstractSurfaceData> {

    private final HashMap<String, float[]> textureCoordinatesByRing = new HashMap<>();

    private Material material;

    public SurfaceDataView(AbstractSurfaceData original) {
        super(original);
    }

    public HashMap<String, float[]> getTextureCoordinatesByRing() {
        return this.textureCoordinatesByRing;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
