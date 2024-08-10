package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.WaterBodyView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.editor.Editor;

public class WaterBodyViewFactory extends AbstractFeatureViewFactory<WaterBody, WaterBody> {
    public WaterBodyViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<WaterBody> create(WaterBody gmlObject) {
        var view = new WaterBodyView(gmlObject);
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
