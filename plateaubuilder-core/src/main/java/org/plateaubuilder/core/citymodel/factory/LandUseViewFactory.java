package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.landuse.LandUse;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.LandUseView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.editor.Editor;

public class LandUseViewFactory extends AbstractFeatureViewFactory<LandUse, LandUse> {
    public LandUseViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<LandUse> create(LandUse gmlObject) {
        var view = new LandUseView(gmlObject);
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
