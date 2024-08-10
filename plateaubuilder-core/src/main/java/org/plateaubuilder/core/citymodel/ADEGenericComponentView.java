package org.plateaubuilder.core.citymodel;

import java.util.List;

import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.editor.Editor;

public class ADEGenericComponentView extends AbstractMultiSurfaceView<ADEGenericComponent> implements IFeatureView {

    public ADEGenericComponentView(ADEGenericComponent gml) {
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
