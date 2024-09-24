package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateaubuilder.core.citymodel.IFeatureView;

/**
 * ツリー構造のRootを示すためのクラス
 */
public class RootAttributeHandler extends AttributeHandler {
    private IFeatureView building;

    public RootAttributeHandler(IFeatureView building) {
        super(building, "root");
        this.building = building;
    }

    @Override
    public AbstractBuilding getContent() {
        return (AbstractBuilding) this.building.getGML();
    }
}