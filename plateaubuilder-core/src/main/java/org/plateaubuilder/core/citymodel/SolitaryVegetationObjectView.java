package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.plateaubuilder.core.editor.Editor;

public class SolitaryVegetationObjectView extends AbstractGeometryView<SolitaryVegetationObject> implements IFeatureView {
    public SolitaryVegetationObjectView(SolitaryVegetationObject gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }
}
