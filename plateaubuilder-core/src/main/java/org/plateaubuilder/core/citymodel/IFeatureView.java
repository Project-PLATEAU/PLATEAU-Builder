package org.plateaubuilder.core.citymodel;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.geometry.ILODView;

import javafx.scene.Node;

public interface IFeatureView {
    AbstractCityObject getGML();

    ILODView getLODView(int lod);

    void setLODView(int lod, ILODView lodView);

    Node getNode();

    void setVisible(boolean visible);

    default String getId() {
        return getNode().getId();
    }

    default Node getParent() {
        return getNode().getParent();
    }

    default CityModelView getCityModelView() {
        var parent = getNode().getParent();
        if (parent instanceof CityModelView) {
            return (CityModelView) parent;
        } else if (parent instanceof IFeatureView) {
            return ((IFeatureView) parent).getCityModelView();
        }

        return null;
    }

    // TODO: 暫定で追加した(ほかのGMLのプロパティによっては共通化する)
    default boolean isSetMeasuredHeight() {
        return false;
    }

    default Length getMeasuredHeight() {
        return null;
    }

    default void setMeasuredHeight(Length length) {
    }

    default void unsetMeasuredHeight() {
    }

    default List<String> getTexturePaths() {
        return new ArrayList<String>();
    }
}
