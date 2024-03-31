package org.plateau.plateaubuilder.citymodel;

import javafx.scene.Parent;
import org.citygml4j.model.gml.base.AbstractGML;

public abstract class GMLView<T extends AbstractGML> extends Parent {
    private final T gml;

    public GMLView(T gml) {
        this.gml = gml;
    }

    public String getGMLID() {
        return gml.getId();
    }

    public T getGML() {
        return gml;
    }
}
