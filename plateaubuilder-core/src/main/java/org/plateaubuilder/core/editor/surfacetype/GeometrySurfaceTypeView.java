package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.geometry.ILODGeometryView;

import javafx.scene.shape.MeshView;

public class GeometrySurfaceTypeView extends MeshView {
    private int lod;
    private AbstractCityObject target;
    private ILODGeometryView geometry;

    public GeometrySurfaceTypeView(int lod) {
        this.lod = lod;
    }

    public void setTarget(AbstractCityObject target, ILODGeometryView geometry) {
        this.target = target;
        this.geometry = geometry;
    }

    public void updateVisual() {
        // TODO: Implement this method
    }
}
