package org.plateau.citygmleditor.exporters;

import org.plateau.citygmleditor.utils3d.geom.Vec3d;

public class ExportOptionBuilder {
    private Vec3d offset;

    public ExportOptionBuilder() {
    }

    public ExportOptionBuilder offset(Vec3d offset) {
        this.offset = offset;
        return this;
    }

    public ExportOption build() {
        return new ExportOption(offset);
    }
}
