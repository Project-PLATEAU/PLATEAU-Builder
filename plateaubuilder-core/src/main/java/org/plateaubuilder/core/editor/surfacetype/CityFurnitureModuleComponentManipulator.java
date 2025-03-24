package org.plateaubuilder.core.editor.surfacetype;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;

public class CityFurnitureModuleComponentManipulator extends AbstractModuleComponentManipulator<CityFurniture> {
    public CityFurnitureModuleComponentManipulator(CityFurniture feature, int lod) {
        super(feature, lod);
    }

    public void clear() {
        unsetMultiSurface();
    }

    public void unsetMultiSurface() {
        var feature = getFeature();
        var lod = getLod();
        switch (lod) {
        case 1:
            feature.unsetLod1Geometry();
            break;
        case 2:
            feature.unsetLod2Geometry();
            break;
        case 3:
            feature.unsetLod3Geometry();
            break;
        default:
            throw new OutOfRangeException(lod, 1, 3);
        }
    }
}
