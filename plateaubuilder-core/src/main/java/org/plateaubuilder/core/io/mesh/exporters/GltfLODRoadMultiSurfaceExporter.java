package org.plateaubuilder.core.io.mesh.exporters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD2RoadMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD3RoadMultiSurfaceView;

import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;

/**
 * GltfLODMultiSurfaceExporterは、ILODMultiSurfaceViewをGltf形式でエクスポートするための機能を提供します。
 */
public class GltfLODRoadMultiSurfaceExporter extends AbstractGltfLODExporter<ILODMultiSurfaceView> {

    /**
     * Constructor
     * 
     * @param lodView      the {@link ILODMultiSurfaceView}
     * @param featureId    the feature id
     * @param exportOption the exportOption
     */
    public GltfLODRoadMultiSurfaceExporter(ILODMultiSurfaceView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    @Override
    protected DefaultSceneModel createSceneModel(ILODMultiSurfaceView lodView) {
        if (lodView instanceof LOD1MultiSurfaceView) {
            return createSceneModel((LOD1MultiSurfaceView) lodView);
        } else if (lodView instanceof LOD2RoadMultiSurfaceView) {
            return createSceneModel((LOD2RoadMultiSurfaceView) lodView);
        } else if (lodView instanceof LOD3RoadMultiSurfaceView) {
            return createSceneModel((LOD3RoadMultiSurfaceView) lodView);
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

    private DefaultSceneModel createSceneModel(LOD2RoadMultiSurfaceView roadLod2MultiSurface) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        {
            DefaultMeshModel meshModel = new DefaultMeshModel();
            meshModel.setName(getFeatureId());
            var multiSurfaceMeshView = roadLod2MultiSurface.getLOD2MultiSurfaceView();
            for (var polygon : multiSurfaceMeshView.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(multiSurfaceMeshView, polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }

            if (meshModel.getMeshPrimitiveModels().size() > 0) {
                nodeModel.addMeshModel(meshModel);
            }
        }

        for (var trafficArea : roadLod2MultiSurface.getTrafficAreas()) {
            DefaultMeshModel meshModel = new DefaultMeshModel();
            meshModel.setName(String.format("trafficArea_%s", trafficArea.getGMLID()));
            for (var polygon : trafficArea.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(trafficArea.getMultiSurfaceMeshView(), polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
            if (meshModel.getMeshPrimitiveModels().size() > 0) {
                DefaultNodeModel childNodeModel = new DefaultNodeModel();
                childNodeModel.addMeshModel(meshModel);
                nodeModel.addChild(childNodeModel);
            }
        }

        for (var auxiliaryTrafficArea : roadLod2MultiSurface.getAuxiliaryTrafficAreas()) {
            DefaultMeshModel meshModel = new DefaultMeshModel();
            meshModel.setName(String.format("auxiliaryTrafficArea_%s", auxiliaryTrafficArea.getGMLID()));
            for (var polygon : auxiliaryTrafficArea.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(auxiliaryTrafficArea.getMultiSurfaceMeshView(), polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
            if (meshModel.getMeshPrimitiveModels().size() > 0) {
                DefaultNodeModel childNodeModel = new DefaultNodeModel();
                childNodeModel.addMeshModel(meshModel);
                nodeModel.addChild(childNodeModel);
            }
        }

        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD3RoadMultiSurfaceView roadLod3MultiSurface) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        {
            DefaultMeshModel meshModel = new DefaultMeshModel();
            meshModel.setName(getFeatureId());
            var multiSurfaceMeshView = roadLod3MultiSurface.getLOD3MultiSurfaceView();
            for (var polygon : multiSurfaceMeshView.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(multiSurfaceMeshView, polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }

            if (meshModel.getMeshPrimitiveModels().size() > 0) {
                nodeModel.addMeshModel(meshModel);
            }
        }

        for (var trafficArea : roadLod3MultiSurface.getTrafficAreas()) {
            DefaultMeshModel meshModel = new DefaultMeshModel();
            meshModel.setName(String.format("trafficArea_%s", trafficArea.getGMLID()));
            for (var polygon : trafficArea.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(trafficArea.getMultiSurfaceMeshView(), polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
            if (meshModel.getMeshPrimitiveModels().size() > 0) {
                DefaultNodeModel childNodeModel = new DefaultNodeModel();
                childNodeModel.addMeshModel(meshModel);
                nodeModel.addChild(childNodeModel);
            }
        }

        for (var auxiliaryTrafficArea : roadLod3MultiSurface.getAuxiliaryTrafficAreas()) {
            DefaultMeshModel meshModel = new DefaultMeshModel();
            meshModel.setName(String.format("auxiliaryTrafficArea_%s", auxiliaryTrafficArea.getGMLID()));
            for (var polygon : auxiliaryTrafficArea.getPolygons()) {
                MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
                DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(auxiliaryTrafficArea.getMultiSurfaceMeshView(), polygon, materialModel);
                meshPrimitiveModel.setMaterialModel(materialModel);
                meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
            }
            if (meshModel.getMeshPrimitiveModels().size() > 0) {
                DefaultNodeModel childNodeModel = new DefaultNodeModel();
                childNodeModel.addMeshModel(meshModel);
                nodeModel.addChild(childNodeModel);
            }
        }

        sceneModel.addNode(nodeModel);

        return sceneModel;
    }
}
