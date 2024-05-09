package org.plateaubuilder.core.io.mesh.exporters;

import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;

/*
 * ObjLODMultiSurfaceExporterは、ILODMultiSurfaceViewをObj形式でエクスポートするための機能を提供します。
 */
public class ObjLODMultiSurfaceExporter extends AbstractObjLODExporter<ILODMultiSurfaceView> {

    public ObjLODMultiSurfaceExporter(ILODMultiSurfaceView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }
}
