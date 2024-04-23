package org.plateaubuilder.core.citymodel;

import javafx.scene.paint.Material;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SurfaceDataView extends GMLView<AbstractSurfaceData> {

    private final Map<String, float[]> textureCoordinatesByRing = new HashMap<>();

    private final Set<String> targetSet = new HashSet<>();

    private Material material;

    private SurfaceType surfaceType;

    public SurfaceDataView(AbstractSurfaceData original, SurfaceType surfaceType) {
        super(original);
        this.surfaceType = surfaceType;
    }

    public Map<String, float[]> getTextureCoordinatesByRing() {
        return this.textureCoordinatesByRing;
    }

    public Set<String> getTargetSet() {
        return this.targetSet;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public SurfaceType getSurfaceType() {
        return surfaceType;
    }
}
