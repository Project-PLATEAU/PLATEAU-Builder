package org.plateaubuilder.core.citymodel.factory;

import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.GeometryProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateaubuilder.core.citymodel.BuildingInstallationView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.citymodel.geometry.GeometryView;
import org.plateaubuilder.core.citymodel.geometry.LinearRingView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.geospatial.GeoCoordinate;
import org.plateaubuilder.core.utils3d.polygonmesh.*;
import org.plateaubuilder.core.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeometryFactory extends CityGMLFactory {
    VertexBuffer vertexBuffer = new VertexBuffer();
    VertexBuffer normalBuffer = new VertexBuffer();
    TexCoordBuffer texCoordBuffer = new TexCoordBuffer();

    protected GeometryFactory(CityModelView target) {
        super(target);
    }

    public GeometryView createGeometryView(GeometryProperty<? extends AbstractGeometry> gmlGeometry) {
        if (gmlGeometry == null)
            return null;
        if(gmlGeometry.getGeometry() == null)
            return null;

        var multiSurface = (MultiSurface) gmlGeometry.getGeometry();
        var polygons = new ArrayList<PolygonView>();
        for (var surfaceMember : multiSurface.getSurfaceMember()) {
            var polygon = (Polygon) surfaceMember.getSurface();
            if (polygon == null)
                continue;
            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);
        }
        var geometry = new GeometryView(gmlGeometry.getGeometry(), vertexBuffer, texCoordBuffer);
        geometry.setPolygons(polygons);
        geometry.setMesh(createTriangleMesh(polygons));
        geometry.setMaterial(World.getActiveInstance().getDefaultMaterial());
        return geometry;
    }

    protected PolygonView createPolygon(Polygon gmlObject) {
        return createPolygon(gmlObject, null);
    }

    protected PolygonView createPolygon(Polygon gmlObject, String targetId) {
        var texCoordOffset = texCoordBuffer.getTexCoordCount();

        var polygon = new PolygonView(gmlObject);

        var exterior = createLinearRing((LinearRing) gmlObject.getExterior().getRing(), targetId);
        polygon.setExteriorRing(exterior);

        var interiorBuffers = new ArrayList<VertexBuffer>();
        for (var interiorRing : gmlObject.getInterior()) {
            var interior = createLinearRing((LinearRing) interiorRing.getRing(), targetId);
            polygon.addInteriorRing(interior);
            interiorBuffers.add(interior.getRing());
        }

        // 輪郭をポリゴンメッシュ化
        var subMeshFaceBuffer = new FaceBuffer();
        var subMeshVertexBuffer = new VertexBuffer();
        Tessellator.tessellate(exterior.getRing(), interiorBuffers, subMeshVertexBuffer, subMeshFaceBuffer);

        // vertexBuffer内での頂点インデックスに変換
        var interiorIndexRemaps = new ArrayList<List<Integer>>();
        for (var interior : polygon.getInteriorRings()) {
            interiorIndexRemaps.add(interior.getVertexIndices());
        }
        var indexRemap = PolygonMeshUtils.concatIndexRemaps(exterior.getVertexIndices(), interiorIndexRemaps);
        var polygonFaceBuffer = polygon.getFaceBuffer();
        PolygonMeshUtils.applyIndexRemap(subMeshFaceBuffer, polygonFaceBuffer, indexRemap);

        // テクスチャ座標インデックス、法線インデックスをオフセット
        var normalOffset = normalBuffer.getVertexCount();
        for (int i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
            polygonFaceBuffer.setTexCoordIndex(i, polygonFaceBuffer.getTexCoordIndex(i) + texCoordOffset);
            polygonFaceBuffer.setNormalIndex(i, polygonFaceBuffer.getNormalIndex(i) + normalOffset);
        }

        // 法線計算
        var subMeshNormals = PolygonMeshUtils.calculateNormal(subMeshVertexBuffer, subMeshFaceBuffer);
        normalBuffer.addVertices(subMeshNormals);

        return polygon;
    }

    protected LinearRingView createLinearRing(LinearRing gmlObject, String targetId) {
        List<Double> coordinates = gmlObject.getPosList().toList3d();

        var ringVertexBuffer = new VertexBuffer();

        // 輪郭点をワールド内座標に変換して登録。始点と終点が重複するため終点を削除している。
        for (int i = 0; i < coordinates.size() - 3 - 1; i += 3) {
            var geoCoordinate = new GeoCoordinate(coordinates.get(i), coordinates.get(i + 1), coordinates.get(i + 2));
            var position = World.getActiveInstance().getGeoReference().project(geoCoordinate);
            ringVertexBuffer.addVertex(position);
        }

        var vertexIndices = new ArrayList<Integer>();
        // ringの頂点をMeshの頂点として登録。重複する頂点は接合される。
        PolygonMeshUtils.weldVertices(
                ringVertexBuffer, vertexBuffer, vertexIndices
        );

        var linearRing = new LinearRingView(
                gmlObject, vertexBuffer, texCoordBuffer,
                vertexIndices, texCoordBuffer.getTexCoordCount());

        var surfaceData = findAssociatedSurfaceData(linearRing, targetId);
        if (surfaceData != null) {
            linearRing.setSurfaceData(surfaceData);

            var texCoords = surfaceData.getTextureCoordinatesByRing().get("#" + linearRing.getGMLID());
            if (texCoords != null) {
                // tesselate処理でテクスチャ座標数と頂点座標数が一致している必要があるため切り詰め or 0埋め
                var desiredTexCoordSize = ringVertexBuffer.getVertexCount() * 2;
                if (texCoords.length > desiredTexCoordSize) {
                    texCoords = Arrays.copyOf(texCoords, desiredTexCoordSize);
                } else {
                    var copiedTexCoords = new float[desiredTexCoordSize];
                    System.arraycopy(texCoords, 0, copiedTexCoords, 0, desiredTexCoordSize);
                    texCoords = copiedTexCoords;
                    System.out.print("TexCoord size of " + linearRing.getGMLID() + " is less than vertex size. TexCoords will be filled with 0.\n");
                }

                texCoordBuffer.addTexCoords(texCoords, true);
            } else {
                var texCoordCount = ringVertexBuffer.getVertexCount();
                // UVない場合は0埋め
                texCoordBuffer.addTexCoords(new float[texCoordCount * 2], false);
            }
        }else {
            var texCoordCount = ringVertexBuffer.getVertexCount();
            // UVない場合は0埋め
            texCoordBuffer.addTexCoords(new float[texCoordCount * 2], false);
        }

        return linearRing;
    }

    private SurfaceDataView findAssociatedSurfaceData(LinearRingView linearRing, String targetId) {
        if (getTarget() == null || getTarget().getAppearance() == null)
            return null;

        SurfaceDataView texture = null;
        SurfaceDataView x3dMaterial = null;
        for (var surfaceData : getTarget().getAppearance().getSurfaceData()) {
            switch (surfaceData.getSurfaceType()) {
            case Texture:
                var texCoords = surfaceData.getTextureCoordinatesByRing().get("#" + linearRing.getGMLID());
                if (texCoords != null) {
                    texture = surfaceData;
                }
                break;
            case X3D:
                if (targetId != null && surfaceData.getTargetSet().contains("#" + targetId)) {
                    x3dMaterial = surfaceData;
                }
                break;
            default:
                break;
            }
        }

        if (texture != null) {
            if (x3dMaterial != null) {
                // TODO: linearRingごとにマテリアルが違う場合がある?
                var textureMaterial = (PhongMaterial) texture.getMaterial();
                var material = (PhongMaterial) x3dMaterial.getMaterial();
                material.setDiffuseColor(textureMaterial.getDiffuseColor());
                material.setSpecularColor(textureMaterial.getSpecularColor());
                material.setSpecularPower(textureMaterial.getSpecularPower());
            }
            return texture;
        } else if (x3dMaterial != null) {
            return x3dMaterial;
        }

        return null;
    }

    /**
     * Polygonの一覧からTriangleMeshを生成します。
     * 面情報以外（頂点、法線、テクスチャ座標）は他Polygonのものも含むため、部分的なPolygonに対して呼び出される場合冗長なデータが生成されます。
     */
    protected TriangleMesh createTriangleMesh(List<PolygonView> polygons) {
        var mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        mesh.getPoints().addAll(vertexBuffer.getBufferAsArray());
        mesh.getTexCoords().addAll(texCoordBuffer.getBufferAsArray());
        mesh.getNormals().addAll(normalBuffer.getBufferAsArray());

        var faces = new FaceBuffer();
        for (var polygon : polygons) {
            faces.addFaces(polygon.getFaceBuffer().getBuffer());
        }
        mesh.getFaces().addAll(faces.getBufferAsArray());

        var smooth = new int[faces.getFaceCount()];
        Arrays.fill(smooth, 1);
        mesh.getFaceSmoothingGroups().addAll(smooth);

        return mesh;
    }
}
