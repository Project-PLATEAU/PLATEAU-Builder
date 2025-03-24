package org.plateaubuilder.core.io.mesh.converters;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityFurnitureView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceCityFurnitureCommand;
import org.plateaubuilder.core.editor.surfacetype.CityFurnitureModuleComponentManipulator;

public class LODCityFurnitureConverter extends AbstractLODGeometryConverter<CityFurnitureView, CityFurniture> {

    public LODCityFurnitureConverter(CityModelView cityModelView, CityFurnitureView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD1(CityFurnitureView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (CityFurniture) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new CityFurnitureModuleComponentManipulator(newFeature, 1).clear();

        // lod1Geometry
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        newFeature.setLod1Geometry(multiSurfaceProperty);

        Editor.getUndoManager().addCommand(new ReplaceCityFurnitureCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD2(CityFurnitureView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (CityFurniture) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new CityFurnitureModuleComponentManipulator(newFeature, 2).clear();

        // lod2Geometry
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        newFeature.setLod2Geometry(new MultiSurfaceProperty(multiSurface));

        Editor.getUndoManager().addCommand(new ReplaceCityFurnitureCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD3(CityFurnitureView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (CityFurniture) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new CityFurnitureModuleComponentManipulator(newFeature, 3).clear();

        // lod3Geometry
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        newFeature.setLod3Geometry(new MultiSurfaceProperty(multiSurface));

        Editor.getUndoManager().addCommand(new ReplaceCityFurnitureCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }
}
