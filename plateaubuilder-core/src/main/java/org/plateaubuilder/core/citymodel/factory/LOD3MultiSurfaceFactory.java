package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.transportation.Road;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSurfaceView;

public class LOD3MultiSurfaceFactory extends GeometryFactory {

    protected LOD3MultiSurfaceFactory(CityModelView target) {
        super(target);
    }

    public LOD3MultiSurfaceView createLOD3MultiSurface(Road gmlObject) {
        return null;
    }
}
