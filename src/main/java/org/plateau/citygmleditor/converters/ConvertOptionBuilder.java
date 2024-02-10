package org.plateau.citygmleditor.converters;

import org.plateau.citygmleditor.utils3d.geom.Vec3d;

public class ConvertOptionBuilder {
    private double wallThreshold = 80;

    private boolean useGeoReference = false;

    private Vec3d offset;

    public ConvertOptionBuilder() {
    }

    public ConvertOptionBuilder wallThreshold(double wallThreshold) {
        this.wallThreshold = wallThreshold;
        return this;
    }

    public ConvertOptionBuilder useGeoReference(boolean useGeoReference) {
        this.useGeoReference = useGeoReference;
        return this;
    }

    public ConvertOptionBuilder offset(Vec3d offset) {
        this.offset = offset;
        return this;
    }

    public ConvertOption build() {
        return new ConvertOption(wallThreshold, useGeoReference, offset);
    }
}
