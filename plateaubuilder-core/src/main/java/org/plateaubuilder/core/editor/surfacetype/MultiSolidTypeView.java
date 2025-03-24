package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSolidView;

import javafx.scene.shape.MeshView;

public class MultiSolidTypeView extends MeshView {
    private int lod;
    private AbstractCityObject target;
    private ILODMultiSolidView multiSolid;

    public MultiSolidTypeView(int lod) {
        this.lod = lod;
    }

    public void setTarget(AbstractCityObject target, ILODMultiSolidView multiSolid) {
        this.target = target;
        this.multiSolid = multiSolid;
    }

    public void updateVisual() {
        // TODO: Implement this method
    }
}
