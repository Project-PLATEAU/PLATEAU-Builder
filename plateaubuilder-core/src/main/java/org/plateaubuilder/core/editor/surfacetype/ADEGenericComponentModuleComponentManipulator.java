package org.plateaubuilder.core.editor.surfacetype;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;

public class ADEGenericComponentModuleComponentManipulator extends AbstractModuleComponentManipulator<ADEGenericComponent> {

    public ADEGenericComponentModuleComponentManipulator(ADEGenericComponent feature, int lod) {
        super(feature, lod);
    }

    @Override
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
