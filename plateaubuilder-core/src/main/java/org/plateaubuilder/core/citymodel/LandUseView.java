package org.plateaubuilder.core.citymodel;

import java.util.List;

import org.citygml4j.model.citygml.landuse.LandUse;
import org.plateaubuilder.core.editor.Editor;

public class LandUseView extends AbstractMultiSurfaceView<LandUse> implements IFeatureView {
    public LandUseView(LandUse gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }

    @Override
    public List<String> getSupportedLODTypes() {
        return List.of("LOD1");
    }
}
