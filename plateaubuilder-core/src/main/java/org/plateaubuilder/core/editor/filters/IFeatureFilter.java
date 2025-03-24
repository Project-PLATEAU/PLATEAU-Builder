package org.plateaubuilder.core.editor.filters;

import org.plateaubuilder.core.citymodel.IFeatureView;

public interface IFeatureFilter {
    boolean evaluate(IFeatureView feature);
}
