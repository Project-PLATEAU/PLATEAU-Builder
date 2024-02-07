package org.plateau.citygmleditor.converters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.vecmath.Point2f;

import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.builder.jaxb.CityGMLBuilderFactory;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
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
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.model.module.citygml.CoreModule;
import org.citygml4j.xml.io.CityGMLOutputFactory;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityModelWriter;
import org.locationtech.jts.algorithm.Orientation;
import org.plateau.citygmleditor.citymodel.AppearanceView;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.factory.AppearanceFactory;
import org.plateau.citygmleditor.citymodel.factory.CityObjectMemberFactory;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.converters.model.TriangleModel;
import org.plateau.citygmleditor.geometry.GeoReference;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.World;

public abstract class AbstractLodConverter {
    private org.locationtech.jts.geom.GeometryFactory _geometryFactory = new org.locationtech.jts.geom.GeometryFactory();

    private CityModelView _cityModelView;

    private BuildingView _buildingView;

    private ILODSolidView _lodSolidView;

    private AppearanceView _appearanceView;

    private CityModel _cityModel;

    private GeoReference _geoReference;

    private ArrayList<AbstractBoundarySurface> _boundedBy = new ArrayList<AbstractBoundarySurface>();

    private CompositeSurface _compositeSurface = new CompositeSurface();

    public AbstractLodConverter(CityModelView cityModelView, ILODSolidView lodSolidView) {
        _cityModelView = cityModelView;
        _lodSolidView = lodSolidView;
        _buildingView = (BuildingView)lodSolidView.getParent();
        _appearanceView = cityModelView.getRGBTextureAppearance();
        _cityModel = (CityModel) cityModelView.getGmlObject();
        _geoReference = World.getActiveInstance().getGeoReference();
    }

    protected org.locationtech.jts.geom.GeometryFactory getGeometryFactory() {
        return _geometryFactory;
    }

    protected CityModelView getCityModelView() {
        return _cityModelView;
    }

    protected BuildingView getBuildingView() {
        return _buildingView;
    }

    protected ILODSolidView getLodSolidView() {
        return _lodSolidView;
    }

    protected org.citygml4j.model.citygml.core.CityModel getCityModel() {
        return _cityModel;
    }

    protected GeoReference getGeoReference() {
        return _geoReference;
    }

    public CityModelView convert(String fileUrl) throws Exception {
        // 各フォーマット用の初期化
        initialize(fileUrl);

        if (_lodSolidView instanceof LOD1SolidView) {
            convertLOD1();
        }
        else if (_lodSolidView instanceof LOD2SolidView) {
            convertLOD2();
        } else {
            throw new IllegalArgumentException("Unsupported LOD");
        }

        return _cityModelView;
    }

    private void convertLOD1() throws Exception {
        // 各フォーマットの実装から三角形のリストを作成
        var triangleModelsMap = createTriangleModelsMap();

        for (var meshKey : triangleModelsMap.keySet()) {
            var trianglesList = triangleModelsMap.get(meshKey);

            // 三角形を結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = createPolygonList(trianglesList);

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, null, null, false);
            }
        }

        // buildingを差し替える
        var building = (AbstractBuilding) _buildingView.getGMLObject();
        BuildingView newBuildingView = createBuildingViewLOD1(building);

        _cityModelView.addCityObjectMember(newBuildingView);
        _cityModelView.removeCityObjectMember(_buildingView);
    }

    private void convertLOD2() throws Exception {
        // 各フォーマットの実装からテクスチャを作成
        var textureMap = createParameterizedTextures();

        // 各フォーマットの実装から三角形のリストを作成
        var triangleModelsMap = createTriangleModelsMap();

        // 各フォーマットの実装から地面の基準となる三角形を特定する
        var groundTriangle = getGroundTriangle(triangleModelsMap);

        for (var meshKey : triangleModelsMap.keySet()) {
            // 同一平面の三角形をグループ化
            var samePlaneTrianglesList = createSamePlaneTrianglesList(triangleModelsMap.get(meshKey), groundTriangle);

            // テクスチャの座標が離れているものを分割する
            var trianglesList = splitByTexture(samePlaneTrianglesList);
            //var trianglesList = samePlaneTrianglesList;

            // グループ化したポリゴンごとに結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = new ArrayList<>();
            for (var triangles : trianglesList) {
                jtsPolygonList.addAll(createPolygonList(triangles));
            }

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, groundTriangle, textureMap.get(meshKey), true);
            }
        }

        // ParameterizedTextureを差し替える
        var appearance = _appearanceView.getOriginal();
        var appearanceView = createAppearanceView(appearance, textureMap);
        removeTexture(appearance);
        _cityModelView.setRGBTextureAppearance(appearanceView);

        // buildingを差し替える
        var building = (AbstractBuilding) _buildingView.getGMLObject();
        var newBuildingView = createBuildingViewLOD2(building);
        _cityModelView.removeCityObjectMember(_buildingView);
        _cityModelView.addCityObjectMember(newBuildingView);
    }

    private List<List<TriangleModel>> createSamePlaneTrianglesList(List<TriangleModel> triangleModels, TriangleModel groundTriangle) {
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
            if (sameNormalList.size() > 0) {
                sameTrianglesList.add(sameNormalList);

                // 三角形のリストから削除
                for (var sameTriangle : sameNormalList) {
                    triangleModels.remove(sameTriangle);
                }
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

        return sameTrianglesList;
    }

    private List<List<TriangleModel>> splitByTexture(List<List<TriangleModel>> trianglesList) {
        List<List<TriangleModel>> result = new ArrayList<>();

        for (var triangles : trianglesList) {
            while (triangles.size() > 0) {
                // 最初に見つかった三角形のテクスチャの頂点を保持
                var baseTriangle = triangles.get(0);
                List<TriangleModel> sameTextureList = new ArrayList<>();
                sameTextureList.add(baseTriangle);
                Set<Point2f> textureCoordinateSet = new HashSet<>();
                addTextureCoordinates(textureCoordinateSet, baseTriangle);
                triangles.remove(0);

                // 1つでも見つかればループする
                boolean found;
                do {
                    found = false;
                    for (var triangle : triangles) {
                        // 2点を共有しているものを探す
                        var count = 0;
                        for (var i = 0; i < 3; i++) {
                            var uv = triangle.getUV(i);
                            if (textureCoordinateSet.contains(uv)) {
                                count++;
                            }
                        }
                        if (count >= 2) {
                            sameTextureList.add(triangle);
                            addTextureCoordinates(textureCoordinateSet, triangle);
                            triangles.remove(triangle);
                            found = true;
                            break;
                        }
                    }
                } while (found);

                // 見つかったものをグループとする
                if (sameTextureList.size() > 0) {
                    result.add(sameTextureList);
                }
            }
        }

        return result;
    }

    private void addTextureCoordinates(Set<Point2f> textureCoordinateSet, TriangleModel triangle) {
        for (var i = 0; i < 3; i++) {
            var uv = triangle.getUV(i);
            if (!textureCoordinateSet.contains(uv)) {
                textureCoordinateSet.add(uv);
            }
        }
    }

    private List<org.locationtech.jts.geom.Polygon> createPolygonList(List<TriangleModel> triangleList) {
        var isInitCCW = false;
        boolean isTriangleCCW = false;
        List<org.locationtech.jts.geom.Polygon> trianglePolygonList = new ArrayList<>();
        var geometryFactory = getGeometryFactory();
        for (var triangleModel : triangleList) {
            var p1 = triangleModel.getVertex(0);
            var p2 = triangleModel.getVertex(1);
            var p3 = triangleModel.getVertex(2);
            var linearRing = geometryFactory.createLinearRing(
                        new org.locationtech.jts.geom.Coordinate[] {
                            new org.locationtech.jts.geom.Coordinate(p1.x, p1.y, p1.z),
                            new org.locationtech.jts.geom.Coordinate(p2.x, p2.y, p2.z),
                            new org.locationtech.jts.geom.Coordinate(p3.x, p3.y, p3.z),
                            new org.locationtech.jts.geom.Coordinate(p1.x, p1.y, p1.z)
                        });

            // 不正なポリゴンは除外する
            var polygon = geometryFactory.createPolygon(linearRing, null);
            if (!polygon.isValid()) continue;

            // PolygonにuserDataとしてTriangleModelを保持しておく
            polygon.setUserData(triangleModel);
            trianglePolygonList.add(polygon);
            if (!isInitCCW) {
                // 初回はCCW判定を行う
                isTriangleCCW = Orientation.isCCW(linearRing.getCoordinateSequence());
                isInitCCW = true;
            }
        }

        if (trianglePolygonList.size() == 0) {
            return Collections.emptyList();
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

        for (var polygon : polygonList) {
            var notFoundList = new ArrayList<org.locationtech.jts.geom.Coordinate>();
            for (var coordinate : polygon.getExteriorRing().getCoordinates()) {
                var sameCoordinatePolygon = getSameCoordinatePolygon(coordinate, trianglePolygonList);
                if (sameCoordinatePolygon != null) {
                    addUserData(polygon, sameCoordinatePolygon);
                } else {
                    notFoundList.add(coordinate);
                }
            }
            for (var i = 0; i < polygon.getNumInteriorRing(); i++) {
                for (var coordinate : polygon.getInteriorRingN(i).getCoordinates()) {
                    var sameCoordinatePolygon = getSameCoordinatePolygon(coordinate, trianglePolygonList);
                    if (sameCoordinatePolygon != null) {
                        addUserData(polygon, sameCoordinatePolygon);
                    } else {
                        notFoundList.add(coordinate);
                    }
                }
            }

            for (var coordinate : notFoundList) {
                var nearPolygon = getNearPolygon(coordinate, trianglePolygonList);
                addUserData(polygon, nearPolygon);
            }
        }

        return polygonList;
    }

    private org.locationtech.jts.geom.Polygon getSameCoordinatePolygon(org.locationtech.jts.geom.Coordinate coordinate, List<org.locationtech.jts.geom.Polygon> trianglePolygonList) {
        for (var trianglePolygon : trianglePolygonList) {
            if (hasSameCoordinate(coordinate, trianglePolygon)) {
                return trianglePolygon;
            }
        }
        return null;
    }

    private boolean hasSameCoordinate(org.locationtech.jts.geom.Coordinate coordinate, org.locationtech.jts.geom.Polygon polygon) {
        for (var polygonCoordinate : polygon.getExteriorRing().getCoordinates()) {
            if (coordinate.x == polygonCoordinate.x && coordinate.y == polygonCoordinate.y && coordinate.z == polygonCoordinate.z) {
                return true;
            }
        }
        return false;
    }

    private org.locationtech.jts.geom.Polygon getNearPolygon(org.locationtech.jts.geom.Coordinate coordinate, List<org.locationtech.jts.geom.Polygon> trianglePolygonList) {
        org.locationtech.jts.geom.Polygon nearPolygon = null;
        double minDistance = Double.MAX_VALUE;
        for (var trianglePolygon : trianglePolygonList) {
            for (var polygonCoordinate : trianglePolygon.getExteriorRing().getCoordinates()) {
                var distance = polygonCoordinate.distance(coordinate);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearPolygon = trianglePolygon;
                }
            }
        }
        //System.out.println(String.format("minDistance:%f", minDistance));
        return nearPolygon;
    }

    private List<org.locationtech.jts.geom.Polygon> splitJtsPolygon(org.locationtech.jts.geom.Geometry geometry) {
        List<org.locationtech.jts.geom.Polygon> polygonList = new ArrayList<>();
        try {
            if (geometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
                var original = (org.locationtech.jts.geom.Polygon)geometry;
                org.locationtech.jts.geom.LinearRing shellCopy = (org.locationtech.jts.geom.LinearRing)original.getExteriorRing().copy();
                org.locationtech.jts.geom.LinearRing[] holeCopies = new org.locationtech.jts.geom.LinearRing[original.getNumInteriorRing()];
                for (int i = 0; i < holeCopies.length; i++) {
                    holeCopies[i] = (org.locationtech.jts.geom.LinearRing)original.getInteriorRingN(i).copy();
                }
                polygonList.add(getGeometryFactory().createPolygon(shellCopy, holeCopies));
            } else if (geometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
                for (var i = 0; i < geometry.getNumGeometries(); i++) {
                    polygonList.add((org.locationtech.jts.geom.Polygon)geometry.getGeometryN(i));
                }
            } else {
                System.out.println(String.format("Error splitJtsPolygon: GeometryType:%s", geometry.getGeometryType()));
            }

            return polygonList;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return polygonList;
        }
    }

    private org.locationtech.jts.geom.Polygon matchPolygonFace(boolean isTriangleCCW, org.locationtech.jts.geom.Polygon polygon) {
        polygon.normalize();
        var isPolygonCCW = Orientation.isCCW(polygon.getExteriorRing().getCoordinateSequence());
        var matchPolygon = isPolygonCCW != isTriangleCCW ? polygon.reverse() : polygon;
        if (matchPolygon.getNumInteriorRing() > 0) {
            var isExteriorCCW = Orientation.isCCW(matchPolygon.getExteriorRing().getCoordinateSequence());
            List<org.locationtech.jts.geom.LinearRing> interiorRingList = new ArrayList<>();
            for (var i = 0; i < matchPolygon.getNumInteriorRing(); i++) {
                var linearRing = matchPolygon.getInteriorRingN(i);
                if (Orientation.isCCW(linearRing.getCoordinateSequence()) != isExteriorCCW) {
                    interiorRingList.add(linearRing);
                } else {
                    interiorRingList.add(linearRing.reverse());
                }
            }
            matchPolygon = getGeometryFactory().createPolygon(matchPolygon.getExteriorRing(), interiorRingList.toArray(new org.locationtech.jts.geom.LinearRing[0]));
        }

        return matchPolygon;
    }

    private void toGmlPolygonList(org.locationtech.jts.geom.Geometry jtsGeometry, TriangleModel groundTriangle, ParameterizedTexture texture, boolean isCreateId) {
        if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
            createPolygon((org.locationtech.jts.geom.Polygon)jtsGeometry, groundTriangle, texture, isCreateId);
        } else if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
            for (var i = 0; i < jtsGeometry.getNumGeometries(); i++) {
                toGmlPolygonList(jtsGeometry.getGeometryN(i), groundTriangle, texture, isCreateId);
            }
        } else {
            System.out.println(String.format("Error toGmlPolygonList: GeometryType:%s", jtsGeometry.getGeometryType()));
        }
    }

    private Polygon createPolygon(org.locationtech.jts.geom.Polygon jtsPolygon, TriangleModel groundTriangle, ParameterizedTexture texture, boolean isCreateId) {
        Polygon polygon = new Polygon();
        polygon.setId(String.format("poly_%s", UUID.randomUUID().toString()));
        var jtsExteriorRing = jtsPolygon.getExteriorRing();
        var gmlExteriorRing = createLinearRing(jtsExteriorRing, isCreateId);
        polygon.setExterior(new Exterior(gmlExteriorRing));

        List<AbstractRingProperty> interiorList = new ArrayList<>();
        for (var i = 0; i < jtsPolygon.getNumInteriorRing(); i++) {
            interiorList.add(new Interior(createLinearRing(jtsPolygon.getInteriorRingN(i), isCreateId)));
        }
        if (interiorList.size() > 0)  {
            polygon.setInterior(interiorList);
        }

        // LODの種別ごとにSurfaceを構築する
        if (_lodSolidView instanceof LOD1SolidView) {
            applyLOD1Surface(polygon);
        }
        else if (_lodSolidView instanceof LOD2SolidView) {
            applyLOD2Surface(polygon, groundTriangle, texture, jtsPolygon);
        } else {
            throw new IllegalArgumentException("Unsupported LOD");
        }

        return polygon;
    }

    private void applyLOD1Surface(Polygon polygon) {
        _compositeSurface.addSurfaceMember(new SurfaceProperty(polygon));
    }

    //private static boolean isDebug = false;

    private void applyLOD2Surface(Polygon polygon, TriangleModel groundTriangle, ParameterizedTexture texture, org.locationtech.jts.geom.Polygon jtsPolygon) {
        // lod2Solid
        // 保持しておいたTriangleModelを取得する
        List<TriangleModel> userDataList = (List<TriangleModel>)jtsPolygon.getUserData();
        if (userDataList == null) {
            throw new IllegalArgumentException("userDataList == null");
        }

        _compositeSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
        _boundedBy.add(createBoundarySurface(polygon, userDataList.get(0), groundTriangle));

        if (texture == null) {
            return;
        }

        // if (jtsPolygon.getExteriorRing().getCoordinates().length == 12 && jtsPolygon.getNumInteriorRing() == 1) {
        //     isDebug = true;
        // }

        TexCoordList texCoordList = new TexCoordList();
        var textureCoordinates = createTextureCoordinates(polygon.getExterior().getRing(), texture, jtsPolygon.getExteriorRing(), userDataList);
        if (textureCoordinates != null) {
            texCoordList.addTextureCoordinates(textureCoordinates);
        }
        for (var i = 0; i < polygon.getInterior().size(); i++) {
            var interiorTextureCoordinates = createTextureCoordinates(polygon.getInterior().get(i).getRing(), texture, jtsPolygon.getInteriorRingN(i), userDataList);
            if (interiorTextureCoordinates != null) {
                texCoordList.addTextureCoordinates(interiorTextureCoordinates);
            }
        }
        //isDebug = false;

        TextureAssociation textureAssociation = new TextureAssociation(texCoordList);
        textureAssociation.setUri(String.format("#%s", polygon.getId()));
        texture.addTarget(textureAssociation);
    }

    private TextureCoordinates createTextureCoordinates(AbstractRing linearRing, ParameterizedTexture texture, org.locationtech.jts.geom.LinearRing jtsLinearRing, List<TriangleModel> userDataList) {
        // if (isDebug) {
        //     System.out.println("linearRing");
        //     for (var coordinate : jtsLinearRing.getCoordinates()) {
        //         System.out.println(String.format("%f,%f,%f", coordinate.x, coordinate.y, coordinate.z));
        //     }
        // }
        List<Double> textureCoordinatesList = new ArrayList<Double>();
        for (var coordinate : jtsLinearRing.getCoordinates()) {
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
                    }
                }

                // 最も距離が近い頂点を採用する
                textureCoordinatesList.add((double)minDistanceUv.x);
                textureCoordinatesList.add((double)minDistanceUv.y);
                System.out.println(String.format("minDistance:%f", minDistance));
            }
        }

        // if (isDebug) {
        //     System.out.println("textureCoordinatesList");
        //     for (var i = 0; i < textureCoordinatesList.size(); i += 2) {
        //         System.out.println(String.format("%f,%f", textureCoordinatesList.get(i), textureCoordinatesList.get(i + 1)));
        //     }
        // }

        // 全ての座標が (0, 1) の場合はテクスチャがないため、TextureAssociationを追加しない
        var any = false;
        for (var i = 0; i < textureCoordinatesList.size(); i += 2) {
            if (textureCoordinatesList.get(i) != 0 || textureCoordinatesList.get(i + 1) != 1) {
                any = true;
                break;
            }
        }
        if (!any) return null;

        // 有効な座標が設定されている場合はテクスチャ座標を設定する
        TextureCoordinates textureCoordinates = new TextureCoordinates();
        textureCoordinates.setRing(String.format("#%s", linearRing.getId()));
        textureCoordinates.setValue(textureCoordinatesList);

        return textureCoordinates;
    }

    private LinearRing createLinearRing(org.locationtech.jts.geom.LinearRing jtsLinearRing, boolean isCreateId) {
        DirectPositionList directPositionList = new DirectPositionList();
        var geoReference = getGeoReference();
        for (var i = 0; i < jtsLinearRing.getNumPoints(); i++) {
            var coordinate = jtsLinearRing.getCoordinateN(i);
            var geoCoordinate = geoReference.unproject(new Vec3f((float)coordinate.x, (float)coordinate.y, (float)coordinate.z));
            directPositionList.addValue(geoCoordinate.lat);
            directPositionList.addValue(geoCoordinate.lon);
            directPositionList.addValue(geoCoordinate.alt);
        }

        LinearRing linearRing = new LinearRing();
        linearRing.setPosList(directPositionList);
        if (isCreateId) {
            linearRing.setId(String.format("line_%s", UUID.randomUUID().toString()));
        }

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
                boundarySurface.setId(String.format("gnd_%s", UUID.randomUUID().toString()));
            } else {
                boundarySurface = new OuterCeilingSurface();
                boundarySurface.setId(String.format("ceil_%s", UUID.randomUUID().toString()));
            }
        } else if (angle >= 90 - threshold && angle <= 90 + threshold) {
            boundarySurface = new WallSurface();
            boundarySurface.setId(String.format("wall_%s", UUID.randomUUID().toString()));
        } else if (angle > 90 + threshold) {
            boundarySurface = new RoofSurface();
            boundarySurface.setId(String.format("roof_%s", UUID.randomUUID().toString()));
        } else {
            throw new IllegalArgumentException(String.format("angle is invalid. angle: %f", angle));
        }

        List<AbstractSurface> surfaces = new ArrayList<AbstractSurface>();
        surfaces.add(polygon);
        boundarySurface.setLod2MultiSurface(new MultiSurfaceProperty(new MultiSurface(surfaces)));

        return boundarySurface;
    }

    private void addUserData(org.locationtech.jts.geom.Polygon polygon, org.locationtech.jts.geom.Polygon trianglePolygon) {
        if (polygon.getUserData() == null) {
            polygon.setUserData(new ArrayList<TriangleModel>());
        }
        List<TriangleModel> userDataList = (List<TriangleModel>)polygon.getUserData();
        userDataList.add((TriangleModel)trianglePolygon.getUserData());
    }

    private void removeTexture(Appearance appearance) {
        var lod2SolidView = (LOD2SolidView)_lodSolidView;
        var solid = (Solid)lod2SolidView.getGmlObject();
        var compositeSurface = (CompositeSurface)solid.getExterior().getObject();

        // もともと参照していたテクスチャを削除する
        var hrefSet = new HashSet<String>();
        for (var surfaceMember : compositeSurface.getSurfaceMember()) {
            hrefSet.add(surfaceMember.getHref());
        }
        removeTexture(appearance, hrefSet);
    }

    private void removeTexture(Appearance appearance, Set<String> hrefSet) {
        var removeTextureList = new ArrayList<ParameterizedTexture>();
        var surfaceDataMemberList = appearance.getSurfaceDataMember();
        for (var surfaceDataMember : surfaceDataMemberList) {
            if (surfaceDataMember.getSurfaceData() instanceof ParameterizedTexture) {
                // LOD2SolidのCompositeSurfaceのhrefと同じURIを持つParameterizedTextureを特定する
                ParameterizedTexture original = (ParameterizedTexture)surfaceDataMember.getSurfaceData();
                for (var originalTarget : original.getTarget()) {
                    if (hrefSet.contains(originalTarget.getUri())) {
                        removeTextureList.add(original);
                        break;
                    }
                }
            }
        }

        for (var removeTexture : removeTextureList) {
            surfaceDataMemberList.removeIf(surfaceDataMember -> surfaceDataMember.getSurfaceData() == removeTexture);
        }
    }

    private AppearanceView createAppearanceView(Appearance appearance, Map<String, ParameterizedTexture> textureMap) {
        var surfaceDataProperty = new SurfaceDataProperty();
        for (var texture : textureMap.values()) {
            surfaceDataProperty.setSurfaceData(texture);
            appearance.getSurfaceDataMember().add(surfaceDataProperty);
        }

        var appearanceMember = new AppearanceMember();
        appearanceMember.setAppearance(appearance);

        var appearanceFactory = new AppearanceFactory(_cityModelView);
        return appearanceFactory.createAppearance(appearanceMember);
    }

    private BuildingView createBuildingViewLOD1(AbstractBuilding building) {
        // lod1Solid
        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(_compositeSurface));
        SolidProperty solidProperty = new SolidProperty(solid);
        building.setLod1Solid(solidProperty);

        return new CityObjectMemberFactory(_cityModelView).createBuilding(building);
    }

    private BuildingView createBuildingViewLOD2(AbstractBuilding building) {
        // lod2Solid
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

        return new CityObjectMemberFactory(_cityModelView).createBuilding(building);
    }

    private void dump(AbstractFeature feature, String name) throws CityGMLBuilderException, CityGMLWriteException {
        CityGMLBuilder builder = CityGMLBuilderFactory.defaults().build();
        CityGMLOutputFactory out = builder.createCityGMLOutputFactory();
        CityModelWriter writer = out.createCityModelWriter(new File(String.format("E:\\Temp\\export\\gml\\udx\\bldg\\%s.gml", name)));
		writer.setPrefixes(CityGMLVersion.v2_0_0);
		writer.setDefaultNamespace(CoreModule.v2_0_0);
		writer.setSchemaLocations(CityGMLVersion.v2_0_0);
		writer.setIndentString("  ");
		writer.writeStartDocument();
        writer.writeFeatureMember(feature);
		writer.close();
    }

    abstract protected void initialize(String fileUrl) throws Exception;

    abstract protected Map<String, ParameterizedTexture> createParameterizedTextures() throws IOException, URISyntaxException;

    abstract protected Map<String, List<TriangleModel>> createTriangleModelsMap();

    abstract protected TriangleModel getGroundTriangle(Map<String, List<TriangleModel>> triangleModelsMap);
}