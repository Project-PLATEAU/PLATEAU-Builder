package org.plateaubuilder.core.io.mesh.exporters;

import org.plateaubuilder.core.citymodel.geometry.ILODMultiSolidView;

/**
 * ObjLODMultiSolidExporterは、ILODMultiSolidViewをObj形式でエクスポートするための機能を提供します。
 */
public class ObjLODMultiSolidExporter extends AbstractObjLODExporter<ILODMultiSolidView> {
    public ObjLODMultiSolidExporter(ILODMultiSolidView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }
}
