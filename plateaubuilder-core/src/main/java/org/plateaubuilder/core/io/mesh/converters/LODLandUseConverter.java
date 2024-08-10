package org.plateaubuilder.core.io.mesh.converters;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.LandUseView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceLandUseCommand;
import org.plateaubuilder.core.editor.surfacetype.LandUseModuleComponentManipulator;

public class LODLandUseConverter extends AbstractLODMultiSurfaceConverter<LandUseView, LandUse> {

    private List<Polygon> _polygons = new ArrayList<>();

    public LODLandUseConverter(CityModelView cityModelView, LandUseView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    @Override
    protected void createLOD1(LandUseView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (LandUse) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new LandUseModuleComponentManipulator(newFeature, 1).clear();

        // lod1MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : _polygons) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        newFeature.setLod1MultiSurface(multiSurfaceProperty);

        Editor.getUndoManager().addCommand(new ReplaceLandUseCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    @Override
    protected void createLOD2(LandUseView feature) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createLOD2'");
    }

    @Override
    protected void createLOD3(LandUseView feature) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createLOD3'");
    }
}
