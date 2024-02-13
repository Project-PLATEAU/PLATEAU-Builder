package org.plateau.citygmleditor.converters;

import org.plateau.citygmleditor.utils3d.geom.Vec3d;

public class ConvertOption {
    public static final double DEFAULT_WALL_THRESHOLD = 80;

    private double wallThreshold;

    private boolean useGeoReference;

    private Vec3d offset;

    public ConvertOption() {
    }

    public ConvertOption(double wallThreshold, boolean useGeoReference, Vec3d offset) {
        this.wallThreshold = wallThreshold;
        this.useGeoReference = useGeoReference;
        this.offset = offset;
    }

    public double getWallThreshold() {
        return wallThreshold;
    }

    public boolean isUseGeoReference() {
        return useGeoReference;
    }

    public Vec3d getOffset() {
        return offset;
    }
}
