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
import org.plateau.citygmleditor.citymodel.geometry.*;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.PolygonMeshUtils;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeometryFactory extends CityGMLFactory {
    VertexBuffer vertexBuffer = new VertexBuffer();
    FaceBuffer faceBuffer = new FaceBuffer();
    TexCoordBuffer texCoordBuffer = new TexCoordBuffer();

    protected GeometryFactory(CityModelView target) {
        super(target);
    }

    public BuildingInstallationView cretateBuildingInstallationView(BuildingInstallation gmlObject) {
        if (gmlObject.getLod3Geometry() == null)
            return null;

        var geometries = new ArrayList<GeometryView>();

        var buildingInstallationView = new BuildingInstallationView(gmlObject);

        var geometry = new GeometryView(gmlObject.getLod3Geometry().getGeometry());
        var multiSurface = (MultiSurface) gmlObject.getLod3Geometry().getGeometry();
        var polygons = new ArrayList<PolygonView>();
        for (var surfaceMember : multiSurface.getSurfaceMember()) {
            var polygon = (Polygon) surfaceMember.getSurface();
            if (polygon == null)
                continue;
            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);

            // ノードの最小単位＝ポリゴン
            var material = polygonObject.getSurfaceData() != null ? polygonObject.getSurfaceData().getMaterial() : null;
            var polygonMesh = new ArrayList<PolygonView>(List.of(polygonObject));
            var meshView = new MeshView();
            meshView.setMesh(createTriangleMesh(polygonMesh));
            meshView.setMaterial(material != null ? material : World.getActiveInstance().getDefaultMaterial());
            meshView.setId(surfaceMember.getGeometry().getId());
            buildingInstallationView.addLod3MeshView(gmlObject.getId(), meshView);
        }
        geometry.setPolygons(polygons);

        buildingInstallationView.setLod3Geometry(geometry);
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
        Tessellator.tessellate(exterior.getRing(), interiorBuffers, null, subMeshFaceBuffer);

        // vertexBuffer内での頂点インデックスに変換
        var interiorIndexRemaps = new ArrayList<List<Integer>>();
        for (var interior : polygon.getInteriorRings()) {
            interiorIndexRemaps.add(interior.getVertexIndices());
        }
        var indexRemap = PolygonMeshUtils.concatIndexRemaps(exterior.getVertexIndices(), interiorIndexRemaps);
        var polygonFaceBuffer = polygon.getFaceBuffer();
        PolygonMeshUtils.applyIndexRemap(subMeshFaceBuffer, polygonFaceBuffer, indexRemap);

        // テクスチャ座標インデックス、法線インデックスをオフセット
        var normalOffset = faceBuffer.getPointCount();
        for (int i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
            polygonFaceBuffer.setTexCoordIndex(i, polygonFaceBuffer.getTexCoordIndex(i) + texCoordOffset);
            polygonFaceBuffer.setNormalIndex(i, polygonFaceBuffer.getNormalIndex(i) + normalOffset);
        }

        // Polygonの面情報をMeshの面情報として登録
        faceBuffer.addFaces(polygonFaceBuffer.getBuffer());

        return polygon;
    }

    protected LinearRingView createLinearRing(LinearRing gmlObject) {
        List<Double> coordinates = gmlObject.getPosList().toList3d();

        var ringVertexBuffer = new VertexBuffer();

        // 輪郭点をワールド内座標に変換して登録。始点と終点が重複するため終点を削除している。
        for (int i = 0; i < coordinates.size() - 3 - 1; i += 3) {
            var geoCoordinate = new GeoCoordinate(coordinates.get(i), coordinates.get(i + 1), coordinates.get(i + 2));
            var position = World.getActiveInstance().getGeoReference().Project(geoCoordinate);
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

        var surfaceDataExists = extractSurfaceData(linearRing);
        if (!surfaceDataExists) {
            var texCoordCount = ringVertexBuffer.getVertexCount();
            // UVない場合は0埋め
            texCoordBuffer.addTexCoords(new float[texCoordCount * 2], false);
        }

        return linearRing;
    }

    private boolean extractSurfaceData(LinearRingView linearRing) {
        if (getTarget() == null || getTarget().getRGBTextureAppearance() == null)
            return false;

        var surfaceDataExists = false;

        for (var surfaceData : getTarget().getRGBTextureAppearance().getSurfaceData()) {
            var texCoords = surfaceData.getTextureCoordinatesByRing().get("#" + linearRing.getGMLID());

            if (texCoords != null) {
                texCoordBuffer.addTexCoords(texCoords, true);
                linearRing.setSurfaceData(surfaceData);
                surfaceDataExists = true;
                break;
            }
        }
        return surfaceDataExists;
    }

    protected TriangleMesh createTriangleMesh(List<PolygonView> polygons) {
        var mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_NORMAL_TEXCOORD);
        mesh.getPoints().addAll(vertexBuffer.getBufferAsArray());
        mesh.getFaces().addAll(faceBuffer.getBufferAsArray());
        mesh.getTexCoords().addAll(texCoordBuffer.getBufferAsArray());

        var normals = PolygonMeshUtils.calculateNormal(vertexBuffer, faceBuffer);
        mesh.getNormals().addAll(normals);

        var smooth = new int[faceBuffer.getBuffer().size() / mesh.getFaceElementSize()];
        Arrays.fill(smooth, 1);
        mesh.getFaceSmoothingGroups().addAll(smooth);

        return mesh;
    }
}
