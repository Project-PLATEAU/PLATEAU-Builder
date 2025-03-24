package org.plateaubuilder.core.io.mesh.exporters;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD2RoadMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD3RoadMultiSurfaceView;

/*
 * ObjLODMultiSurfaceExporterは、ILODMultiSurfaceViewをObj形式でエクスポートするための機能を提供します。
 */
public class ObjLODRoadMultiSurfaceExporter extends AbstractObjLODExporter<ILODMultiSurfaceView> {

    public ObjLODRoadMultiSurfaceExporter(ILODMultiSurfaceView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    @Override
    protected List<ObjectModel> createObjectModels(ILODMultiSurfaceView lodView) {
        if (lodView instanceof LOD1MultiSurfaceView) {
            return createObjectModels((LOD1MultiSurfaceView) lodView);
        } else if (lodView instanceof LOD2RoadMultiSurfaceView) {
            return createObjectModels((LOD2RoadMultiSurfaceView) lodView);
        } else if (lodView instanceof LOD3RoadMultiSurfaceView) {
            return createObjectModels((LOD3RoadMultiSurfaceView) lodView);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }

    private List<ObjectModel> createObjectModels(LOD1MultiSurfaceView lod1MultiSurface) {
        return super.createObjectModels(lod1MultiSurface);
    }

    private List<ObjectModel> createObjectModels(LOD2RoadMultiSurfaceView roadLod2MultiSurface) {
        List<ObjectModel> objectModels = new ArrayList<>();

        var vertexOffset = 0;
        var uvOffset = 0;

        var multiSurfaceMeshView = roadLod2MultiSurface.getLOD2MultiSurfaceView();
        var objectModel = createObjectModel(multiSurfaceMeshView);
        if (objectModel != null) {
            objectModels.add(objectModel);
            vertexOffset += objectModel.getVertexCount();
            uvOffset += objectModel.getUVCount();
        }

        for (var trafficArea : roadLod2MultiSurface.getTrafficAreas()) {
            var childObjectModel = createObjectModel(trafficArea, String.format("trafficArea_%s", trafficArea.getGMLID()), vertexOffset, uvOffset);
            if (childObjectModel != null) {
                objectModels.add(childObjectModel);
                vertexOffset += childObjectModel.getVertexCount();
                uvOffset += childObjectModel.getUVCount();
            }
        }

        for (var auxiliaryTrafficArea : roadLod2MultiSurface.getAuxiliaryTrafficAreas()) {
            var childObjectModel = createObjectModel(auxiliaryTrafficArea, String.format("auxiliaryTrafficArea_%s", auxiliaryTrafficArea.getGMLID()),
                    vertexOffset, uvOffset);
            if (childObjectModel != null) {
                objectModels.add(childObjectModel);
                vertexOffset += childObjectModel.getVertexCount();
                uvOffset += childObjectModel.getUVCount();
            }
        }

        return objectModels;
    }

    private List<ObjectModel> createObjectModels(LOD3RoadMultiSurfaceView roadLod3MultiSurface) {
        List<ObjectModel> objectModels = new ArrayList<>();

        var vertexOffset = 0;
        var uvOffset = 0;

        var multiSurfaceMeshView = roadLod3MultiSurface.getLOD3MultiSurfaceView();
        var objectModel = createObjectModel(multiSurfaceMeshView);
        if (objectModel != null) {
            objectModels.add(objectModel);
            vertexOffset += objectModel.getVertexCount();
            uvOffset += objectModel.getUVCount();
        }

        for (var trafficArea : roadLod3MultiSurface.getTrafficAreas()) {
            var childObjectModel = createObjectModel(trafficArea, String.format("trafficArea_%s", trafficArea.getGMLID()), vertexOffset, uvOffset);
            if (childObjectModel != null) {
                objectModels.add(childObjectModel);
                vertexOffset += childObjectModel.getVertexCount();
                uvOffset += childObjectModel.getUVCount();
            }
        }

        for (var auxiliaryTrafficArea : roadLod3MultiSurface.getAuxiliaryTrafficAreas()) {
            var childObjectModel = createObjectModel(auxiliaryTrafficArea, String.format("auxiliaryTrafficArea_%s", auxiliaryTrafficArea.getGMLID()),
                    vertexOffset, uvOffset);
            if (childObjectModel != null) {
                objectModels.add(childObjectModel);
                vertexOffset += childObjectModel.getVertexCount();
                uvOffset += childObjectModel.getUVCount();
            }
        }

        return objectModels;
    }
}
