package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.transportation.Road;
import org.plateaubuilder.core.editor.Editor;

public class RoadView extends AbstractMultiSurfaceView<Road> implements IFeatureView {
    public RoadView(Road gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }
}
