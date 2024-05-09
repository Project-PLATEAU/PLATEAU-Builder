package org.plateaubuilder.core.editor.surfacetype;

import org.citygml4j.model.citygml.transportation.Road;

public class RoadModuleComponentManipulator {
    private final Road feature;
    private final int lod;

    public RoadModuleComponentManipulator(Road feature, int lod) {
        this.feature = feature;
        this.lod = lod;
    }

    public void clear() {

    }
}
