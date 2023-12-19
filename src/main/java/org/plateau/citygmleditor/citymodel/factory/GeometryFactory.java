package org.plateau.citygmleditor.citymodel.factory;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.citymodel.SurfaceData;
import org.plateau.citygmleditor.citymodel.geometry.*;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GeometryFactory extends CityGMLFactory {
    protected GeometryFactory(CityModel target) {
        super(target);
    }

    public LOD1Solid createLOD1Solid(Solid gmlObject) {
        var exterior = gmlObject.getExterior();
        var compositeSurface = (CompositeSurface) exterior.getObject();

        List<SurfaceProperty> surfaceMember = compositeSurface.getSurfaceMember();

        var polygons = new ArrayList<Polygon>();

        for (SurfaceProperty surfaceMemberElement : surfaceMember) {
            var polygon = (org.citygml4j.model.gml.geometry.primitives.Polygon) surfaceMemberElement.getSurface();
            if (polygon == null)
                continue;

            var polygonObject = createPolygon(polygon);
            polygons.add(polygonObject);
        }

        var mesh = createTriangleMesh(polygons);

        var solid = new LOD1Solid(gmlObject);
        solid.setPolygons(polygons);
        solid.setMesh(mesh);
        solid.setMaterial(World.getActiveInstance().getDefaultMaterial());

        return solid;
    }

    public LOD2Solid createLOD2Solid(AbstractBuilding gmlObject) {
        if (gmlObject.getLod2Solid() == null)
            return null;

        var solid = new LOD2Solid(gmlObject.getLod2Solid().getObject());

        var polygons = new ArrayList<Polygon>();
        var boundaries = new ArrayList<BoundarySurface>();

        for (var boundedBySurface : gmlObject.getBoundedBySurface()) {
            var boundary = new BoundarySurface(boundedBySurface.getBoundarySurface());
            boundaries.add(boundary);

            var boundaryPolygons = new ArrayList<Polygon>();
            for (var surfaceMember : boundedBySurface.getBoundarySurface().getLod2MultiSurface().getMultiSurface().getSurfaceMember()) {
                var polygon = (org.citygml4j.model.gml.geometry.primitives.Polygon) surfaceMember.getSurface();
                if (polygon == null)
                    continue;

                var polygonObject = createPolygon(polygon);
                polygons.add(polygonObject);
                boundaryPolygons.add(polygonObject);
            }

            boundary.setPolygons(boundaryPolygons);
        }
        solid.setBoundaries(boundaries);

        var polygonsMap = solid.getSurfaceDataPolygonsMap();
        for (Map.Entry<SurfaceData, ArrayList<Polygon>> entry : polygonsMap.entrySet()) {
            var meshView = new MeshView();
            meshView.setMesh(createTriangleMesh(entry.getValue()));
            if (entry.getKey() == null) {
                meshView.setMaterial(World.getActiveInstance().getDefaultMaterial());
            } else {
                meshView.setMaterial(entry.getKey().getMaterial());
            }
            solid.addMeshView(meshView);
        }

        return solid;
    }

    public Polygon createPolygon(org.citygml4j.model.gml.geometry.primitives.Polygon gmlObject) {
        var polygon = new Polygon(gmlObject);

        var exterior = createLinearRing((org.citygml4j.model.gml.geometry.primitives.LinearRing) gmlObject.getExterior().getRing());
        polygon.setExteriorRing(exterior);
//        var interior = createLinearRing((org.citygml4j.model.gml.geometry.primitives.LinearRing) gmlObject.getInterior().getRing());

        var faces = Tessellator.tessellate(exterior.getVertices(), null, 3);

        polygon.setFaces(faces);

        return polygon;
    }

    public LinearRing createLinearRing(org.citygml4j.model.gml.geometry.primitives.LinearRing gmlObject) {
        var linearRing = new LinearRing(gmlObject);
        List<Double> coordinates = gmlObject.getPosList().toList3d();

        var vertices = new float[coordinates.size()];

        for (int i = 0; i < coordinates.size() - 1; i += 3) {
            var geoCoordinate = new GeoCoordinate(coordinates.get(i), coordinates.get(i + 1), coordinates.get(i + 2));
            var position = World.getActiveInstance().getGeoReference().Project(geoCoordinate);
            vertices[i] = position.x;
            vertices[i + 1] = position.y;
            vertices[i + 2] = position.z;
        }

        linearRing.setVertices(vertices);

        for (var surfaceData : getTarget().getRGBTextureAppearance().getSurfaceData()) {
            var texCoords = surfaceData.getTextureCoordinatesByRing().get("#" + linearRing.getGMLID());

            if (texCoords != null) {
                linearRing.setUVs(texCoords);
                linearRing.setSurfaceData(surfaceData);
                break;
            }
        }
        if (linearRing.getUVs() == null) {
            linearRing.setUVs(new float[linearRing.getVertices().length / 3 * 2]);
        }

        return linearRing;
    }

    public static TriangleMesh createTriangleMesh(ArrayList<Polygon> polygons) {
        var indexCount = 0;
        for (var polygon : polygons) {
            indexCount += polygon.getFaces().length;
        }

        var faces = new int[indexCount];
        var verticesSize = 0;
        var uvsSize = 0;
        var faceIndex = 0;
        for (var polygon : polygons) {
            var polygonFaces = polygon.getFaces();
            for (var i = 0; i < polygonFaces.length; i += 2) {
                // 頂点インデックス
                faces[faceIndex++] = polygonFaces[i] + verticesSize / 3;
                // UVインデックス
                faces[faceIndex++] = polygonFaces[i + 1] + uvsSize / 2;
            }

            verticesSize += polygon.getAllVerticesSize();
            uvsSize += polygon.getAllUVsSize();
        }

        var vertices = new float[verticesSize];
        var vertexIndexOffset = 0;
        for (var polygon : polygons) {
            var subVertices = polygon.getAllVertices();
            for (int i = 0; i < subVertices.length; ++i) {
                vertices[vertexIndexOffset + i] = (float) subVertices[i];
            }
            vertexIndexOffset += subVertices.length;
        }

        var uvs = new float[uvsSize];
        var uvIndexOffset = 0;
        for (var polygon : polygons) {
            var subUVs = polygon.getAllUVs();
            for (int i = 0; i < subUVs.length; i += 2) {
                // x
                uvs[uvIndexOffset + i] = (float) subUVs[i];
                // y
                uvs[uvIndexOffset + i + 1] = 1 - (float) subUVs[i + 1];
            }
            uvIndexOffset += subUVs.length;
        }

        var mesh = new TriangleMesh();
        mesh.getPoints().addAll(vertices);
        mesh.getFaces().addAll(faces);
        mesh.getTexCoords().addAll(uvs);

        var smooth = new int[faces.length / mesh.getFaceElementSize()];
        Arrays.fill(smooth, 1);

        mesh.getFaceSmoothingGroups().addAll(smooth);

        return mesh;
    }
}
