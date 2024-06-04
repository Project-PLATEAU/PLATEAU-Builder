package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;

import javafx.scene.shape.MeshView;

public class MultiSurfaceTypeView extends MeshView {
    private int lod;
    private AbstractCityObject target;
    private ILODMultiSurfaceView multiSurface;

    public MultiSurfaceTypeView(int lod) {
        this.lod = lod;
    }

    public void setTarget(AbstractCityObject target, ILODMultiSurfaceView multiSurface) {
        this.target = target;
        this.multiSurface = multiSurface;
    }

    public void updateVisual() {
        // TODO: Implement this method
    }
}
