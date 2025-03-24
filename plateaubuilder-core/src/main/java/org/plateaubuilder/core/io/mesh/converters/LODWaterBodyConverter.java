package org.plateaubuilder.core.io.mesh.converters;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.WaterBodyView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceWaterBodyCommand;
import org.plateaubuilder.core.editor.surfacetype.WaterBodyModuleComponentManipulator;

public class LODWaterBodyConverter extends AbstractLODMultiSurfaceConverter<WaterBodyView, WaterBody> {
    public LODWaterBodyConverter(CityModelView cityModelView, WaterBodyView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD1(WaterBodyView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (WaterBody) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new WaterBodyModuleComponentManipulator(newFeature, 1).clear();

        // lod1MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        newFeature.setLod1MultiSurface(multiSurfaceProperty);

        Editor.getUndoManager().addCommand(new ReplaceWaterBodyCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD2(WaterBodyView feature) {
        throw new UnsupportedOperationException("Unimplemented method 'createLOD2'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD3(WaterBodyView feature) {
        throw new UnsupportedOperationException("Unimplemented method 'createLOD3'");
    }
}
