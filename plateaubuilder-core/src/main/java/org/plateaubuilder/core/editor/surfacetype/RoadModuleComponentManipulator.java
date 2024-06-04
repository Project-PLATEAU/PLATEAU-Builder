package org.plateaubuilder.core.editor.surfacetype;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;

public class RoadModuleComponentManipulator {
    private final Road feature;
    private final int lod;

    public RoadModuleComponentManipulator(Road feature, int lod) {
        this.feature = feature;
        this.lod = lod;
    }

    public void clear() {
        List<AbstractSurface> surfaces = getSurfaces(getMultiSurface());
        for (var trafficArea : feature.getTrafficArea()) {
            var multiSurfaceProperty = getMultiSurface(trafficArea);
            if (multiSurfaceProperty == null) {
                continue;
            }
            surfaces.addAll(getSurfaces(multiSurfaceProperty));
        }
        for (var auxiliaryTrafficArea : feature.getAuxiliaryTrafficArea()) {
            var multiSurfaceProperty = getMultiSurface(auxiliaryTrafficArea);
            if (multiSurfaceProperty == null) {
                continue;
            }
            surfaces.addAll(getSurfaces(multiSurfaceProperty));
        }
        unsetMultiSurface();
    }

    private List<AbstractSurface> getSurfaces(MultiSurfaceProperty multiSurfaceProperty) {
        if (multiSurfaceProperty == null || multiSurfaceProperty.getMultiSurface() == null)
            return new ArrayList<>();

        var multiSurface = (MultiSurface) multiSurfaceProperty.getMultiSurface();

        return multiSurface.getSurfaceMember().stream().map(surfaceProperty -> surfaceProperty.getSurface()).collect(Collectors.toList());
    }

    private MultiSurfaceProperty getMultiSurface() {
        switch (lod) {
        case 1:
            return feature.getLod1MultiSurface();
        case 2:
            return feature.getLod2MultiSurface();
        case 3:
            return feature.getLod3MultiSurface();
        default:
            throw new OutOfRangeException(lod, 1, 3);
        }
    }

    private MultiSurfaceProperty getMultiSurface(TrafficAreaProperty trafficAreaProperty) {
        if (trafficAreaProperty == null || trafficAreaProperty.getTrafficArea() == null) {
            return null;
        }
        switch (lod) {
        case 1:
            return null;
        case 2:
            return trafficAreaProperty.getTrafficArea().getLod2MultiSurface();
        case 3:
            return trafficAreaProperty.getTrafficArea().getLod3MultiSurface();
        default:
            throw new OutOfRangeException(lod, 1, 3);
        }
    }

    private MultiSurfaceProperty getMultiSurface(AuxiliaryTrafficAreaProperty auxiliaryTrafficAreaProperty) {
        if (auxiliaryTrafficAreaProperty == null || auxiliaryTrafficAreaProperty.getAuxiliaryTrafficArea() == null) {
            return null;
        }
        switch (lod) {
        case 1:
            return null;
        case 2:
            return auxiliaryTrafficAreaProperty.getAuxiliaryTrafficArea().getLod2MultiSurface();
        case 3:
            return auxiliaryTrafficAreaProperty.getAuxiliaryTrafficArea().getLod3MultiSurface();
        default:
            throw new OutOfRangeException(lod, 1, 3);
        }
    }

    private void unsetMultiSurface() {
        switch (lod) {
        case 1:
            feature.unsetLod1MultiSurface();
            break;
        case 2:
            feature.unsetLod2MultiSurface();
            for (var trafficArea : feature.getTrafficArea()) {
                trafficArea.getTrafficArea().unsetLod2MultiSurface();
            }
            for (var auxiliaryTrafficArea : feature.getAuxiliaryTrafficArea()) {
                auxiliaryTrafficArea.getAuxiliaryTrafficArea().unsetLod2MultiSurface();
            }
            break;
        case 3:
            feature.unsetLod3MultiSurface();
            for (var trafficArea : feature.getTrafficArea()) {
                trafficArea.getTrafficArea().unsetLod3MultiSurface();
            }
            for (var auxiliaryTrafficArea : feature.getAuxiliaryTrafficArea()) {
                auxiliaryTrafficArea.getAuxiliaryTrafficArea().unsetLod3MultiSurface();
            }
            break;
        default:
            throw new OutOfRangeException(lod, 1, 3);
        }
    }
}
