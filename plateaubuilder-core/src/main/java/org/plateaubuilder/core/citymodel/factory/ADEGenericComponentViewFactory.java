package org.plateaubuilder.core.citymodel.factory;

import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.citymodel.ADEGenericComponentView;
import org.plateaubuilder.core.editor.Editor;

public class ADEGenericComponentViewFactory extends AbstractFeatureViewFactory<ADEGenericComponent, ADEGenericComponent> {

    public ADEGenericComponentViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<ADEGenericComponent> create(ADEGenericComponent gmlObject) {
        var view = new ADEGenericComponentView(gmlObject);
        view.setId(gmlObject.getId());

        var lod1MultiSurfaceFactory = new LOD1MultiSurfaceFactory(getTarget());
        var lod1MultiSurface = lod1MultiSurfaceFactory.createLOD1MultiSurface(gmlObject);
        if (lod1MultiSurface != null) {
            view.setLOD1MultiSurface(lod1MultiSurface);
        }

        view.setDefaultVisible();

        view.toggleLODView(Editor.getCityModelViewMode().getLOD());

        return view;
    }
}
