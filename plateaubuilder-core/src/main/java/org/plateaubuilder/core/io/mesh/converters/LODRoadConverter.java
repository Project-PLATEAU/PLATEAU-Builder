package org.plateaubuilder.core.io.mesh.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.appearance.ParameterizedTexture;
import org.citygml4j.model.citygml.appearance.TexCoordList;
import org.citygml4j.model.citygml.appearance.TextureAssociation;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficArea;
import org.citygml4j.model.citygml.transportation.AuxiliaryTrafficAreaProperty;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.citygml.transportation.TrafficAreaProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.RoadView;
import org.plateaubuilder.core.citymodel.factory.LOD1MultiSurfaceFactory;
import org.plateaubuilder.core.citymodel.geometry.LOD2RoadMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD3RoadMultiSurfaceView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceRoadCommand;
import org.plateaubuilder.core.editor.surfacetype.RoadModuleComponentManipulator;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;

public class LODRoadConverter extends AbstractLODConverter<RoadView, Road> {

    private List<Polygon> _polygons = new ArrayList<>();
    private Map<String, List<Polygon>> trafficPolygons = new HashMap<>();
    private Map<String, List<Polygon>> auxiliaryTrafficAreaPolygons = new HashMap<>();

    public LODRoadConverter(CityModelView cityModelView, RoadView featureView, int lod, ConvertOption convertOption, Abstract3DFormatHandler formatHandler) {
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
        createLOD1(getFeatureView());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void convertLOD2() throws Exception {
        // 各フォーマットの実装からSurfaceを作成
        var surfaceMap = createSurfaceData();

        // 各フォーマットの実装から三角形のリストを作成
        var triangleModelsMap = createTriangleModelsMap();

        for (var meshKey : triangleModelsMap.keySet()) {
            // 同一名の三角形をグループ化
            var samePlaneTrianglesList = createSameNameTrianglesList(triangleModelsMap.get(meshKey));

            // グループ化したポリゴンごとに結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = new ArrayList<>();
            for (var triangles : samePlaneTrianglesList) {
                jtsPolygonList.addAll(createPolygonList(triangles));
            }

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, surfaceMap.get(meshKey), true, null);
            }
        }

        // ParameterizedTextureを差し替える
        var appearance = getAppearanceView().getGML();
        var appearanceView = createAppearanceView(appearance, surfaceMap);
        removeTexture(appearance);
        getCityModelView().setAppearance(appearanceView);

        // cityObjectを差し替える
        createLOD2(getFeatureView());
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

        for (var meshKey : triangleModelsMap.keySet()) {
            // 同一名の三角形をグループ化
            var samePlaneTrianglesList = createSameNameTrianglesList(triangleModelsMap.get(meshKey));

            // グループ化したポリゴンごとに結合
            List<org.locationtech.jts.geom.Polygon> jtsPolygonList = new ArrayList<>();
            for (var triangles : samePlaneTrianglesList) {
                jtsPolygonList.addAll(createPolygonList(triangles));
            }

            // gmlのPolygonに変換
            for (var jtsPolygon : jtsPolygonList) {
                toGmlPolygonList(jtsPolygon, surfaceMap.get(meshKey), true, null);
            }
        }

        // ParameterizedTextureを差し替える
        var appearance = getAppearanceView().getGML();
        var appearanceView = createAppearanceView(appearance, surfaceMap);
        removeTexture(appearance);
        getCityModelView().setAppearance(appearanceView);

        // cityObjectを差し替える
        createLOD3(getFeatureView());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD1Surface(Polygon polygon) {
        _polygons.add(polygon);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD2Surface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle) {
        applySurface(polygon, surfaceData, jtsPolygon, groundTriangle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD3Surface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon,
            TriangleModel groundTriangle) {
        applySurface(polygon, surfaceData, jtsPolygon, groundTriangle);
    }

    private void applySurface(Polygon polygon, AbstractSurfaceData surfaceData, org.locationtech.jts.geom.Polygon jtsPolygon, TriangleModel groundTriangle) {
        var userData = jtsPolygon.getUserData();
        if (userData instanceof List<?>) {
            var userDataList = (List<TriangleModel>) userData;
            var triangleModel = userDataList.get(0);
            var name = triangleModel.getName();
            if (name != null) {
                if (name.startsWith("trafficArea_")) {
                    if (!trafficPolygons.containsKey(name)) {
                        trafficPolygons.put(name, new ArrayList<Polygon>());
                    }
                    trafficPolygons.get(name).add(polygon);
                } else if (name.startsWith("auxiliaryTrafficArea_")) {
                    if (!auxiliaryTrafficAreaPolygons.containsKey(name)) {
                        auxiliaryTrafficAreaPolygons.put(name, new ArrayList<Polygon>());
                    }
                    auxiliaryTrafficAreaPolygons.get(name).add(polygon);
                }
            }

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
                }
            }
        }
        _polygons.add(polygon);
    }

    private void removeTexture(Appearance appearance) {
        // もともと参照していたテクスチャを削除する
        var hrefSet = new HashSet<String>();

        MultiSurface gmlObject;
        var lod = getLOD();
        var featureView = getFeatureView();
        switch (lod) {
        case 2:
            var lod2View = (LOD2RoadMultiSurfaceView) featureView.getLODView(lod);
            if (lod2View == null) {
                return;
            }

            collectHref(hrefSet, lod2View.getGmlObject());
            for (var trafficArea : lod2View.getTrafficAreas()) {
                collectHref(hrefSet, trafficArea.getGmlObject());
            }
            for (var auxiliaryTrafficArea : lod2View.getAuxiliaryTrafficAreas()) {
                collectHref(hrefSet, auxiliaryTrafficArea.getGmlObject());
            }
            break;
        case 3:
            var lod3View = (LOD3RoadMultiSurfaceView) featureView.getLODView(lod);
            if (lod3View == null) {
                return;
            }

            collectHref(hrefSet, lod3View.getGmlObject());
            for (var trafficArea : lod3View.getTrafficAreas()) {
                collectHref(hrefSet, trafficArea.getGmlObject());
            }
            for (var auxiliaryTrafficArea : lod3View.getAuxiliaryTrafficAreas()) {
                collectHref(hrefSet, auxiliaryTrafficArea.getGmlObject());
            }
            break;
        default:
            throw new IllegalArgumentException("Unsupported LOD");
        }

        removeTexture(appearance, hrefSet);
    }

    private void collectHref(Set<String> hrefSet, MultiSurface multiSurface) {
        for (var surfaceMember : multiSurface.getSurfaceMember()) {
            hrefSet.add(surfaceMember.getHref());
        }
    }

    private void createLOD1(RoadView feature) {
        var currentFeature = feature.getGML();

        // lod1MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : _polygons) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        currentFeature.setLod1MultiSurface(multiSurfaceProperty);

        getFeatureView().setLODView(1, new LOD1MultiSurfaceFactory(getCityModelView()).createLOD1MultiSurface(currentFeature));
    }

    private void createLOD2(RoadView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (Road) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new RoadModuleComponentManipulator(newFeature, 2).clear();

        // lod2MultiSurface
        {
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : _polygons) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }

            for (var key : trafficPolygons.keySet()) {
                for (var polygon : trafficPolygons.get(key)) {
                    multiSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
                }
            }
            for (var key : auxiliaryTrafficAreaPolygons.keySet()) {
                for (var polygon : auxiliaryTrafficAreaPolygons.get(key)) {
                    multiSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
                }
            }
            newFeature.setLod3MultiSurface(new MultiSurfaceProperty(multiSurface));
        }
        for (var key : trafficPolygons.keySet()) {
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : trafficPolygons.get(key)) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }
            TrafficArea trafficArea = new TrafficArea();
            trafficArea.setId(key);
            trafficArea.setLod2MultiSurface(new MultiSurfaceProperty(multiSurface));
            newFeature.addTrafficArea(new TrafficAreaProperty(trafficArea));
        }
        for (var key : auxiliaryTrafficAreaPolygons.keySet()) {
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : auxiliaryTrafficAreaPolygons.get(key)) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }
            AuxiliaryTrafficArea auxiliaryTrafficArea = new AuxiliaryTrafficArea();
            auxiliaryTrafficArea.setId(key);
            auxiliaryTrafficArea.setLod2MultiSurface(new MultiSurfaceProperty(multiSurface));
            newFeature.addAuxiliaryTrafficArea(new AuxiliaryTrafficAreaProperty(auxiliaryTrafficArea));
        }

        Editor.getUndoManager().addCommand(new ReplaceRoadCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    private void createLOD3(RoadView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (Road) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new RoadModuleComponentManipulator(newFeature, 3).clear();

        // lod3MultiSurface
        {
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : _polygons) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }

            for (var key : trafficPolygons.keySet()) {
                for (var polygon : trafficPolygons.get(key)) {
                    multiSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
                }
            }
            for (var key : auxiliaryTrafficAreaPolygons.keySet()) {
                for (var polygon : auxiliaryTrafficAreaPolygons.get(key)) {
                    multiSurface.addSurfaceMember(new SurfaceProperty(String.format("#%s", polygon.getId())));
                }
            }
            newFeature.setLod3MultiSurface(new MultiSurfaceProperty(multiSurface));
        }
        for (var key : trafficPolygons.keySet()) {
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : trafficPolygons.get(key)) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }
            TrafficArea trafficArea = new TrafficArea();
            trafficArea.setId(key);
            trafficArea.setLod3MultiSurface(new MultiSurfaceProperty(multiSurface));
            newFeature.addTrafficArea(new TrafficAreaProperty(trafficArea));
        }
        for (var key : auxiliaryTrafficAreaPolygons.keySet()) {
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : auxiliaryTrafficAreaPolygons.get(key)) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }
            AuxiliaryTrafficArea auxiliaryTrafficArea = new AuxiliaryTrafficArea();
            auxiliaryTrafficArea.setId(key);
            auxiliaryTrafficArea.setLod3MultiSurface(new MultiSurfaceProperty(multiSurface));
            newFeature.addAuxiliaryTrafficArea(new AuxiliaryTrafficAreaProperty(auxiliaryTrafficArea));
        }

        Editor.getUndoManager().addCommand(new ReplaceRoadCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }
}
