package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.transportation.Road;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;

import javafx.scene.shape.MeshView;

public class TrafficAreaSurfaceTypeView extends MeshView {
    private int lod;
    private Road road;
    private ILODMultiSurfaceView multiSurface;

    public TrafficAreaSurfaceTypeView(int lod) {
        this.lod = lod;
    }

    public void setTarget(Road road, ILODMultiSurfaceView multiSurface) {
        this.road = road;
        this.multiSurface = multiSurface;
    }

    public void updateVisual() {
        // TODO: Implement this method
    }
}
