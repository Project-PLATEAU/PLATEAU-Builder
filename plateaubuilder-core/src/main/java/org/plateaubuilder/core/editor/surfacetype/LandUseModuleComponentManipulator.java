package org.plateaubuilder.core.editor.surfacetype;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.landuse.LandUse;

public class LandUseModuleComponentManipulator extends AbstractModuleComponentManipulator<LandUse> {
    public LandUseModuleComponentManipulator(LandUse feature, int lod) {
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
            feature.unsetLod1MultiSurface();
            break;
        default:
            throw new OutOfRangeException(lod, 1, 1);
        }
    }
}
