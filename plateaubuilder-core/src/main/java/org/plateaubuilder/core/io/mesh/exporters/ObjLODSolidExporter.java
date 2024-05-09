package org.plateaubuilder.core.io.mesh.exporters;

import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;

/**
 * ObjLODSolidExporterは、ILODSolidViewをObj形式でエクスポートするための機能を提供します。
 */
public class ObjLODSolidExporter extends AbstractObjLODExporter<ILODSolidView> {
    public ObjLODSolidExporter(ILODSolidView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }
}
