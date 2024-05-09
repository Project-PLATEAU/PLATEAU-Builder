package org.plateaubuilder.core.io.mesh.exporters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSurfaceView;

import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;

/**
 * GltfLODMultiSurfaceExporterは、LODMultiSurfaceViewをGltf形式でエクスポートするための機能を提供します。
 */
public class GltfLODMultiSurfaceExporter extends AbstractGltfLODExporter<ILODMultiSurfaceView> {

    /**
     * Constructor
     * 
     * @param lodView      the {@link ILODMultiSurfaceView}
     * @param featureId    the feature id
     * @param exportOption the exportOption
     */
    public GltfLODMultiSurfaceExporter(ILODMultiSurfaceView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    @Override
    protected DefaultSceneModel createSceneModel(ILODMultiSurfaceView lodView) {
        if (lodView instanceof LOD1MultiSurfaceView) {
            return createSceneModel((LOD1MultiSurfaceView) lodView);
        } else if (lodView instanceof LOD2MultiSurfaceView) {
            return createSceneModel((LOD2MultiSurfaceView) lodView);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }

    private DefaultSceneModel createSceneModel(LOD1MultiSurfaceView lod1MultiSurface) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());
        var defaultMaterialModel = GetDefaultMaterialModel();
        for (var polygon : lod1MultiSurface.getPolygons()) {
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod1MultiSurface, polygon, defaultMaterialModel);
            meshPrimitiveModel.setMaterialModel(defaultMaterialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD2MultiSurfaceView lod2MultiSurface) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var polygon : lod2MultiSurface.getPolygons()) {
            MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod2MultiSurface, polygon, materialModel);
            meshPrimitiveModel.setMaterialModel(materialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }
}
