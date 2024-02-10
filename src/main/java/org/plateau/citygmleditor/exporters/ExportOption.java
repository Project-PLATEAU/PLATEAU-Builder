package org.plateau.citygmleditor.exporters;

import org.plateau.citygmleditor.utils3d.geom.Vec3d;

public class ExportOption {
    private Vec3d offset;

    public ExportOption() {
    }

    public ExportOption(Vec3d offset) {
        this.offset = offset;
    }

    public Vec3d getOffset() {
        return offset;
    }
}
