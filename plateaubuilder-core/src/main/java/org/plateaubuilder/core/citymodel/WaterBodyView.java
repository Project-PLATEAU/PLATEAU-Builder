package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.editor.Editor;

public class WaterBodyView extends AbstractMultiSurfaceView<WaterBody> implements IFeatureView {
    public WaterBodyView(WaterBody gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }
}
