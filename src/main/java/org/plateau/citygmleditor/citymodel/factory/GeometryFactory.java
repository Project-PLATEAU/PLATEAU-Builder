package org.plateau.citygmleditor.citymodel.factory;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateau.citygmleditor.citymodel.BuildingInstallationView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.citymodel.geometry.*;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.polygonmesh.*;
import org.plateau.citygmleditor.world.World;

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

    public BuildingInstallationView cretateBuildingInstallationView(BuildingInstallation gmlObject, int lod) {
        var lodGeometry = lod == 2 ? gmlObject.getLod2Geometry() : gmlObject.getLod3Geometry();
        if (lodGeometry == null)
            return null;

        var buildingInstallationView = new BuildingInstallationView(gmlObject, vertexBuffer, texCoordBuffer);

        var geometry = new GeometryView(lodGeometry.getGeometry());
        var multiSurface = (MultiSurface) lodGeometry.getGeometry();
        var polygons = new ArrayList<PolygonView>();
        for (var surfaceMember : multiSurface.getSurfaceMember()) {
            var polygon = (Polygon) surfaceMember.getSurface();
            if (polygon == null)
                continue;
            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);
        }
        geometry.setPolygons(polygons);
        buildingInstallationView.setMesh(createTriangleMesh(polygons));
        buildingInstallationView.setGeometryView(geometry);
        return buildingInstallationView;
    }

    protected PolygonView createPolygon(Polygon gmlObject) {
        var texCoordOffset = texCoordBuffer.getTexCoordCount();

        var polygon = new PolygonView(gmlObject);

        var exterior = createLinearRing((LinearRing) gmlObject.getExterior().getRing());
        polygon.setExteriorRing(exterior);

        var interiorBuffers = new ArrayList<VertexBuffer>();
        for (var interiorRing : gmlObject.getInterior()) {
            var interior = createLinearRing((LinearRing) interiorRing.getRing());
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

    protected LinearRingView createLinearRing(LinearRing gmlObject) {
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

        var surfaceData = findAssociatedSurfaceData(linearRing);
        if (surfaceData != null) {
            linearRing.setSurfaceData(surfaceData);

            var texCoords = surfaceData.getTextureCoordinatesByRing().get("#" + linearRing.getGMLID());
            // tesselate処理でテクスチャ座標数と頂点座標数が一致している必要があるため切り詰め or 0埋め
            var desiredTexCoordSize = ringVertexBuffer.getVertexCount() * 2;
            if (texCoords.length > desiredTexCoordSize) {
                texCoords = Arrays.copyOf(texCoords, desiredTexCoordSize);
                System.out.print("TexCoord size of " + linearRing.getGMLID() + " is greater than vertex size. TexCoords will be truncated.\n");
            } else {
                var copiedTexCoords = new float[desiredTexCoordSize];
                System.arraycopy(texCoords, 0, copiedTexCoords, 0, desiredTexCoordSize);
                texCoords = copiedTexCoords;
                System.out.print("TexCoord size of " + linearRing.getGMLID() + " is less than vertex size. TexCoords will be filled with 0.\n");
            }

            texCoordBuffer.addTexCoords(texCoords, true);
        }else {
            var texCoordCount = ringVertexBuffer.getVertexCount();
            // UVない場合は0埋め
            texCoordBuffer.addTexCoords(new float[texCoordCount * 2], false);
        }

        return linearRing;
    }

    private SurfaceDataView findAssociatedSurfaceData(LinearRingView linearRing) {
        if (getTarget() == null || getTarget().getRGBTextureAppearance() == null)
            return null;

        for (var surfaceData : getTarget().getRGBTextureAppearance().getSurfaceData()) {
            var texCoords = surfaceData.getTextureCoordinatesByRing().get("#" + linearRing.getGMLID());
            if (texCoords != null)
                return surfaceData;
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
