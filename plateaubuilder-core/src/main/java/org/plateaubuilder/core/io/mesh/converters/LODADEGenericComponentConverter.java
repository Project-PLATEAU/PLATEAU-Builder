package org.plateaubuilder.core.io.mesh.converters;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.ADEGenericComponentView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceADEGenericComponentCommand;
import org.plateaubuilder.core.editor.surfacetype.ADEGenericComponentModuleComponentManipulator;

public class LODADEGenericComponentConverter extends AbstractLODMultiSurfaceConverter<ADEGenericComponentView, ADEGenericComponent> {

    public LODADEGenericComponentConverter(CityModelView cityModelView, ADEGenericComponentView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    @Override
    protected void createLOD1(ADEGenericComponentView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (ADEGenericComponent) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new ADEGenericComponentModuleComponentManipulator(newFeature, 1).clear();

        // lod1MultiSurface
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        MultiSurfaceProperty multiSurfaceProperty = new MultiSurfaceProperty(multiSurface);
        newFeature.setLod1MultiSurface(multiSurfaceProperty);

        Editor.getUndoManager().addCommand(new ReplaceADEGenericComponentCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD2(ADEGenericComponentView feature) {
        throw new UnsupportedOperationException("Unimplemented method 'createLOD2'");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD3(ADEGenericComponentView feature) {
        throw new UnsupportedOperationException("Unimplemented method 'createLOD3'");
    }
}
