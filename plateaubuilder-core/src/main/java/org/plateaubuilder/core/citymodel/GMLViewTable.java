package org.plateaubuilder.core.citymodel;

import javafx.scene.Node;
import org.citygml4j.model.gml.base.AbstractGML;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GMLViewTable {
    private final Map<AbstractGML, Node> map = new HashMap<>();

    public <T extends AbstractGML> void register(ManagedGMLView<T> view) {
        map.put(view.getGML(), view);
    }

    public Node get(AbstractGML gml) {
        return map.get(gml);
    }

    public void remove(AbstractGML gml) {
        map.remove(gml);
    }

    public Collection<Node> getAll() {
        return map.values();
    }
}
