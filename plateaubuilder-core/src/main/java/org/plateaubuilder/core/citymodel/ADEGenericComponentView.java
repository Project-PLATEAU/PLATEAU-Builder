package org.plateaubuilder.core.citymodel;

import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.editor.Editor;

public class ADEGenericComponentView extends AbstractMultiSurfaceView<ADEGenericComponent> implements IFeatureView {

    public ADEGenericComponentView(ADEGenericComponent gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }

    public String getFeatureType() {
        return getGML().getNodeName();
    }

    @Override
    public List<String> getSupportedLODTypes() {
        return List.of("LOD1");
    }

    @Override
    public List<ADEComponent> getADEComponents() {
        return getGML().getGenericApplicationPropertyOfADEGenericComponent();
    }
}
