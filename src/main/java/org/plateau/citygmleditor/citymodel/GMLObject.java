package org.plateau.citygmleditor.citymodel;

import org.citygml4j.model.gml.base.AbstractGML;

public class GMLObject<T extends AbstractGML> {
    private final T original;

    private final String gmlID;

    public GMLObject(T original) {
        this.original = original;
        this.gmlID = original.getId();
    }

    public T getOriginal() {
        return this.original;
    }

    public String getGMLID() {
        return gmlID;
    }
}
