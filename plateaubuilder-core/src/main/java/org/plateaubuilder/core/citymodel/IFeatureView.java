package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.geometry.ILODView;

import javafx.scene.Node;

public interface IFeatureView {
    AbstractCityObject getGML();

    ILODView getLODView(int lod);

    Node getNode();

    void setVisible(boolean visible);
}
