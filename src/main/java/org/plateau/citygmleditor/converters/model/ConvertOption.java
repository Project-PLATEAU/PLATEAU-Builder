package org.plateau.citygmleditor.converters.model;

import org.plateau.citygmleditor.utils3d.geom.Vec3d;

public class ConvertOption {
    private Vec3d offset;

    public ConvertOption(Vec3d offset) {
        this.offset = offset;
    }

    public Vec3d getOffset() {
        return offset;
    }
}
