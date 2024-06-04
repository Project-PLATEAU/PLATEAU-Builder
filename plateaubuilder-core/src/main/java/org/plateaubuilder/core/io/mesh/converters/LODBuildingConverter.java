package org.plateaubuilder.core.io.mesh.converters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.building.BoundarySurfaceProperty;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.building.GroundSurface;
import org.citygml4j.model.citygml.building.OuterCeilingSurface;
import org.citygml4j.model.citygml.building.RoofSurface;
import org.citygml4j.model.citygml.building.WallSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.AbstractSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.BuildingView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.factory.LOD1SolidFactory;
import org.plateaubuilder.core.citymodel.geometry.LOD2SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD3SolidView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceBuildingCommand;
import org.plateaubuilder.core.editor.surfacetype.BuildingModuleComponentManipulator;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;

public class LODBuildingConverter extends AbstractLODConverter<BuildingView, AbstractBuilding> {

    private ArrayList<AbstractBoundarySurface> _boundedBy = new ArrayList<AbstractBoundarySurface>();

    private CompositeSurface _compositeSurface = new CompositeSurface();

    public LODBuildingConverter(CityModelView cityModelView, BuildingView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        var cityObject = getFeatureView().getGML();
        createLOD1(cityObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        var appearance = getAppearanceView().getGML();
        var appearanceView = createAppearanceView(appearance, textureMap);
        removeTexture(appearance);
        getCityModelView().setAppearance(appearanceView);

        // cityObjectを差し替える
        var cityObject = getFeatureView().getGML();
        createLOD2(cityObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
        var appearance = getAppearanceView().getGML();
        var appearanceView = createAppearanceView(appearance, surfaceMap);
        removeTexture(appearance);
        getCityModelView().setAppearance(appearanceView);

        // cityObjectを差し替える
        var cityObject = getFeatureView().getGML();
        createLOD3(cityObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD1Surface(Polygon polygon) {
        _compositeSurface.addSurfaceMember(new SurfaceProperty(polygon));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD2Surface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle) {
        applyBoundarySurfaces(polygon, surfaceData, jtsPolygon, groundTriangle, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD3Surface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle) {
        applyBoundarySurfaces(polygon, surfaceData, jtsPolygon, groundTriangle, 3);
    }

    @SuppressWarnings("unchecked")
    private void applyBoundarySurfaces(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle, int lod) {
        // lod2Solid
        // 保持しておいたTriangleModelを取得する
        List<TriangleModel> userDataList = (List<TriangleModel>) jtsPolygon.getUserData();
        if (userDataList == null) {
            throw new IllegalArgumentException("userDataList == null");
        }

        _compositeSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
        _boundedBy.add(createBoundarySurface(polygon, userDataList.get(0), groundTriangle, lod));

        if (surfaceData == null) {
            return;
        }

        if (surfaceData instanceof ParameterizedTexture) {
            var texture = (ParameterizedTexture) surfaceData;
            TexCoordList texCoordList = new TexCoordList();
            var textureCoordinates = createTextureCoordinates(polygon.getExterior().getRing(), texture, jtsPolygon.getExteriorRing(), userDataList);
            if (textureCoordinates != null) {
                texCoordList.addTextureCoordinates(textureCoordinates);
            }
            for (var i = 0; i < polygon.getInterior().size(); i++) {
                var interiorTextureCoordinates = createTextureCoordinates(polygon.getInterior().get(i).getRing(), texture, jtsPolygon.getInteriorRingN(i),
                        userDataList);
                if (interiorTextureCoordinates != null) {
                    texCoordList.addTextureCoordinates(interiorTextureCoordinates);
                }
            }

            TextureAssociation textureAssociation = new TextureAssociation(texCoordList);
            textureAssociation.setUri(String.format("#%s", polygon.getId()));
            texture.addTarget(textureAssociation);
        }
    }

    private void createLOD1(AbstractBuilding feature) {
        // lod1Solid
        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(_compositeSurface));
        SolidProperty solidProperty = new SolidProperty(solid);
        feature.setLod1Solid(solidProperty);

        getFeatureView().setLODView(1, new LOD1SolidFactory(getCityModelView()).createLOD1Solid(feature));
    }

    private void createLOD2(AbstractBuilding feature) {
        var newFeature = (Building) feature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new BuildingModuleComponentManipulator(newFeature, 2).clearSolidIncludingBoundaries();

        // lod2Solid
        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(_compositeSurface));
        SolidProperty solidProperty = new SolidProperty(solid);
        newFeature.setLod2Solid(solidProperty);

        // boundedBy
        var boundedBySurface = newFeature.getBoundedBySurface();
        for (var boundarySurface : _boundedBy) {
            boundedBySurface.add(new BoundarySurfaceProperty(boundarySurface));
        }

        Editor.getUndoManager().addCommand(new ReplaceBuildingCommand(getCityModelView().getGML(), feature, newFeature));
    }

    private void createLOD3(AbstractBuilding feature) {
        var newFeature = (Building) feature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new BuildingModuleComponentManipulator(newFeature, 3).clearSolidIncludingBoundaries();

        // lod3Solid
        Solid solid = new Solid();
        solid.setExterior(new SurfaceProperty(_compositeSurface));
        SolidProperty solidProperty = new SolidProperty(solid);
        newFeature.setLod3Solid(solidProperty);

        // boundedBy
        var boundedBySurface = newFeature.getBoundedBySurface();
        for (var boundarySurface : _boundedBy) {
            boundedBySurface.add(new BoundarySurfaceProperty(boundarySurface));
        }

        Editor.getUndoManager().addCommand(new ReplaceBuildingCommand(getCityModelView().getGML(), feature, newFeature));
    }

    private AbstractBoundarySurface createBoundarySurface(Polygon polygon, TriangleModel triangle, TriangleModel groundTriangle, int lod) {
        AbstractBoundarySurface boundarySurface = null;

        // 90度を基準に±何度までを壁とするかの閾値
        double threshold = getConvertOption().getWallThreshold();

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
        if (lod == 2)
            boundarySurface.setLod2MultiSurface(new MultiSurfaceProperty(new MultiSurface(surfaces)));
        else
            boundarySurface.setLod3MultiSurface(new MultiSurfaceProperty(new MultiSurface(surfaces)));

        return boundarySurface;
    }

    private void removeTexture(Appearance appearance) {
        Solid gmlObject;
        var lod = getLOD();
        var featureView = getFeatureView();
        switch (lod) {
        case 2:
            var lod2View = (LOD2SolidView) featureView.getLODView(lod);
            if (lod2View == null)
                return;

            gmlObject = (Solid) lod2View.getGmlObject();
            break;
        case 3:
            var lod3View = (LOD3SolidView) featureView.getLODView(lod);
            if (lod3View == null)
                return;

            gmlObject = (Solid) lod3View.getGmlObject();
            break;
        default:
            throw new IllegalArgumentException("Unsupported LOD");
        }
        var compositeSurface = (CompositeSurface) gmlObject.getExterior().getObject();

        // もともと参照していたテクスチャを削除する
        var hrefSet = new HashSet<String>();
        for (var surfaceMember : compositeSurface.getSurfaceMember()) {
            hrefSet.add(surfaceMember.getHref());
        }
        removeTexture(appearance, hrefSet);
    }
}
