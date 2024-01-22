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

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.OuterFloorSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.locationtech.jts.algorithm.Orientation;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.factory.CityModelFactory;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.converters.model.TriangleModel;
import org.plateau.citygmleditor.geometry.GeoReference;
import org.plateau.citygmleditor.importers.obj.ObjImporter;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.World;

import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.TriangleMesh;

public class Obj2LodConverter {
    private org.locationtech.jts.geom.GeometryFactory _geometryFactory = new org.locationtech.jts.geom.GeometryFactory();

    private ArrayList<AbstractBoundarySurface> _boundedBy = new ArrayList<AbstractBoundarySurface>();

    private CompositeSurface _compositeSurface = new CompositeSurface();

    private CityModelView _cityModelView;

    private ILODSolidView _lodSolidView;

    private org.citygml4j.model.citygml.core.CityModel _cityModel;

    private GeoReference _geoReference;

    public Obj2LodConverter(CityModelView cityModelView, ILODSolidView lodSolidView) {
        _cityModelView = cityModelView;
        _lodSolidView = lodSolidView;
        _cityModel = (org.citygml4j.model.citygml.core.CityModel) cityModelView.getGmlObject();
        _geoReference = World.getActiveInstance().getGeoReference();
    }

    public CityModelView convert(String fileUrl) throws Exception {
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
            var groundBaseTriangle = TriangleModel.CreateGroundTriangle();
            TriangleModel groundTriangle = null;
            for (var i = 0; i < faces.size(); i += 6) {
                var triangleModel = new TriangleModel(faces, i, vertices, coords);

                // 不正な三角形は除外する
                if (triangleModel.isValid()) {
                    triangleModels.add(triangleModel);

                    // 同時に地面の基準になる三角形を特定する
                    // 基準となる三角形の平面との角度が180±5度以下のもののうち、最もZ座標が小さいものを採用する(GroundSurfaceの法線は地面の方を向いている)
                    double angle = Math.toDegrees(groundBaseTriangle.getNormal().angle(triangleModel.getNormal()));
                    if (angle >= 175 && angle <= 185) {
                        if (groundTriangle == null) {
                            groundTriangle = triangleModel;
                        } else {
                            if (triangleModel.getMinZ() < groundTriangle.getMinZ()) {
                                groundTriangle = triangleModel;
                            }
                        }
                    }
                }
                else {
                    var p1 = triangleModel.getVertex(0);
                    var p2 = triangleModel.getVertex(1);
                    var p3 = triangleModel.getVertex(2);
                    System.out.println(String.format("%f %f %f, %f %f %f, %f %f %f", p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z));
                }
            }

            List<List<TriangleModel>> sameTrianglesList = new ArrayList<>();
            {
                List<TriangleModel> sameNormalList = new ArrayList<>();
                for (var i = 1; i < triangleModels.size(); i++) {

                    // 精度向上のためGroundSurfaceは特別扱いして先に判定する
                    // 地面の基準となる三角形の平面との角度が0度で、平面が同じものをグループ化
                    var triangleModel = triangleModels.get(i);
                    double angle = Math.toDegrees(groundTriangle.getNormal().angle(triangleModel.getNormal()));
                    if (angle == 0 && groundTriangle.isSamePlane(triangleModel))  {
                        sameNormalList.add(triangleModel);
                    }
                }
                sameTrianglesList.add(sameNormalList);

                // 三角形のリストから削除
                for (var sameTriangle : sameNormalList) {
                    triangleModels.remove(sameTriangle);
                }
            }

            // 三角形のリストが空になるまでループ
            while (triangleModels.size() > 0) {
                var baseTriangle = triangleModels.get(0);
                List<TriangleModel> sameNormalList = new ArrayList<>();
                sameNormalList.add(baseTriangle);

                // 最初に見つかった三角形を基準として、法線ベクトルと平面が同じ三角形をグループ化
                for (var i = 1; i < triangleModels.size(); i++) {
                    var triangleModel = triangleModels.get(i);
                    var angle = baseTriangle.getNormal().angle(triangleModel.getNormal());
                    if (angle == 0 && baseTriangle.isSamePlane(triangleModel))  {
                        sameNormalList.add(triangleModel);
                    }
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
            System.out.println(String.format("Polygon count: %d", jtsPolygonList.size()));

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, groundTriangle, parameterizedTexture);
            }

            // ParameterizedTextureを差し替える
            // このメソッドの中でlod2Solidを参照しているため、lod2Solidより先に実行する必要がある
            replaceParameterizedTexture(cityModel, parameterizedTexture);

            // lod2Solidを差し替える
            replaceLod2Solid(cityModel);

            // For:DEBUG
            //GmlExporter.export(Paths.get("E:\\Temp\\export\\gml\\udx\\bldg\\53392633_bldg_6697_2_op.gml").toString(), cityModel, _cityModelView.getSchemaHandler());
        }

        var cityModelFactory = new CityModelFactory();
        var cityModelView = cityModelFactory.createCityModel(cityModel, _cityModelView.getGmlPath(), _cityModelView.getSchemaHandler());

        return cityModelView;
    }

    private List<org.locationtech.jts.geom.Polygon> createPolygonList(List<TriangleModel> triangleList) {
        var isInitCCW = false;
        boolean isTriangleCCW = false;
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
            if (!isInitCCW) {
                // 初回はCCW判定を行う
                isTriangleCCW = Orientation.isCCW(linearRing.getCoordinateSequence());
                isInitCCW = true;
            }
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
        List<org.locationtech.jts.geom.Polygon> polygonList = new ArrayList<>();
        for (var polygon : splitJtsPolygon(geometry)) {
            // ポリゴンの法線が変わっている可能性があるため三角形の法線に合わせる
            polygonList.add(matchPolygonFace(isTriangleCCW, polygon));
        }

        if (polygonList.size() == 1) {
            // ポリゴンが1つなら判定不要
            var polygon = polygonList.get(0);
            for (var trianglePolygon : trianglePolygonList) {
                addUserData(polygon, trianglePolygon);
            }
        } else {
            // 元のTriangleが、分割したPolygonに内包されるかどうかを判定
            // 内包される場合は、そのTriangleModelのuserDataを保持する
            for (var trianglePolygon : trianglePolygonList) {
                var found = false;
                for (var polygon : polygonList) {
                    if (polygon.contains(trianglePolygon) || polygon.equalsExact(trianglePolygon)) {
                        addUserData(polygon, trianglePolygon);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // 内包判定に失敗したら距離で判定する
                    for (var polygon : polygonList) {
                        var distance = polygon.distance(trianglePolygon);
                        //System.out.println(String.format("polygon distance:%f", distance));
                        if (distance <= 0.003) {
                            addUserData(polygon, trianglePolygon);
                            found = true;
                            break;
                        }
                    }
                }
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

    private org.locationtech.jts.geom.Polygon matchPolygonFace(boolean isTriangleCCW, org.locationtech.jts.geom.Polygon polygon) {
        polygon.normalize();
        var isPolygonCCW = Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence());
        var matchPolygon = isPolygonCCW != isTriangleCCW ? polygon.reverse() : polygon;
        if (isPolygonCCW != isTriangleCCW) {
            if (matchPolygon.getNumInteriorRing() > 0) {
                List<org.locationtech.jts.geom.LinearRing> interiorRingList = new ArrayList<>();
                for (var i = 0; i < matchPolygon.getNumInteriorRing(); i++) {
                    var linearRing = matchPolygon.getInteriorRingN(i);
                    if (Orientation.isCCW(linearRing.getCoordinateSequence()) == isPolygonCCW) {
                        interiorRingList.add(linearRing);
                    } else {
                        interiorRingList.add(linearRing.reverse());
                    }
                }
                matchPolygon = _geometryFactory.createPolygon(matchPolygon.getExteriorRing(), interiorRingList.toArray(new org.locationtech.jts.geom.LinearRing[0]));
            }
        }

        return matchPolygon;
    }

    private void toGmlPolygonList(org.locationtech.jts.geom.Geometry jtsGeometry, TriangleModel groundTriangle, ParameterizedTexture parameterizedTexture) {
        if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
            createPolygon((org.locationtech.jts.geom.Polygon)jtsGeometry, groundTriangle, parameterizedTexture);
        } else if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
            for (var i = 0; i < jtsGeometry.getNumGeometries(); i++) {
                toGmlPolygonList(jtsGeometry.getGeometryN(i), groundTriangle, parameterizedTexture);
            }
        } else {
            System.out.println(String.format("Error toGmlPolygonList: GeometryType:%s", jtsGeometry.getGeometryType()));
        }
    }

    private Polygon createPolygon(org.locationtech.jts.geom.Polygon jtsPolygon, TriangleModel groundTriangle, ParameterizedTexture parameterizedTexture) {
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

        // 保持しておいたTriangleModelを取得する
        List<TriangleModel> userDataList = (List<TriangleModel>)jtsPolygon.getUserData();
        if (userDataList == null) {
            throw new IllegalArgumentException("userDataList == null");
        }

        _boundedBy.add(createBoundarySurface(polygon, userDataList.get(0), groundTriangle));

        // テクスチャの頂点を特定する
        List<Double> textureCoordinatesList = new ArrayList<Double>();
        for (var coordinate : jtsExteriorRing.getCoordinates()) {
            var found = false;
            for (var triangleModel : userDataList) {
                if (found) break;
                for (var i = 0; i < 3. ; i++) {
                    if (found) break;
                    var vertex = triangleModel.getVertex(i);
                    if (coordinate.x == vertex.x && coordinate.y == vertex.y && coordinate.z == vertex.z) {
                        var uv = triangleModel.getUV(i);
                        textureCoordinatesList.add((double)uv.x);
                        textureCoordinatesList.add((double)uv.y);
                        found = true;
                    }
                }
            }
            if (!found) {
                System.out.println("not found textureCoord → search by distance");

                // 同一頂点が見つからなかったら距離で判定する
                var minDistance = Double.MAX_VALUE;
                Point2f minDistanceUv = null;
                for (var triangleModel : userDataList) {
                    for (var i = 0; i < 3. ; i++) {
                        var vertex = triangleModel.getVertex(i);
                        var vertexCoord = new org.locationtech.jts.geom.Coordinate(vertex.x, vertex.y, vertex.z);
                        var distance = vertexCoord.distance(coordinate);
                        if (distance < minDistance) {
                            minDistance = distance;
                            minDistanceUv = triangleModel.getUV(i);
                        }
                        minDistance = Math.min(minDistance, distance);
                    }
                }

                // 最も距離が近い頂点を採用する
                textureCoordinatesList.add((double)minDistanceUv.x);
                textureCoordinatesList.add((double)minDistanceUv.y);
                System.out.println(String.format("minDistance:%f", minDistance));
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
        DirectPositionList directPositionList = new DirectPositionList();
        for (var i = 0; i < jtsLinearRing.getNumPoints(); i++) {
            var coordinate = jtsLinearRing.getCoordinateN(i);
            var geoCoordinate = _geoReference.unproject(new Vec3f((float)coordinate.x, (float)coordinate.y, (float)coordinate.z));
            directPositionList.addValue(geoCoordinate.lat);
            directPositionList.addValue(geoCoordinate.lon);
            directPositionList.addValue(geoCoordinate.alt);
        }

        LinearRing linearRing = new LinearRing();
        linearRing.setPosList(directPositionList);
        linearRing.setId(UUID.randomUUID().toString());

        return linearRing;
    }

    private AbstractBoundarySurface createBoundarySurface(Polygon polygon, TriangleModel triangle, TriangleModel groundTriangle) {
        AbstractBoundarySurface boundarySurface = null;

        // 90度を基準に±何度までを壁とするかの閾値
        double threshold = 80;

        // 閾値を使ってSurfaceの種類を決定する
        double angle = Math.toDegrees(groundTriangle.getNormal().angle(triangle.getNormal()));
        if (angle < 90 - threshold) {
            var isSamePlane = groundTriangle.isSamePlane(triangle);
            if (isSamePlane) {
                boundarySurface = new GroundSurface();
                boundarySurface.setId(String.format("gnd_%s", polygon.getId()));
            } else {
                boundarySurface = new OuterFloorSurface();
                boundarySurface.setId(String.format("outerfloor_%s", polygon.getId()));
            }
        } else if (angle >= 90 - threshold && angle <= 90 + threshold) {
            boundarySurface = new WallSurface();
            boundarySurface.setId(String.format("wall_%s", polygon.getId()));
        } else if (angle > 90 + threshold) {
            boundarySurface = new RoofSurface();
            boundarySurface.setId(String.format("roof_%s", polygon.getId()));
        } else {
            throw new IllegalArgumentException("angle is invalid");
        }

        List<AbstractSurface> surfaces = new ArrayList<AbstractSurface>();
        surfaces.add(polygon);
        boundarySurface.setLod2MultiSurface(new MultiSurfaceProperty(new MultiSurface(surfaces)));

        return boundarySurface;
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

    private void addUserData(org.locationtech.jts.geom.Polygon polygon, org.locationtech.jts.geom.Polygon trianglePolygon) {
        if (polygon.getUserData() == null) {
            polygon.setUserData(new ArrayList<TriangleModel>());
        }
        List<TriangleModel> userDataList = (List<TriangleModel>)polygon.getUserData();
        userDataList.add((TriangleModel)trianglePolygon.getUserData());
    }

    private void replaceParameterizedTexture(CityModel cityModel, ParameterizedTexture newParameterizedTexture) {
        var lod2SolidView = (LOD2SolidView)_lodSolidView;
        var solid = (Solid)lod2SolidView.getGmlObject();
        var compositeSurface = (CompositeSurface)solid.getExterior().getObject();
        for (var surfaceMember : compositeSurface.getSurfaceMember()) {
            if (replaceParameterizedTexture(cityModel, newParameterizedTexture, surfaceMember.getHref())) {
                return;
            }
        }
    }

    private boolean replaceParameterizedTexture(CityModel cityModel, ParameterizedTexture newParameterizedTexture, String targetUri) {
        for (var appearanceMember : cityModel.getAppearanceMember()) {
            for (var surfaceDataMember : appearanceMember.getAppearance().getSurfaceDataMember()) {
                if (surfaceDataMember.getSurfaceData() instanceof ParameterizedTexture) {
                    // LOD2SolidのCompositeSurfaceのhrefと同じURIを持つParameterizedTextureを特定する
                    ParameterizedTexture original = (ParameterizedTexture)surfaceDataMember.getSurfaceData();
                    for (var originalTarget : original.getTarget()) {
                        if (originalTarget.getUri().equals(targetUri)) {
                            surfaceDataMember.setSurfaceData(newParameterizedTexture);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void replaceLod2Solid(CityModel cityModel) {
        // lod2Solid
        BuildingView buildingView = (BuildingView)_lodSolidView.getParent();
        Building original = (Building)buildingView.getGMLObject();

        Building building = null;
        for (CityObjectMember cityObjectMember : cityModel.getCityObjectMember()) {
            AbstractCityObject cityObject = cityObjectMember.getCityObject();
            if (cityObject.getCityGMLClass() != CityGMLClass.BUILDING) continue;

            var b = (Building)cityObject;
            if (original.getId().equals(b.getId())) {
                building = b;
                break;
            }
        }

        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(_compositeSurface));
        SolidProperty solidProperty = new SolidProperty(solid);
        building.setLod2Solid(solidProperty);

        // boundedBy
        var boundedBySurface = building.getBoundedBySurface();
        boundedBySurface.clear();
        for (var boundarySurface : _boundedBy) {
            boundedBySurface.add(new BoundarySurfaceProperty(boundarySurface));
        }
    }
}
