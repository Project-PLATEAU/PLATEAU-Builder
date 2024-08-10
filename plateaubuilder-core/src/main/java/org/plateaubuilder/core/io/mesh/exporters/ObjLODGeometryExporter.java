package org.plateaubuilder.core.io.mesh.exporters;

import org.plateaubuilder.core.citymodel.geometry.ILODGeometryView;

/**
 * ObjLODGeometryExporterは、ILODGeometryViewをObj形式でエクスポートするための機能を提供します。
 */
public class ObjLODGeometryExporter extends AbstractObjLODExporter<ILODGeometryView> {
    public ObjLODGeometryExporter(ILODGeometryView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }
}
