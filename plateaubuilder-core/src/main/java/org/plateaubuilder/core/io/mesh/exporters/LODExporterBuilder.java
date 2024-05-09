package org.plateaubuilder.core.io.mesh.exporters;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.io.mesh.FormatEnum;

public class LODExporterBuilder {
    private ILODView lodView;

    private String featureId;

    private ExportOption exportOption;

    private FormatEnum format;

    public LODExporterBuilder lodView(ILODView lodView) {
        this.lodView = lodView;
        return this;
    }

    public LODExporterBuilder featureId(String featureId) {
        this.featureId = featureId;
        return this;
    }

    public LODExporterBuilder exportOption(ExportOption exportOption) {
        this.exportOption = exportOption;
        return this;
    }

    public LODExporterBuilder format(FormatEnum format) {
        this.format = format;
        return this;
    }

    public AbstractLODExporter build() {
        if (format == FormatEnum.gLTF) {
            return buildGltfExporter();
        } else if (format == FormatEnum.OBJ) {
            return buildObjExporter();
        } else {
            throw new NotImplementedException(format + " is not supported.");
        }
    }

    private AbstractLODExporter buildGltfExporter() {
        if (lodView instanceof ILODSolidView) {
            return new GltfLODSolidExporter((ILODSolidView) lodView, featureId, exportOption);
        } else if (lodView instanceof ILODMultiSurfaceView) {
            return new GltfLODMultiSurfaceExporter((ILODMultiSurfaceView) lodView, featureId, exportOption);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }

    private AbstractLODExporter buildObjExporter() {
        if (lodView instanceof ILODSolidView) {
            return new ObjLODSolidExporter((ILODSolidView) lodView, featureId, exportOption);
        } else if (lodView instanceof ILODMultiSurfaceView) {
            return new ObjLODMultiSurfaceExporter((ILODMultiSurfaceView) lodView, featureId, exportOption);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }
}
