package org.plateau.citygmleditor.citymodel.factory;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import org.citygml4j.model.citygml.building.AbstractBuilding;
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
import java.util.Map;

public class GeometryFactory extends CityGMLFactory {
    VertexBuffer vertexBuffer = new VertexBuffer();
    FaceBuffer faceBuffer = new FaceBuffer();
    TexCoordBuffer texCoordBuffer = new TexCoordBuffer();

    protected GeometryFactory(CityModelView target) {
        super(target);
    }

    public BuildingInstallationView cretateBuildingInstallationView(AbstractBuilding gmlObject) {
        var BuildingInstallationView = new BuildingInstallationView(gmlObject);

        // </bldg:outerBuildingInstallation>
        var geometrys = new ArrayList<GeometryView>();
        for (var OuterBuildingInstallation : gmlObject.getOuterBuildingInstallation()) {
            var buildingInstallation = OuterBuildingInstallation.getBuildingInstallation();
            if (buildingInstallation.getLod3Geometry() == null)
                continue;
            var geometry = new GeometryView(buildingInstallation.getLod3Geometry().getGeometry());
            geometrys.add(geometry);
            var multiSurface = (MultiSurface) buildingInstallation.getLod3Geometry().getGeometry();
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
                BuildingInstallationView.addLod3MeshView(buildingInstallation.getId(), meshView);
            }
            geometry.setPolygons(polygons);
        }
        BuildingInstallationView.setLod3OuterBuildingInstallations(geometrys);

        return BuildingInstallationView;
    }

    protected PolygonView createPolygon(Polygon gmlObject) {
        var texCoordOffset = texCoordBuffer.getTexCoordCount();

        var polygon = new PolygonView(gmlObject);

        var exterior = createLinearRing((LinearRing) gmlObject.getExterior().getRing());
        polygon.setExteriorRing(exterior);
//        var interior = createLinearRing((org.citygml4j.model.gml.geometry.primitives.LinearRingView) gmlObject.getInterior().getRing());

        // 輪郭をポリゴンメッシュ化
        var ringFaceBuffer = new FaceBuffer();
        Tessellator.tessellate(exterior.getRing(), null, ringFaceBuffer);

        // vertexBuffer内での頂点インデックスに変換
        var polygonFaceBuffer = polygon.getFaceBuffer();
        PolygonMeshUtils.applyIndexRemap(ringFaceBuffer, polygonFaceBuffer, exterior.getVertexIndices());

        // テクスチャ座標インデックス、法線インデックスをオフセット
        var normalOffset = faceBuffer.getPointCount();
        for (int i = 0; i < polygonFaceBuffer.getPointCount(); ++i) {
            polygonFaceBuffer.setTexCoordIndex(i, polygonFaceBuffer.getTexCoordIndex(i) + texCoordOffset);
            polygonFaceBuffer.setNormalIndex(i, polygonFaceBuffer.getNormalIndex(i) + normalOffset);
        }

        faceBuffer.addFaces(polygonFaceBuffer.getBuffer());

        return polygon;
    }

    protected LinearRingView createLinearRing(LinearRing gmlObject) {
        List<Double> coordinates = gmlObject.getPosList().toList3d();

        var ringVertexBuffer = new VertexBuffer();

        for (int i = 0; i < coordinates.size() - 1; i += 3) {
            var geoCoordinate = new GeoCoordinate(coordinates.get(i), coordinates.get(i + 1), coordinates.get(i + 2));
            var position = World.getActiveInstance().getGeoReference().Project(geoCoordinate);
            ringVertexBuffer.addVertex(position);
        }

        var vertexIndices = new ArrayList<Integer>();
        PolygonMeshUtils.weldVertices(
                ringVertexBuffer, vertexBuffer, vertexIndices
        );

        var linearRing = new LinearRingView(
                gmlObject, vertexBuffer, texCoordBuffer,
                vertexIndices, texCoordBuffer.getTexCoordCount());

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
        if (!surfaceDataExists) {
            var texCoordCount = ringVertexBuffer.getVertexCount();
            // UVない場合は0埋め
            texCoordBuffer.addTexCoords(new float[texCoordCount * 2], false);
        }

        return linearRing;
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
