package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.plateaubuilder.core.editor.Editor;

public class CityFurnitureView extends AbstractGeometryView<CityFurniture> implements IFeatureView {
    public CityFurnitureView(CityFurniture gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }
}
