package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.IFeatureView;

/**
 * ツリー構造のRootを示すためのクラス
 */
public class RootAttributeHandler extends AttributeHandler {
    private IFeatureView featureView;

    public RootAttributeHandler(IFeatureView featureView) {
        super(featureView, "root");
        this.featureView = featureView;
    }

    @Override
    public AbstractCityObject getContent() {
        return this.featureView.getGML();
    }
}