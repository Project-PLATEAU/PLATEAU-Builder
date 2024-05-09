package org.plateaubuilder.core.io.mesh.converters;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.appearance.AbstractSurfaceData;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.RoadView;
import org.plateaubuilder.core.citymodel.factory.LOD1MultiSurfaceFactory;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceRoadCommand;
import org.plateaubuilder.core.editor.surfacetype.RoadModuleComponentManipulator;
import org.plateaubuilder.core.io.mesh.converters.model.TriangleModel;

public class LODMultiSurfaceConverter extends AbstractLODConverter<RoadView, Road> {

    private List<Polygon> _polygons = new ArrayList<>();

    public LODMultiSurfaceConverter(CityModelView cityModelView, RoadView featureView, int lod, ConvertOption convertOption,
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
                toGmlPolygonList(jtsPolygon, null, null, false);
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void convertLOD3() throws Exception {
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
    protected void applyLOD2Surface(Polygon polygon, TriangleModel groundTriangle, AbstractSurfaceData surfaceData,
            org.locationtech.jts.geom.Polygon jtsPolygon) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void applyLOD3Surface(Polygon polygon, TriangleModel groundTriangle, AbstractSurfaceData surfaceData,
            org.locationtech.jts.geom.Polygon jtsPolygon) {
    }
    private void createLOD1(Road feature) {
        // lod1MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : _polygons) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        feature.setLod1MultiSurface(multiSurfaceProperty);

        getFeatureView().setLODView(1, new LOD1MultiSurfaceFactory(getCityModelView()).createLOD1MultiSurface(feature));
    }

    private void createLOD2(Road feature) {
        var newRoad = (Road) feature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new RoadModuleComponentManipulator(newRoad, 2).clear();

        // lod2MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : _polygons) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        feature.setLod2MultiSurface(multiSurfaceProperty);

        Editor.getUndoManager().addCommand(new ReplaceRoadCommand(getCityModelView().getGML(), feature, newRoad));
    }

    private void createLOD3(Road feature) {
        var newRoad = (Road) feature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new RoadModuleComponentManipulator(newRoad, 3).clear();

        // lod3MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : _polygons) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        feature.setLod3MultiSurface(multiSurfaceProperty);

        Editor.getUndoManager().addCommand(new ReplaceRoadCommand(getCityModelView().getGML(), feature, newRoad));
    }
}
