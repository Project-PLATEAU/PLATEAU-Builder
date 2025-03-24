package org.plateaubuilder.core.citymodel;

import javafx.scene.Group;
import javafx.scene.Node;
import org.citygml4j.model.gml.base.AbstractGML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CityModelGroup extends Group {
    private final List<CityModelHierarchyChangeListener> listeners = new ArrayList<>();
    private final GMLViewTable table = new GMLViewTable();

    public Node findView(AbstractGML gml) {
        return table.get(gml);
    }

    public <T extends AbstractGML> void registerView(ManagedGMLView<T> view) {
        table.register(view);
    }

    public <T extends AbstractGML> void removeView(ManagedGMLView<T> view) {
        table.remove(view.getGML());
    }

    public Collection<Node> getAllFeatures() {
        return table.getAll();
    }

    public void addCityModel(CityModelView cityModelView) {
        getChildren().add(cityModelView);
        registerView(cityModelView);
        fireChangeEvent();
    }

    public void removeCityModel(CityModelView cityModelView) {
        getChildren().remove(cityModelView);
        removeView(cityModelView);
        fireChangeEvent();
    }

    public void addChangeListener(CityModelHierarchyChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(CityModelHierarchyChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireChangeEvent() {
        for (var listener : listeners) {
            listener.onChange();
        }
    }
}
