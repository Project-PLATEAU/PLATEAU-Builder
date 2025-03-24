package org.plateaubuilder.core.io.mesh.exporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.io.mesh.AxisDirection;
import org.plateaubuilder.core.io.mesh.AxisTransformer;

import javafx.scene.paint.PhongMaterial;

/**
 * 抽象的なObjLODExporterの基底クラスです。 ObjLODExporterは、ILODViewをObj形式でエクスポートするための機能を提供します。
 */
abstract public class AbstractObjLODExporter<T extends ILODView> extends AbstractLODExporter<T> {
    private static final MaterialModel defaultMaterialModel = new MaterialModel("defaultMaterialModel");

    /**
     * AbstractObjLODExporterクラスのコンストラクターです。
     * 
     * @param lodView      {@link ILODView}
     * @param featureId    Feature ID
     * @param exportOption エクスポートオプション
     */
    public AbstractObjLODExporter(T lodView, String featureId, ExportOption exportOption) {
        super(lodView, featureId, exportOption);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void export(String fileUrl) {
        var objectModels = createObjectModels(getLODView());

        File file = new File(fileUrl);
        var fileName = file.getName();
        var mtlFileName = String.format("%s.mtl", fileName.substring(0, fileName.lastIndexOf(".")));
        try (var stream = new FileOutputStream(file, false); var writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {
            writer.write("# org.plateaubuilder.core\r\n");
            writer.write(String.format("mtllib %s\r\n", mtlFileName));

            for (var objectModel : objectModels) {
                writer.write(String.format("o %s\r\n", objectModel.getName()));

                var exportOption = getExportOption();
                var axisTransformer = new AxisTransformer(AxisDirection.TOOL_AXIS_DIRECTION,
                        new AxisDirection(exportOption.getAxisEast(), exportOption.getAxisTop(), false));
                var offset = exportOption.getOffset();
                var vertices = objectModel.getVertices();
                for (int i = 0; i < vertices.length; i += 3) {
                    var vec3f = axisTransformer.transform(vertices[i] + offset.x, vertices[i + 1] + offset.y, vertices[i + 2] + offset.z);
                    writer.write(String.format("v %f %f %f\r\n", vec3f.x, vec3f.y, vec3f.z));
                }

                var uvs = objectModel.getUVs();
                for (int i = 0; i < uvs.length; i += 2) {
                    writer.write(String.format("vt %f %f\r\n", uvs[i], uvs[i + 1]));
                }

                for (var name : objectModel.getMaterialNames()) {
                    writer.write(String.format("usemtl %s\r\n", name));
                    var faces = objectModel.getFaces(name);
                    for (int i = 0; i < faces.length; i += 6) {
                        writer.write(String.format("f %d/%d %d/%d %d/%d\r\n", faces[i] + 1, faces[i + 1] + 1, faces[i + 4] + 1, faces[i + 5] + 1,
                                faces[i + 2] + 1, faces[i + 3] + 1));
                    }
                }

                writer.write("\r\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var stream = new FileOutputStream(new File(file.getParent(), mtlFileName), false);
                var writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {

            for (var objectModel : objectModels) {
                for (var name : objectModel.getMaterialNames()) {
                    var material = objectModel.getMaterial(name);
                    writer.write(String.format("newmtl %s\r\n", material.getName()));
                    writer.write("Ka 0.000000 0.000000 0.000000\r\n");
                    var diffuseColor = material.getDiffuseColor();
                    if (diffuseColor != null) {
                        writer.write(String.format("Kd %f %f %f\r\n", diffuseColor.getRed(), diffuseColor.getGreen(), diffuseColor.getBlue()));
                    }
                    var specularColor = material.getSpecularColor();
                    if (specularColor != null) {
                        writer.write(String.format("Ks %f %f %f\r\n", specularColor.getRed(), specularColor.getGreen(), specularColor.getBlue()));
                    }
                    writer.write("Ns 2.000000\r\n");
                    writer.write("d 1.000000\r\n");
                    writer.write("Tr 0.000000\r\n");
                    writer.write("Pr 0.333333\r\n");
                    writer.write("Pm 0.080000\r\n");
                    if (material.hasFileName()) {
                        var materialFileName = material.getFileName();
                        writer.write(String.format("map_Kd %s\r\n", materialFileName));

                        var copyPath = new File(file.getParent(), materialFileName);
                        Files.copy(Paths.get(new File(material.getMaterialUrl()).getAbsolutePath()), Paths.get(copyPath.getAbsolutePath()),
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                    writer.write("\r\n");
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<ObjectModel> createObjectModels(T lodView) {
        return List.of(createObjectModel(lodView));
    }

    protected ObjectModel createObjectModel(T lodView) {
        return createObjectModel(lodView, getFeatureId());
    }

    protected ObjectModel createObjectModel(T lodView, String id) {
        return createObjectModel(lodView, id, 0, 0);
    }

    protected ObjectModel createObjectModel(T lodView, String id, int vertexOffset, int uvOffset) {
        var objectModel = new ObjectModel(id, lodView.getVertexBuffer().getBufferAsArray(), lodView.getTexCoordBuffer().getBufferAsArray(true));

        var materialMap = new HashMap<PhongMaterial, MaterialModel>();
        var materialFacesMap = new HashMap<MaterialModel, List<Integer>>();
        for (var polygon : lodView.getPolygons()) {
            var materialModel = createOrGetMaterial(polygon, materialMap);
            if (!materialFacesMap.containsKey(materialModel)) {
                materialFacesMap.put(materialModel, new ArrayList<>());
            }
            var facesList = materialFacesMap.get(materialModel);
            var polygonFaceBuffer = polygon.getFaceBuffer();
            for (var i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
                // 頂点インデックス
                facesList.add(polygonFaceBuffer.getVertexIndex(i) + vertexOffset);
                // UVインデックス
                facesList.add(polygonFaceBuffer.getTexCoordIndex(i) + uvOffset);
            }
        }

        for (var entry : materialFacesMap.entrySet()) {
            var materialModel = entry.getKey();
            var faces = entry.getValue().stream().mapToInt(i -> i).toArray();
            objectModel.addMaterialFaces(materialModel.getName(), materialModel, faces);
        }

        return objectModel;
    }

    private MaterialModel createOrGetMaterial(PolygonView polygonView, Map<PhongMaterial, MaterialModel> materialMap) {
        var surfaceData = polygonView.getSurfaceData();
        if (surfaceData == null)
            return defaultMaterialModel;

        var material = surfaceData.getMaterial();
        if (!(material instanceof PhongMaterial))
            return defaultMaterialModel;

        var phongMaterial = (PhongMaterial) material;
        if (materialMap.containsKey(phongMaterial)) {
            return materialMap.get(phongMaterial);
        }

        var diffuseMap = phongMaterial.getDiffuseMap();
        MaterialModel materialModel = null;
        if (diffuseMap != null) {
            materialModel = createMaterialModel(phongMaterial, diffuseMap.getUrl());
        } else {
            materialModel = createMaterialModel(phongMaterial);
        }
        materialMap.put(phongMaterial, materialModel);

        return materialModel;
    }

    private MaterialModel createMaterialModel(PhongMaterial material, String url) {
        File materialPath = null;
        try {
            materialPath = new File(new URI(url).getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        var fileName = materialPath.getName();
        var name = fileName.substring(0, fileName.lastIndexOf("."));

        var materialModel = new MaterialModel(name, fileName, materialPath.toString());
        materialModel.setDiffuseColor(material.getDiffuseColor());
        materialModel.setSpecularColor(material.getSpecularColor());
        // PhongMaterial には emissiveColor がない
        // materialModel.setEmissiveColor(material.getEmissiveColor());

        return materialModel;
    }

    private MaterialModel createMaterialModel(PhongMaterial material) {
        var materialModel = new MaterialModel(String.format("%d", material.hashCode()));
        materialModel.setDiffuseColor(material.getDiffuseColor());
        materialModel.setSpecularColor(material.getSpecularColor());
        // PhongMaterial には emissiveColor がない
        // materialModel.setEmissiveColor(material.getEmissiveColor());

        return materialModel;
    }
}
