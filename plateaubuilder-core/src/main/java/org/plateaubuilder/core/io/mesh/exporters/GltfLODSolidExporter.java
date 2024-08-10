package org.plateaubuilder.core.io.mesh.exporters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD1SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD3SolidView;

import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;

/**
 * GltfLODSolidExporterは、ILODSolidViewをGltf形式でエクスポートするための機能を提供します。
 */
public class GltfLODSolidExporter extends AbstractGltfLODExporter<ILODSolidView> {

    /**
     * Constructor
     * 
     * @param lodView      the {@link ILODSolidView}
     * @param featureId    the feature id
     * @param exportOption the exportOption
     */
    public GltfLODSolidExporter(ILODSolidView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    @Override
    protected DefaultSceneModel createSceneModel(ILODSolidView lodView) {
        if (lodView instanceof LOD1SolidView) {
            return createSceneModel((LOD1SolidView) lodView);
        } else if (lodView instanceof LOD2SolidView) {
            return createSceneModel((LOD2SolidView) lodView);
        } else if (lodView instanceof LOD3SolidView) {
            return createSceneModel((LOD3SolidView) lodView);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }

    private DefaultSceneModel createSceneModel(LOD1SolidView lod1Solid) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());
        var defaultMaterialModel = GetDefaultMaterialModel();
        for (var polygon : lod1Solid.getPolygons()) {
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod1Solid, polygon, defaultMaterialModel);
            meshPrimitiveModel.setMaterialModel(defaultMaterialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD2SolidView lod2Solid) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var boundary : lod2Solid.getBoundaries()) {
            for (var polygon : boundary.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod2Solid, polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD3SolidView lod3Solid) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var boundary : lod3Solid.getBoundaries()) {
            for (var polygon : boundary.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod3Solid, polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }
}
