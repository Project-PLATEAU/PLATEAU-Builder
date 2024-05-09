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
import java.util.List;

import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.io.mesh.AxisDirection;
import org.plateaubuilder.core.io.mesh.AxisTransformer;

import javafx.scene.paint.PhongMaterial;

/**
 * 抽象的なObjLODExporterの基底クラスです。 ObjLODExporterは、ILODViewをObj形式でエクスポートするための機能を提供します。
 */
public class AbstractObjLODExporter<T extends ILODView> extends AbstractLODExporter<T> {
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
        ObjectModel objectModel = createObjectModel();

        File file = new File(fileUrl);
        var fileName = file.getName();
        var mtlFileName = String.format("%s.mtl", fileName.substring(0, fileName.lastIndexOf(".")));
        try (var stream = new FileOutputStream(file, false); var writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {
            writer.write("# org.plateaubuilder.core\r\n");
            writer.write(String.format("mtllib %s\r\n", mtlFileName));

            writer.write(String.format("g %s\r\n", objectModel.getName()));
            writer.write(String.format("usemtl %s\r\n", objectModel.getMaterial().getName()));

            var exportOption = getExportOption();
            var axisTransformer = new AxisTransformer(AxisDirection.TOOL_AXIS_DIRECTION,
                    new AxisDirection(exportOption.getAxisEast(), exportOption.getAxisTop(), false));
            var offset = exportOption.getOffset();
            var vertices = objectModel.getVertices();
            for (int i = 0; i < vertices.length; i += 3) {
                var vec3f = axisTransformer.transform((float) (vertices[i] + offset.x), (float) (vertices[i + 1] + offset.y),
                        (float) (vertices[i + 2] + offset.z));
                writer.write(String.format("v %f %f %f\r\n", vec3f.x, vec3f.y, vec3f.z));
            }

            var uvs = objectModel.getUVs();
            for (int i = 0; i < uvs.length; i += 2) {
                writer.write(String.format("vt %f %f\r\n", uvs[i], uvs[i + 1]));
            }

            var faces = objectModel.getFaces();
            for (int i = 0; i < faces.length; i += 6) {
                writer.write(String.format("f %d/%d %d/%d %d/%d\r\n", faces[i] + 1, faces[i + 1] + 1, faces[i + 4] + 1, faces[i + 5] + 1,
                        faces[i + 2] + 1, faces[i + 3] + 1));
            }
            writer.write("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (var stream = new FileOutputStream(new File(file.getParent(), mtlFileName), false);
                var writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"))) {

            var material = objectModel.getMaterial();
            writer.write(String.format("newmtl %s\r\n", material.getName()));
            writer.write("Ka 0.000000 0.000000 0.000000\r\n");
            if (material.hasFileName()) {
                writer.write("Kd 1.0 1.0 1.0\r\n");
            } else {
                writer.write("Kd 0.45 0.5 0.5\r\n");
            }
            writer.write("Ks 0.000000 0.000000 0.000000\r\n");
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
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObjectModel createObjectModel() {
        var lodView = getLODView();
        var polygons = lodView.getPolygons();
        var indexCount = 0;
        for (var polygon : polygons) {
            indexCount += polygon.getFaceBuffer().getPointCount() * 2;
        }

        var faces = new int[indexCount];
        var faceIndex = 0;
        for (var polygon : polygons) {
            var polygonFaceBuffer = polygon.getFaceBuffer();
            for (var i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
                // 頂点インデックス
                faces[faceIndex++] = polygonFaceBuffer.getVertexIndex(i);
                // UVインデックス
                faces[faceIndex++] = polygonFaceBuffer.getTexCoordIndex(i);
            }
        }

        var materialModel = createOrGetMaterial(polygons);

        return new ObjectModel(getFeatureId(), faces, lodView.getVertexBuffer().getBufferAsArray(),
                lodView.getTexCoordBuffer().getBufferAsArray(true), materialModel);
    }

    private MaterialModel createOrGetMaterial(List<PolygonView> polygons) {
        for (var polygon : polygons) {
            var surfaceData = polygon.getSurfaceData();
            if (surfaceData == null)
                continue;

            var material = surfaceData.getMaterial();
            if (!(material instanceof PhongMaterial))
                continue;

            return createMaterialModel((PhongMaterial) material);
        }

        return defaultMaterialModel;
    }

    private MaterialModel createMaterialModel(PhongMaterial material) {
        File materialPath = null;
        try {
            materialPath = new File(new URI(material.getDiffuseMap().getUrl()).getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        var fileName = materialPath.getName();
        var name = fileName.substring(0, fileName.lastIndexOf("."));

        return new MaterialModel(name, fileName, materialPath.toString());
    }
}
