package org.plateaubuilder.core.editor.surfacetype;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.vegetation.PlantCover;

public class PlantCoverModuleComponentManipulator extends AbstractModuleComponentManipulator<PlantCover> {
    public PlantCoverModuleComponentManipulator(PlantCover feature, int lod) {
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
            feature.unsetLod1MultiSolid();
            break;
        case 2:
            feature.unsetLod2MultiSolid();
            feature.unsetLod2MultiSurface();
            break;
        case 3:
            feature.unsetLod3MultiSolid();
            feature.unsetLod3MultiSurface();
            break;
        default:
            throw new OutOfRangeException(lod, 1, 3);
        }
    }
}
