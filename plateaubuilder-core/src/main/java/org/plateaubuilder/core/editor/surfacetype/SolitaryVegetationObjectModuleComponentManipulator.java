package org.plateaubuilder.core.editor.surfacetype;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;

public class SolitaryVegetationObjectModuleComponentManipulator extends AbstractModuleComponentManipulator<SolitaryVegetationObject> {
    public SolitaryVegetationObjectModuleComponentManipulator(SolitaryVegetationObject feature, int lod) {
        super(feature, lod);
    }

    public void clear() {
        unsetMultiSurface();
    }

    private void unsetMultiSurface() {
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
