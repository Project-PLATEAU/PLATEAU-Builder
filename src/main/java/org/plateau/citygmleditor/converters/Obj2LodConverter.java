package org.plateau.citygmleditor.converters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.Coord;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.geometry.GeoReference;
import org.plateau.citygmleditor.importers.obj.ObjImporter;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;

import javafx.collections.ObservableFloatArray;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.ObservableFaceArray;
import javafx.scene.shape.TriangleMesh;

public class Obj2LodConverter {

    private org.locationtech.jts.geom.GeometryFactory _geometryFactory = new org.locationtech.jts.geom.GeometryFactory();

    private CompositeSurface _compositeSurface = new CompositeSurface();

    private org.citygml4j.model.citygml.core.CityModel _cityModel;

    private GeoReference _geoReference;

    public Obj2LodConverter(CityModelView cityModelView) {
        _cityModel = (org.citygml4j.model.citygml.core.CityModel) cityModelView.getGmlObject();
        var envelope = (_cityModel).getBoundedBy().getEnvelope();
        var lowerCorner = envelope.getLowerCorner().toList3d();
        var min = new GeoCoordinate(lowerCorner);
        var upperCorner = envelope.getUpperCorner().toList3d();
        var max = new GeoCoordinate(upperCorner);
        var center = min.add(max).divide(2);
        _geoReference = new GeoReference(center);
    }

    public ILODSolidView convert(String fileUrl) throws Exception {
        var reader = new ObjImporter(Paths.get(fileUrl).toUri().toURL().toString());
        var meshKeys = reader.getMeshes();

        var cityModel = (org.citygml4j.model.citygml.core.CityModel)_cityModel.copy(new DeepCopyBuilder());

        // TODO:ObjModel→CityModel変換
        Map<String, ParameterizedTexture> textureMap = new HashMap<>();
        for (var key : meshKeys) {
            Material material = reader.getMaterial(key);
            ParameterizedTexture parameterizedTexture = createOrGetParameterizedTexture(key, material, textureMap);

            TriangleMesh triangleMesh = reader.getMesh(key);

            // 三角形のリストを作成
            var faces = triangleMesh.getFaces();
            var vertices = triangleMesh.getPoints();
            var coords = triangleMesh.getTexCoords();
            List<TriangleModel> triangleModels = new LinkedList<>();
            for (var i = 0; i < faces.size(); i += 6) {
                triangleModels.add(new TriangleModel(faces, i, vertices, coords));
            }

            List<List<TriangleModel>> sameTrianglesList = new ArrayList<>();

            // 三角形のリストが空になるまでループ
            while (triangleModels.size() > 0) {
                var baseTriangle = triangleModels.get(0);
                List<TriangleModel> sameNormalList = new ArrayList<>();
                sameNormalList.add(baseTriangle);

                var baseDot = baseTriangle.dot();

                // 最初に見つかった三角形を基準として、法線ベクトルと平面が同じ三角形をグループ化
                for (var i = 1; i < triangleModels.size(); i++) {
                    var triangleModel = triangleModels.get(i);
                    var angle = baseTriangle.getNormal().angle(triangleModel.getNormal());
                    if (angle != 0) continue;
                    if (baseDot != triangleModel.dot()) continue;
                    sameNormalList.add(triangleModel);
                }
                sameTrianglesList.add(sameNormalList);

                // 三角形のリストから削除
                for (var sameTriangle : sameNormalList) {
                    triangleModels.remove(sameTriangle);
                }
            }

            // グループ化したポリゴンごとに結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = new ArrayList<>();
            for (var sameTriangleList : sameTrianglesList) {
                jtsPolygonList.addAll(createPolygonList(sameTriangleList));
            }
            System.out.println(jtsPolygonList.size());

            // gmlのPolygonに変換
            List<Polygon> polygonList = new ArrayList<>();
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, polygonList, parameterizedTexture);
            }

            // TODO:DEBUG
            for (var polygon : polygonList) {
                dump(polygon);
            }

            // gmlのParameterizedTextureを差し替える
            var replaced = false;
            for (var orgAppearanceMember : cityModel.getAppearanceMember()) {
                if (replaced) break;
                for (var orgSurfaceDataMember : orgAppearanceMember.getAppearance().getSurfaceDataMember()) {
                    if (replaced) break;
                    if (orgSurfaceDataMember.getSurfaceData() instanceof ParameterizedTexture) {
                        ParameterizedTexture orgParameterizedTexture = (ParameterizedTexture)orgSurfaceDataMember.getSurfaceData();
                        if (orgParameterizedTexture.getImageURI().endsWith("hnap0665.jpg")) {
                            orgSurfaceDataMember.setSurfaceData(parameterizedTexture);
                            replaced = true;
                        }
                    }
                }
            }
        }

        // Solid solid = new Solid();
        // solid.setExterior(new SurfaceProperty(compositeSurface));
        // LOD2SolidView lod2Solid = new LOD2SolidView(solid);
        // lod2Solid.setBoundaries(boundedBy);

        // return lod2Solid;
        return null;
    }

    private List<org.locationtech.jts.geom.Polygon> createPolygonList(List<TriangleModel> triangleList) {
        List<org.locationtech.jts.geom.Polygon> trianglePolygonList = new LinkedList<>();
        for (var triangleModel : triangleList) {
            var p1 = triangleModel.getVertex(0);
            var p2 = triangleModel.getVertex(1);
            var p3 = triangleModel.getVertex(2);
            var linearRing = _geometryFactory.createLinearRing(
                        new org.locationtech.jts.geom.Coordinate[] {
                            new org.locationtech.jts.geom.Coordinate(p1.x, p1.y, p1.z),
                            new org.locationtech.jts.geom.Coordinate(p2.x, p2.y, p2.z),
                            new org.locationtech.jts.geom.Coordinate(p3.x, p3.y, p3.z),
                            new org.locationtech.jts.geom.Coordinate(p1.x, p1.y, p1.z)
                        });

            // PolygonにuserDataとしてTriangleModelを保持しておく
            var polygon = _geometryFactory.createPolygon(linearRing, null);
            polygon.setUserData(triangleModel);
            trianglePolygonList.add(polygon);
        }

        org.locationtech.jts.geom.Geometry geometry = null;
        for (var trianglePolygon : trianglePolygonList) {
            if (geometry == null) {
                geometry = trianglePolygon;
            } else {
                geometry = geometry.union(trianglePolygon);
            }
        }

        // 結合した結果がMultiPolygonの場合があるのでPolygonに分割する
        List<org.locationtech.jts.geom.Polygon> polygonList = splitJtsPolygon(geometry);

        // 元のTriangleが、分割したPolygonに内包されるかどうかを判定
        // 内包される場合は、そのTriangleModelのuserDataを保持する
        for (var trianglePolygon : trianglePolygonList) {
            var found = false;
            for (var polygon : polygonList) {
                if (polygon.contains(trianglePolygon) || polygon.equalsExact(trianglePolygon)) {
                    if (polygon.getUserData() == null) {
                        polygon.setUserData(new ArrayList<TriangleModel>());
                    }
                    List<TriangleModel> userDataList = (List<TriangleModel>)polygon.getUserData();
                    userDataList.add((TriangleModel)trianglePolygon.getUserData());
                    found = true;
                    break;
                }
            }

            // TODO:DEBUG
            // check found
            if (!found) {
                System.out.println("Triangle");
                dumpInsertSql(trianglePolygon);
                System.out.println("Polygon");
                for (var polygon : polygonList) {
                    dumpInsertSql(polygon);
                }
                System.out.println();
            }
        }

        // check userdata
        for (var polygon : polygonList) {
            if (polygon.getUserData() == null) {
                System.out.println("userdata null");
                System.out.println(polygon);
            }
        }

        return polygonList;
    }

    private List<org.locationtech.jts.geom.Polygon> splitJtsPolygon(org.locationtech.jts.geom.Geometry geometry) {
        List<org.locationtech.jts.geom.Polygon> polygonList = new ArrayList<>();
        if (geometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
            var original = (org.locationtech.jts.geom.Polygon)geometry;
            org.locationtech.jts.geom.LinearRing shellCopy = (org.locationtech.jts.geom.LinearRing)original.getExteriorRing().copy();
            org.locationtech.jts.geom.LinearRing[] holeCopies = new org.locationtech.jts.geom.LinearRing[original.getNumInteriorRing()];
            for (int i = 0; i < holeCopies.length; i++) {
                holeCopies[i] = (org.locationtech.jts.geom.LinearRing)original.getInteriorRingN(i).copy();
            }
            polygonList.add(_geometryFactory.createPolygon(shellCopy, holeCopies));
        } else if (geometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
            for (var i = 0; i < geometry.getNumGeometries(); i++) {
                polygonList.add((org.locationtech.jts.geom.Polygon)geometry.getGeometryN(i));
            }
        } else {
            System.out.println(String.format("Error splitJtsPolygon: GeometryType:%s", geometry.getGeometryType()));
        }

        return polygonList;
    }

    private void toGmlPolygonList(org.locationtech.jts.geom.Geometry jtsGeometry, List<Polygon> polygonList, ParameterizedTexture parameterizedTexture) {
        if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
            polygonList.add(createPolygon((org.locationtech.jts.geom.Polygon)jtsGeometry, parameterizedTexture));
        } else if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
            for (var i = 0; i < jtsGeometry.getNumGeometries(); i++) {
                toGmlPolygonList(jtsGeometry.getGeometryN(i), polygonList, parameterizedTexture);
            }
        } else {
            System.out.println(String.format("Error toGmlPolygonList: GeometryType:%s", jtsGeometry.getGeometryType()));
        }
    }

    private Polygon createPolygon(org.locationtech.jts.geom.Polygon jtsPolygon, ParameterizedTexture parameterizedTexture) {
        Polygon polygon = new Polygon();
        polygon.setId(UUID.randomUUID().toString());
        var jtsExteriorRing = jtsPolygon.getExteriorRing();
        var gmlExteriorRing = createLinearRing(jtsExteriorRing);
        polygon.setExterior(new Exterior(gmlExteriorRing));

        List<AbstractRingProperty> interiorList = new ArrayList<>();
        for (var i = 0; i < jtsPolygon.getNumInteriorRing(); i++) {
            interiorList.add(new Interior(createLinearRing(jtsPolygon.getInteriorRingN(i))));
        }
        if (interiorList.size() > 0)  {
            polygon.setInterior(interiorList);
        }

        _compositeSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
        List<AbstractSurface> surfaces = new ArrayList<AbstractSurface>();
        surfaces.add(polygon);
        // boundedBy.add(new BoundarySurface(createBoundarySurface(surfaces)));

        // 保持しておいたTriangleModelからテクスチャの頂点を特定する
        List<TriangleModel> userDataList = (List<TriangleModel>)jtsPolygon.getUserData();
        if (userDataList == null) {
            System.out.println("serDataList == null ここには入ってほしくない");
            return polygon;
        }
        List<Double> textureCoordinatesList = new ArrayList<Double>();
        for (var coordinate : jtsExteriorRing.getCoordinates()) {
            var found = false;
            for (var triangleModel : userDataList) {
                if (found) break;
                for (var i = 0; i < 3. ; i++) {
                    if (found) break;
                    var vertex = triangleModel.getVertex(i);
                    if (coordinate.x == vertex.x && coordinate.y == vertex.y && coordinate.z == vertex.z) {
                        found = true;
                        var uv = triangleModel.getUV(i);
                        textureCoordinatesList.add((double)uv.x);
                        textureCoordinatesList.add((double)uv.y);
                    }
                }
            }
        }

        var any = false;
        for (var i = 0; i < textureCoordinatesList.size(); i += 2) {
            if (textureCoordinatesList.get(i) != 0 || textureCoordinatesList.get(i + 1) != 1) {
                any = true;
                break;
            }
        }
        if (!any) return polygon;

        // 有効な座標が設定されている場合はテクスチャ座標を設定する
        TextureCoordinates textureCoordinates = new TextureCoordinates();
        textureCoordinates.setRing(String.format("#%s", gmlExteriorRing.getId()));
        textureCoordinates.setValue(textureCoordinatesList);

        TexCoordList texCoordList = new TexCoordList();
        texCoordList.addTextureCoordinates(textureCoordinates);

        TextureAssociation textureAssociation = new TextureAssociation(texCoordList);
        textureAssociation.setUri(String.format("#%s", polygon.getId()));
        parameterizedTexture.addTarget(textureAssociation);

        return polygon;
    }

    private LinearRing createLinearRing(org.locationtech.jts.geom.LinearRing jtsLinearRing) {
        List<Coord> coordList = new ArrayList<>();
        for (var i = 0; i < jtsLinearRing.getNumPoints(); i++) {
            var coordinate = jtsLinearRing.getCoordinateN(i);
            var geoCoordinate = _geoReference.unproject(new Vec3f((float)coordinate.x, (float)coordinate.y, (float)coordinate.z));
            var coord = new Coord();
            coord.setX(geoCoordinate.lat);
            coord.setY(geoCoordinate.lon);
            coord.setZ(geoCoordinate.alt);
            coordList.add(coord);
        }

        LinearRing linearRing = new LinearRing();
        linearRing.setCoord(coordList);
        linearRing.setId(UUID.randomUUID().toString());

        return linearRing;
    }

    private ParameterizedTexture createOrGetParameterizedTexture(String materialName, Material material, Map<String, ParameterizedTexture> textureMap) throws IOException, URISyntaxException {
        if (textureMap.containsKey(materialName)) return textureMap.get(materialName);
        if (!(material instanceof PhongMaterial)) return null;
        PhongMaterial phongMaterial = (PhongMaterial) material;

        var image = phongMaterial.getDiffuseMap();
        var texturePath = Paths.get(new URL(image.getUrl()).toURI());
        ParameterizedTexture parameterizedTexture = new ParameterizedTexture();
        parameterizedTexture.setImageURI(texturePath.toString());
        parameterizedTexture.setMimeType(new Code(Files.probeContentType(texturePath)));
        textureMap.put(materialName, parameterizedTexture);

        return parameterizedTexture;
    }

    private void dumpInsertSql(org.locationtech.jts.geom.Polygon polygon) {
        var first = true;
        System.out.print("INSERT INTO TEST (geom) VALUES (ST_GeomFromText('POLYGON Z((");
        for (var c1 : polygon.getExteriorRing().getCoordinates()) {
            if (first) {
                first = false;
            } else {
                System.out.print(", ");
            }
            System.out.print(String.format("%f %f %f", c1.x, c1.y, c1.z));
        }
        System.out.print(")");
        for (var i = 0; i < polygon.getNumInteriorRing(); i++) {
            System.out.print(", (");
            first = true;
            for (var c1 : polygon.getInteriorRingN(i).getCoordinates()) {
                if (first) {
                    first = false;
                } else {
                    System.out.print(", ");
                }
                System.out.print(String.format("%f %f %f", c1.x, c1.y, c1.z));
            }
            System.out.print(")");
        }
        System.out.println(")', 2451));");
    }

    private void dump(Polygon polygon) {
        System.out.println(String.format("<gml:Polygon gml:id=\"%s\">", polygon.getId()));
        System.out.println("\t<gml:exterior>");
        var exterior = (LinearRing)polygon.getExterior().getRing();
        System.out.println(String.format("\t\t<gml:LinearRing gml:id=\"%s\">", exterior.getId()));
        System.out.print("\t\t\t<gml:posList>");
        var first = true;
        for (var c : exterior.getCoord()) {
            if (first) {
                first = false;
            } else {
                System.out.print(" ");
            }
            System.out.print(String.format("%f %f %f", c.getX(), c.getY(), c.getZ()));
        }
        System.out.println("</gml:posList>");
        System.out.println("\t\t</gml:LinearRing>");
        // System.out.print(")");
        // for (var i = 0; i < polygon.getNumInteriorRing(); i++) {
        //     System.out.print(", (");
        //     for (var c1 : polygon.getInteriorRingN(i).getCoordinates()) {
        //         System.out.print(String.format("%f %f %f", c1.x, c1.y, c1.z));
        //     }
        //     System.out.print(")");
        // }
        System.out.println("\t</gml:exterior>");
        System.out.println("</gml:Polygon>");
    }

    private class TriangleModel {
        private int[] _vertexIndices = new int[3];
        private int[] _uvIndices = new int[3];
        private Point3f[] _vertices = new Point3f[3];
        private Point2f[] _uvs = new Point2f[3];
        private Vector3f _normal = new Vector3f();

        public TriangleModel(ObservableFaceArray faces, int startIndex, ObservableFloatArray vertices, ObservableFloatArray uvs) {
            for (var i = 0  ; i < 3; i++) {
                _vertexIndices[i] = faces.get(startIndex + i * 2);
                _uvIndices[i] = faces.get(startIndex + i * 2 + 1);
                _vertices[i] = new Point3f(vertices.get(_vertexIndices[i] * 3), vertices.get(_vertexIndices[i] * 3 + 1), vertices.get(_vertexIndices[i] * 3 + 2));
                _uvs[i] = new Point2f(uvs.get(_uvIndices[i] * 2), 1 - uvs.get(_uvIndices[i] * 2 + 1));
            }

            Vector3f first = new Vector3f();
            first.sub(_vertices[2], _vertices[0]);
            Vector3f second = new Vector3f();
            second.sub(_vertices[2], _vertices[1]);
            _normal.cross(first, second);
            _normal.normalize();
        }

        public Point3f[] getVertices() {
            return _vertices;
        }

        public Point2f[] getUVs() {
            return _uvs;
        }

        public Vector3f getNormal() {
            return _normal;
        }

        public Point3f getVertex(int index) {
            return _vertices[index];
        }

        public Vector3f getVertexAsVector3f(int index) {
            var p = _vertices[index];
            return new Vector3f(p.x, p.y, p.z);
        }

        public float dot() {
            return getVertexAsVector3f(0).dot(getNormal());
        }

        public Point2f getUV(int index) {
            return _uvs[index];
        }
    }
}
