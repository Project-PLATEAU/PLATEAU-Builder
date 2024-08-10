package org.plateaubuilder.core.io.mesh.converters;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.vecmath.Point2f;

import org.apache.commons.lang3.NotImplementedException;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.SurfaceDataProperty;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.appearance.TextureCoordinates;
import org.citygml4j.model.citygml.appearance.X3DMaterial;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.gml.geometry.primitives.AbstractRing;
import org.citygml4j.model.gml.geometry.primitives.AbstractRingProperty;
import org.citygml4j.model.gml.geometry.primitives.DirectPositionList;
import org.citygml4j.model.gml.geometry.primitives.Exterior;
import org.citygml4j.model.gml.geometry.primitives.Interior;
import org.citygml4j.model.gml.geometry.primitives.LinearRing;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.locationtech.jts.algorithm.Orientation;
import org.plateaubuilder.core.citymodel.AppearanceView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.factory.AppearanceFactory;
import org.plateaubuilder.core.geospatial.GeoReference;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;
import org.plateaubuilder.core.utils3d.geom.Vec3d;
import org.plateaubuilder.core.utils3d.geom.Vec3f;
import org.plateaubuilder.core.world.World;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * 抽象的なLOD（Level of Detail）コンバータの基底クラスです。 TはIFeatureViewのサブクラス、TGMLはAbstractCityObjectのサブクラスを指定します。
 */
abstract public class AbstractLODConverter<T extends IFeatureView, TGML extends AbstractCityObject> {
    private org.locationtech.jts.geom.GeometryFactory _geometryFactory = new org.locationtech.jts.geom.GeometryFactory();

    private CityModel _cityModel;

    private CityModelView _cityModelView;

    private T _featureView;

    private int _lod;

    private ConvertOption _convertOption;

    private AppearanceView _appearanceView;

    private GeoReference _geoReference;

    private Abstract3DFormatHandler _formatHandler;

    private List<Polygon> _polygons = new ArrayList<>();

    public AbstractLODConverter(CityModelView cityModelView, T featureView, int lod, ConvertOption convertOption, Abstract3DFormatHandler formatHandler) {
        _cityModelView = cityModelView;
        // _lodSolidView = lodSolidView;
        _lod = lod;
        _convertOption = convertOption;
        _featureView = featureView;
        _appearanceView = cityModelView.getAppearance();
        _cityModel = (CityModel) cityModelView.getGML();
        _geoReference = World.getActiveInstance().getGeoReference();
        _formatHandler = formatHandler;
    }

    protected org.locationtech.jts.geom.GeometryFactory getGeometryFactory() {
        return _geometryFactory;
    }

    protected CityModelView getCityModelView() {
        return _cityModelView;
    }

    protected T getFeatureView() {
        return _featureView;
    }

    protected int getLOD() {
        return _lod;
    }

    protected ConvertOption getConvertOption() {
        return _convertOption;
    }

    protected AppearanceView getAppearanceView() {
        return _appearanceView;
    }

    protected org.citygml4j.model.citygml.core.CityModel getCityModel() {
        return _cityModel;
    }

    protected GeoReference getGeoReference() {
        return _geoReference;
    }

    protected List<Polygon> getPolygons() {
        return _polygons;
    }

    public CityModelView convert(String fileUrl) throws Exception {
        // 各フォーマット用の初期化
        initialize(fileUrl);

        switch (_lod) {
        case 1:
            convertLOD1();
            break;
        case 2:
            convertLOD2();
            break;
        case 3:
            convertLOD3();
            break;
        default:
            throw new IllegalArgumentException("Unsupported LOD");
        }
        return _cityModelView;
    }

    protected void convertLOD1() throws Exception {
        // 各フォーマットの実装から三角形のリストを作成
        var triangleModelsMap = createTriangleModelsMap();

        for (var meshKey : triangleModelsMap.keySet()) {
            var trianglesList = triangleModelsMap.get(meshKey);

            // 三角形を結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = createPolygonList(trianglesList);

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, null, false, null);
            }
        }

        // cityObjectを差し替える
        createLOD1(getFeatureView());
    }

    protected void convertLOD2() throws Exception {
        // 各フォーマットの実装からテクスチャを作成
        var textureMap = createSurfaceData();

        // 各フォーマットの実装から三角形のリストを作成
        var triangleModelsMap = createTriangleModelsMap();

        // 各フォーマットの実装から地面の基準となる三角形を特定する
        var groundTriangle = getGroundTriangle(triangleModelsMap);

        for (var meshKey : triangleModelsMap.keySet()) {
            // 同一平面の三角形をグループ化
            var samePlaneTrianglesList = createSamePlaneTrianglesList(triangleModelsMap.get(meshKey), groundTriangle);

            // テクスチャの座標が離れているものを分割する
            var trianglesList = splitByTexture(samePlaneTrianglesList);
            // var trianglesList = samePlaneTrianglesList;

            // グループ化したポリゴンごとに結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = new ArrayList<>();
            for (var triangles : trianglesList) {
                jtsPolygonList.addAll(createPolygonList(triangles));
            }

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, textureMap.get(meshKey), true, groundTriangle);
            }
        }

        // ParameterizedTextureを差し替える
        var oldAppearanceView = getAppearanceView();
        var appearance = oldAppearanceView != null ? oldAppearanceView.getGML() : new Appearance();
        var appearanceView = createAppearanceView(appearance, textureMap);
        removeTexture(appearance);
        getCityModelView().setAppearance(appearanceView);

        // cityObjectを差し替える
        createLOD2(getFeatureView());
    }

    protected void convertLOD3() throws Exception {
        // 各フォーマットの実装からSurfaceを作成
        var surfaceMap = createSurfaceData();

        // 各フォーマットの実装から三角形のリストを作成
        var triangleModelsMap = createTriangleModelsMap();

        // 各フォーマットの実装から地面の基準となる三角形を特定する
        var groundTriangle = getGroundTriangle(triangleModelsMap);

        for (var meshKey : triangleModelsMap.keySet()) {
            // 同一平面の三角形をグループ化
            var samePlaneTrianglesList = createSamePlaneTrianglesList(triangleModelsMap.get(meshKey), groundTriangle);

            // テクスチャの座標が離れているものを分割する
            var trianglesList = splitByTexture(samePlaneTrianglesList);

            // グループ化したポリゴンごとに結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = new ArrayList<>();
            for (var triangles : trianglesList) {
                jtsPolygonList.addAll(createPolygonList(triangles));
            }

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, surfaceMap.get(meshKey), true, groundTriangle);
            }
        }

        // ParameterizedTextureを差し替える
        var oldAppearanceView = getAppearanceView();
        var appearance = oldAppearanceView != null ? oldAppearanceView.getGML() : new Appearance();
        var appearanceView = createAppearanceView(appearance, surfaceMap);
        removeTexture(appearance);
        getCityModelView().setAppearance(appearanceView);

        // cityObjectを差し替える
        createLOD3(getFeatureView());
    }

    protected Polygon createPolygon(org.locationtech.jts.geom.Polygon jtsPolygon, AbstractSurfaceData surfaceData, boolean isCreateId,
            TriangleModel groundTriangle) {
        Polygon polygon = new Polygon();
        polygon.setId(String.format("poly_%s", UUID.randomUUID().toString()));
        var jtsExteriorRing = jtsPolygon.getExteriorRing();
        var gmlExteriorRing = createLinearRing(jtsExteriorRing, isCreateId);
        polygon.setExterior(new Exterior(gmlExteriorRing));

        List<AbstractRingProperty> interiorList = new ArrayList<>();
        for (var i = 0; i < jtsPolygon.getNumInteriorRing(); i++) {
            interiorList.add(new Interior(createLinearRing(jtsPolygon.getInteriorRingN(i), isCreateId)));
        }
        if (interiorList.size() > 0) {
            polygon.setInterior(interiorList);
        }

        // LODの種別ごとにSurfaceを構築する
        switch (getLOD()) {
        case 1:
            applyLOD1Surface(polygon);
            break;
        case 2:
            applyLOD2Surface(polygon, surfaceData, jtsPolygon, groundTriangle);
            break;
        case 3:
            applyLOD3Surface(polygon, surfaceData, jtsPolygon, groundTriangle);
            break;
        default:
            throw new IllegalArgumentException("Unsupported LOD");
        }

        return polygon;
    }

    protected LinearRing createLinearRing(org.locationtech.jts.geom.LinearRing jtsLinearRing, boolean isCreateId) {
        DirectPositionList directPositionList = new DirectPositionList();
        var geoReference = getGeoReference();
        var offset = getConvertOption().getOffset();
        if (offset == null)
            offset = new Vec3d();
        for (var i = 0; i < jtsLinearRing.getNumPoints(); i++) {
            var coordinate = jtsLinearRing.getCoordinateN(i);
            var vec3f = new Vec3f((float) (coordinate.x + offset.x), (float) (coordinate.y + offset.y), (float) (coordinate.z + offset.z));
            var geoCoordinate = geoReference.unproject(vec3f);
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

    protected void rotatePoint(org.locationtech.jts.geom.Coordinate point) {
        // 垂直なポリゴンをなるべくなくすために適当に回転させるための関数
        Transform transform = new Rotate(12.3, new Point3D(1, 0, 0));
        transform = transform.createConcatenation(new Rotate(45.6, new Point3D(0, 1, 0)));
        var result = transform.deltaTransform(new Point3D(point.x, point.y, point.z));
        point.x = result.getX();
        point.y = result.getY();
        point.z = result.getZ();
    }

    protected void invertRotatePoint(org.locationtech.jts.geom.Coordinate point) {
        // 垂直なポリゴンをなるべくなくすために適当に回転させるための関数の逆変換
        Transform transform = new Rotate(45.6, new Point3D(0, -1, 0));
        transform = transform.createConcatenation(new Rotate(12.3, new Point3D(-1, 0, 0)));
        var result = transform.deltaTransform(new Point3D(point.x, point.y, point.z));
        point.x = result.getX();
        point.y = result.getY();
        point.z = result.getZ();
    }

    protected boolean isVerticalToXYPlane(org.locationtech.jts.geom.Polygon polygon) {
        var coordinates = polygon.getExteriorRing().getCoordinates();
        if (coordinates.length > 4)
            throw new NotImplementedException();

        var v1 = new Point2D(coordinates[0].x, coordinates[0].y);
        var v2 = new Point2D(coordinates[1].x, coordinates[1].y);
        var v3 = new Point2D(coordinates[2].x, coordinates[2].y);
        var l1 = v1.distance(v2);
        var l2 = v1.distance(v3);
        var l3 = v2.distance(v3);
        return Math.abs(l1 + l2 - l3) < 0.001 || Math.abs(l1 + l3 - l2) < 0.001 || Math.abs(l2 + l3 - l1) < 0.001;
    }

    protected List<List<TriangleModel>> splitByTexture(List<List<TriangleModel>> trianglesList) {
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

    protected void addTextureCoordinates(Set<Point2f> textureCoordinateSet, TriangleModel triangle) {
        for (var i = 0; i < 3; i++) {
            var uv = triangle.getUV(i);
            if (!textureCoordinateSet.contains(uv)) {
                textureCoordinateSet.add(uv);
            }
        }
    }

    protected List<List<TriangleModel>> createSamePlaneTrianglesList(List<TriangleModel> triangleModels, TriangleModel groundTriangle) {
        List<List<TriangleModel>> sameTrianglesList = new ArrayList<>();
        {
            List<TriangleModel> sameNormalList = new ArrayList<>();
            if (groundTriangle != null) {
                for (var i = 1; i < triangleModels.size(); i++) {
                    // 精度向上のためGroundSurfaceは特別扱いして先に判定する
                    // 地面の基準となる三角形の平面との角度が0度で、平面が同じものをグループ化
                    var triangleModel = triangleModels.get(i);
                    double angle = Math.toDegrees(groundTriangle.getNormal().angle(triangleModel.getNormal()));
                    if (angle == 0 && groundTriangle.isSamePlane(triangleModel)) {
                        sameNormalList.add(triangleModel);
                    }
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
                if (angle == 0 && baseTriangle.isSamePlane(triangleModel)) {
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

    protected List<List<TriangleModel>> createSameNameTrianglesList(List<TriangleModel> triangleModels) {
        List<List<TriangleModel>> sameTrianglesList = new ArrayList<>();

        // 三角形のリストが空になるまでループ
        while (triangleModels.size() > 0) {
            var baseTriangle = triangleModels.get(0);
            List<TriangleModel> sameNormalList = new ArrayList<>();
            sameNormalList.add(baseTriangle);

            // 最初に見つかった三角形と同じ名前の三角形をグループ化
            for (var i = 1; i < triangleModels.size(); i++) {
                var triangleModel = triangleModels.get(i);
                if (baseTriangle.getName().equals(triangleModel.getName())) {
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

    protected List<org.locationtech.jts.geom.Polygon> createPolygonList(List<TriangleModel> triangleList) {
        var isInitCCW = false;
        boolean isTriangleCCW = false;
        List<org.locationtech.jts.geom.Polygon> trianglePolygonList = new ArrayList<>();
        var geometryFactory = getGeometryFactory();

        List<org.locationtech.jts.geom.Polygon> invalidPolygonList = new ArrayList<>();

        for (var triangleModel : triangleList) {
            var p1 = triangleModel.getVertex(0);
            var p2 = triangleModel.getVertex(1);
            var p3 = triangleModel.getVertex(2);
            var linearRing = geometryFactory.createLinearRing(new org.locationtech.jts.geom.Coordinate[] {
                    new org.locationtech.jts.geom.Coordinate(p1.x, p1.y, p1.z), new org.locationtech.jts.geom.Coordinate(p2.x, p2.y, p2.z),
                    new org.locationtech.jts.geom.Coordinate(p3.x, p3.y, p3.z), new org.locationtech.jts.geom.Coordinate(p1.x, p1.y, p1.z) });

            // XY平面と垂直なポリゴンをなるべくなくすように適当に回転（後で戻す）
            for (var point : linearRing.getCoordinates()) {
                rotatePoint(point);
            }

            // 不正なポリゴン、XY平面と垂直な面をunionするとバグるのでそのまま扱う
            var polygon = geometryFactory.createPolygon(linearRing, null);
            if (!polygon.isValid() || isVerticalToXYPlane(polygon)) {
                var resultPolygon = (org.locationtech.jts.geom.Polygon) polygon.copy();
                invalidPolygonList.add(resultPolygon);
                polygon.setUserData(triangleModel);
                addUserData(resultPolygon, polygon);
                isVerticalToXYPlane(polygon);
                continue;
            }

            // PolygonにuserDataとしてTriangleModelを保持しておく
            polygon.setUserData(triangleModel);
            trianglePolygonList.add(polygon);
            if (!isInitCCW) {
                // 初回はCCW判定を行う
                isTriangleCCW = Orientation.isCCW(linearRing.getCoordinateSequence());
                isInitCCW = true;
            }
        }

        for (var polygon : invalidPolygonList) {
            for (var point : polygon.getExteriorRing().getCoordinates()) {
                invertRotatePoint(point);
            }
        }

        if (trianglePolygonList.size() == 0) {
            return invalidPolygonList;
        }

        // 最初に見つかった三角形を基準に距離が近い順にソートする
        // これをやらないと広域かつ細かく分割されている三角形を結合したときに、結合に時間がかかる
        var basePolygon = trianglePolygonList.get(0);
        Comparator<org.locationtech.jts.geom.Polygon> comparator = new Comparator<org.locationtech.jts.geom.Polygon>() {
            @Override
            public int compare(org.locationtech.jts.geom.Polygon o1, org.locationtech.jts.geom.Polygon o2) {
                var d1 = basePolygon.distance(o1);
                var d2 = basePolygon.distance(o1);
                if (d1 == d2) {
                    return 0;
                }
                return d1 < d2 ? -1 : 1;
            }
        };
        Collections.sort(trianglePolygonList, comparator);

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

        for (var polygon : polygonList) {
            for (var point : polygon.getExteriorRing().getCoordinates()) {
                invertRotatePoint(point);
            }
            for (int i = 0; i < polygon.getNumInteriorRing(); ++i) {
                for (var point : polygon.getInteriorRingN(i).getCoordinates()) {
                    invertRotatePoint(point);
                }
            }
        }

        for (var polygon : trianglePolygonList) {
            for (var point : polygon.getExteriorRing().getCoordinates()) {
                invertRotatePoint(point);
            }
        }

        polygonList.addAll(invalidPolygonList);
        return polygonList;
    }

    @SuppressWarnings("unchecked")
    protected void addUserData(org.locationtech.jts.geom.Polygon polygon, org.locationtech.jts.geom.Polygon trianglePolygon) {
        if (polygon.getUserData() == null) {
            polygon.setUserData(new ArrayList<TriangleModel>());
        }
        List<TriangleModel> userDataList = (List<TriangleModel>) polygon.getUserData();
        userDataList.add((TriangleModel) trianglePolygon.getUserData());
    }

    protected org.locationtech.jts.geom.Polygon getSameCoordinatePolygon(org.locationtech.jts.geom.Coordinate coordinate,
            List<org.locationtech.jts.geom.Polygon> trianglePolygonList) {
        for (var trianglePolygon : trianglePolygonList) {
            if (hasSameCoordinate(coordinate, trianglePolygon)) {
                return trianglePolygon;
            }
        }
        return null;
    }

    protected boolean hasSameCoordinate(org.locationtech.jts.geom.Coordinate coordinate, org.locationtech.jts.geom.Polygon polygon) {
        for (var polygonCoordinate : polygon.getExteriorRing().getCoordinates()) {
            if (coordinate.x == polygonCoordinate.x && coordinate.y == polygonCoordinate.y && coordinate.z == polygonCoordinate.z) {
                return true;
            }
        }
        return false;
    }

    protected org.locationtech.jts.geom.Polygon getNearPolygon(org.locationtech.jts.geom.Coordinate coordinate,
            List<org.locationtech.jts.geom.Polygon> trianglePolygonList) {
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

        return nearPolygon;
    }

    protected List<org.locationtech.jts.geom.Polygon> splitJtsPolygon(org.locationtech.jts.geom.Geometry geometry) {
        List<org.locationtech.jts.geom.Polygon> polygonList = new ArrayList<>();
        try {
            if (geometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
                var original = (org.locationtech.jts.geom.Polygon) geometry;
                org.locationtech.jts.geom.LinearRing shellCopy = (org.locationtech.jts.geom.LinearRing) original.getExteriorRing().copy();
                org.locationtech.jts.geom.LinearRing[] holeCopies = new org.locationtech.jts.geom.LinearRing[original.getNumInteriorRing()];
                for (int i = 0; i < holeCopies.length; i++) {
                    holeCopies[i] = (org.locationtech.jts.geom.LinearRing) original.getInteriorRingN(i).copy();
                }
                polygonList.add(getGeometryFactory().createPolygon(shellCopy, holeCopies));
            } else if (geometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
                for (var i = 0; i < geometry.getNumGeometries(); i++) {
                    polygonList.add((org.locationtech.jts.geom.Polygon) geometry.getGeometryN(i));
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

    protected org.locationtech.jts.geom.Polygon matchPolygonFace(boolean isTriangleCCW, org.locationtech.jts.geom.Polygon polygon) {
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
            matchPolygon = getGeometryFactory().createPolygon(matchPolygon.getExteriorRing(),
                    interiorRingList.toArray(new org.locationtech.jts.geom.LinearRing[0]));
        }

        return matchPolygon;
    }

    protected void toGmlPolygonList(org.locationtech.jts.geom.Geometry jtsGeometry, AbstractSurfaceData surfaceData, boolean isCreateId,
            TriangleModel groundTriangle) {
        if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_POLYGON)) {
            createPolygon((org.locationtech.jts.geom.Polygon) jtsGeometry, surfaceData, isCreateId, groundTriangle);
        } else if (jtsGeometry.getGeometryType().equals(org.locationtech.jts.geom.Geometry.TYPENAME_MULTIPOLYGON)) {
            for (var i = 0; i < jtsGeometry.getNumGeometries(); i++) {
                toGmlPolygonList(jtsGeometry.getGeometryN(i), surfaceData, isCreateId, groundTriangle);
            }
        } else {
            System.out.println(String.format("Error toGmlPolygonList: GeometryType:%s", jtsGeometry.getGeometryType()));
        }
    }

    protected TextureCoordinates createTextureCoordinates(AbstractRing linearRing, ParameterizedTexture texture,
            org.locationtech.jts.geom.LinearRing jtsLinearRing, List<TriangleModel> userDataList) {
        List<Double> textureCoordinatesList = new ArrayList<Double>();
        for (var coordinate : jtsLinearRing.getCoordinates()) {
            var found = false;
            for (var triangleModel : userDataList) {
                if (found)
                    break;
                for (var i = 0; i < 3; i++) {
                    if (found)
                        break;
                    var vertex = triangleModel.getVertex(i);
                    if (coordinate.x == vertex.x && coordinate.y == vertex.y && coordinate.z == vertex.z) {
                        var uv = triangleModel.getUV(i);
                        textureCoordinatesList.add((double) uv.x);
                        textureCoordinatesList.add((double) uv.y);
                        found = true;
                    }
                }
            }
            if (!found) {
                // System.out.println("not found textureCoord → search by distance");

                // 同一頂点が見つからなかったら距離で判定する
                var minDistance = Double.MAX_VALUE;
                Point2f minDistanceUv = null;
                for (var triangleModel : userDataList) {
                    for (var i = 0; i < 3; i++) {
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
                textureCoordinatesList.add((double) minDistanceUv.x);
                textureCoordinatesList.add((double) minDistanceUv.y);
                // System.out.println(String.format("minDistance:%f", minDistance));
            }
        }

        // 全ての座標が (0, 1) の場合はテクスチャがないため、TextureAssociationを追加しない
        var any = false;
        for (var i = 0; i < textureCoordinatesList.size(); i += 2) {
            if (textureCoordinatesList.get(i) != 0 || textureCoordinatesList.get(i + 1) != 1) {
                any = true;
                break;
            }
        }
        if (!any)
            return null;

        // 有効な座標が設定されている場合はテクスチャ座標を設定する
        TextureCoordinates textureCoordinates = new TextureCoordinates();
        textureCoordinates.setRing(String.format("#%s", linearRing.getId()));
        textureCoordinates.setValue(textureCoordinatesList);

        return textureCoordinates;
    }

    protected AppearanceView createAppearanceView(Appearance appearance, Map<String, AbstractSurfaceData> surfaceMap) {
        for (var surface : surfaceMap.values()) {
            var surfaceDataProperty = new SurfaceDataProperty();
            surfaceDataProperty.setSurfaceData(surface);
            appearance.getSurfaceDataMember().add(surfaceDataProperty);
        }

        var appearanceMember = new AppearanceMember();
        appearanceMember.setAppearance(appearance);

        var appearanceFactory = new AppearanceFactory(getCityModelView());
        return appearanceFactory.createAppearance(appearanceMember);
    }

    protected TriangleModel getGroundTriangle(Map<String, List<TriangleModel>> triangleModelsMap) {
        TriangleModel groundTriangle = null;
        var groundBaseTriangle = TriangleModel.CreateGroundTriangle();

        // 地面の基準になる三角形を特定する
        // 基準となる三角形の平面との角度が180±5度以下のもののうち、最もZ座標が小さいものを採用する(GroundSurfaceの法線は地面の方を向いている)
        for (var triangleModels : triangleModelsMap.values()) {
            for (var triangleModel : triangleModels) {
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
        }

        return groundTriangle;
    }

    protected void removeTexture(Appearance appearance, Set<String> hrefSet) {
        var removeTextureList = new ArrayList<ParameterizedTexture>();
        var surfaceDataMemberList = appearance.getSurfaceDataMember();
        for (var surfaceDataMember : surfaceDataMemberList) {
            if (surfaceDataMember.getSurfaceData() instanceof ParameterizedTexture) {
                // 同じURIを持つParameterizedTextureを特定する
                ParameterizedTexture original = (ParameterizedTexture) surfaceDataMember.getSurfaceData();
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

    protected void initialize(String fileUrl) throws Exception {
        _formatHandler.initialize(fileUrl);
    }

    protected Map<String, List<TriangleModel>> createTriangleModelsMap() {
        return _formatHandler.createTriangleModelsMap();
    }

    protected Map<String, AbstractSurfaceData> createSurfaceData() throws IOException, URISyntaxException {
        return _formatHandler.createSurfaceData();
    }

    protected void applyLOD1Surface(Polygon polygon) {
        _polygons.add(polygon);
    }

    protected void applyLOD2Surface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle) {
        applySurface(polygon, surfaceData, jtsPolygon, groundTriangle);
    }

    protected void applyLOD3Surface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle) {
        applySurface(polygon, surfaceData, jtsPolygon, groundTriangle);
    }

    protected void applySurface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon, TriangleModel groundTriangle) {
        var userData = jtsPolygon.getUserData();
        if (userData instanceof List<?>) {
            var userDataList = (List<TriangleModel>) userData;
            if (surfaceData != null) {
                if (surfaceData instanceof ParameterizedTexture) {
                    var texture = (ParameterizedTexture) surfaceData;
                    TexCoordList texCoordList = new TexCoordList();
                    var textureCoordinates = createTextureCoordinates(polygon.getExterior().getRing(), texture, jtsPolygon.getExteriorRing(), userDataList);
                    if (textureCoordinates != null) {
                        texCoordList.addTextureCoordinates(textureCoordinates);
                    }
                    for (var i = 0; i < polygon.getInterior().size(); i++) {
                        var interiorTextureCoordinates = createTextureCoordinates(polygon.getInterior().get(i).getRing(), texture,
                                jtsPolygon.getInteriorRingN(i), userDataList);
                        if (interiorTextureCoordinates != null) {
                            texCoordList.addTextureCoordinates(interiorTextureCoordinates);
                        }
                    }

                    TextureAssociation textureAssociation = new TextureAssociation(texCoordList);
                    textureAssociation.setUri(String.format("#%s", polygon.getId()));
                    texture.addTarget(textureAssociation);
                } else if (surfaceData instanceof X3DMaterial) {
                    var x3dMaterial = (X3DMaterial) surfaceData;
                    x3dMaterial.addTarget(String.format("#%s", polygon.getId()));
                } else {
                    throw new IllegalArgumentException("Unsupported SurfaceData");
                }
            }
        }
        _polygons.add(polygon);
    }

    abstract protected void createLOD1(T feature);

    abstract protected void createLOD2(T feature);

    abstract protected void createLOD3(T feature);

    abstract protected void removeTexture(Appearance appearance);
}
