package org.plateaubuilder.core.io.mesh.exporters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSolidView;

import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;

/**
 * GltfLODMultiSolidExporterは、ILODMultiSolidViewをGltf形式でエクスポートするための機能を提供します。
 */
public class GltfLODMultiSolidExporter extends AbstractGltfLODExporter<ILODMultiSolidView> {

    /**
     * Constructor
     * 
     * @param lodView      the {@link ILODMultiSolidView}
     * @param featureId    the feature id
     * @param exportOption the exportOption
     */
    public GltfLODMultiSolidExporter(ILODMultiSolidView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    @Override
    protected DefaultSceneModel createSceneModel(ILODMultiSolidView lodView) {
        if (lodView instanceof LOD1MultiSolidView) {
            return createSceneModel((LOD1MultiSolidView) lodView);
        } else if (lodView instanceof LOD2MultiSolidView) {
            return createSceneModel((LOD2MultiSolidView) lodView);
        } else if (lodView instanceof LOD3MultiSolidView) {
            return createSceneModel((LOD3MultiSolidView) lodView);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }

    private DefaultSceneModel createSceneModel(LOD1MultiSolidView lod1MultiSolid) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());
        var defaultMaterialModel = GetDefaultMaterialModel();
        for (var polygon : lod1MultiSolid.getPolygons()) {
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod1MultiSolid, polygon, defaultMaterialModel);
            meshPrimitiveModel.setMaterialModel(defaultMaterialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD2MultiSolidView lod2MultiSolid) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var polygon : lod2MultiSolid.getPolygons()) {
            MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod2MultiSolid, polygon, materialModel);
            meshPrimitiveModel.setMaterialModel(materialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD3MultiSolidView lod3MultiSolid) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var polygon : lod3MultiSolid.getPolygons()) {
            MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod3MultiSolid, polygon, materialModel);
            meshPrimitiveModel.setMaterialModel(materialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }
}
