package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.core.AbstractCityObject;

abstract public class AbstractModuleComponentManipulator<T extends AbstractCityObject> {
    private final T feature;
    private final int lod;

    public AbstractModuleComponentManipulator(T feature, int lod) {
        this.feature = feature;
        this.lod = lod;
    }

    public T getFeature() {
        return feature;
    }

    public int getLod() {
        return lod;
    }

    abstract public void clear();
}
