package org.plateaubuilder.core.io.mesh.exporters;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.geometry.ILODGeometryView;
import org.plateaubuilder.core.citymodel.geometry.LOD1GeometryView;
import org.plateaubuilder.core.citymodel.geometry.LOD2GeometryView;
import org.plateaubuilder.core.citymodel.geometry.LOD3GeometryView;

import de.javagl.jgltf.model.impl.DefaultMeshModel;
import de.javagl.jgltf.model.impl.DefaultMeshPrimitiveModel;
import de.javagl.jgltf.model.impl.DefaultNodeModel;
import de.javagl.jgltf.model.impl.DefaultSceneModel;
import de.javagl.jgltf.model.v2.MaterialModelV2;

/**
 * GltfLODGeometryExporterは、ILODGeometryViewをGltf形式でエクスポートするための機能を提供します。
 */
public class GltfLODGeometryExporter extends AbstractGltfLODExporter<ILODGeometryView> {

    /**
     * Constructor
     * 
     * @param lodView      the {@link ILODGeometryView}
     * @param featureId    the feature id
     * @param exportOption the exportOption
     */
    public GltfLODGeometryExporter(ILODGeometryView lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    @Override
    protected DefaultSceneModel createSceneModel(ILODGeometryView lodView) {
        if (lodView instanceof LOD1GeometryView) {
            return createSceneModel((LOD1GeometryView) lodView);
        } else if (lodView instanceof LOD2GeometryView) {
            return createSceneModel((LOD2GeometryView) lodView);
        } else if (lodView instanceof LOD3GeometryView) {
            return createSceneModel((LOD3GeometryView) lodView);
        } else {
            throw new NotImplementedException(lodView.getClass().getName() + " is not supported.");
        }
    }

    private DefaultSceneModel createSceneModel(LOD1GeometryView lod1Geometry) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());
        var defaultMaterialModel = GetDefaultMaterialModel();
        for (var polygon : lod1Geometry.getPolygons()) {
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod1Geometry, polygon, defaultMaterialModel);
            meshPrimitiveModel.setMaterialModel(defaultMaterialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD2GeometryView lod2Geometry) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var polygon : lod2Geometry.getPolygons()) {
            MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod2Geometry, polygon, materialModel);
            meshPrimitiveModel.setMaterialModel(materialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }

    private DefaultSceneModel createSceneModel(LOD3GeometryView lod3Geometry) {
        DefaultSceneModel sceneModel = new DefaultSceneModel();
        Map<String, MaterialModelV2> materialMap = new HashMap<>();
        DefaultNodeModel nodeModel = new DefaultNodeModel();
        DefaultMeshModel meshModel = new DefaultMeshModel();
        meshModel.setName(getFeatureId());

        for (var polygon : lod3Geometry.getPolygons()) {
            MaterialModelV2 materialModel = createOrGetMaterialModel(materialMap, polygon);
            DefaultMeshPrimitiveModel meshPrimitiveModel = createMeshPrimitive(lod3Geometry, polygon, materialModel);
            meshPrimitiveModel.setMaterialModel(materialModel);
            meshModel.addMeshPrimitiveModel(meshPrimitiveModel);
        }

        nodeModel.addMeshModel(meshModel);
        sceneModel.addNode(nodeModel);

        return sceneModel;
    }
}
