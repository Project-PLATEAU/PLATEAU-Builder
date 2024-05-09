package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.transportation.Road;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.RoadView;
import org.plateaubuilder.core.editor.Editor;

public class RoadViewFactory extends AbstractFeatureViewFactory<Road, Road> {
    public RoadViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<Road> create(Road gmlObject) {
        var view = new RoadView(gmlObject);
        view.setId(gmlObject.getId());

        var lod1MultiSurfaceFactory = new LOD1MultiSurfaceFactory(getTarget());
        var lod1MultiSurface = lod1MultiSurfaceFactory.createLOD1MultiSurface(gmlObject);
        if (lod1MultiSurface != null) {
            view.setLOD1MultiSurface(lod1MultiSurface);
        }

        var lod2MultiSurfaceFactory = new LOD2MultiSurfaceFactory(getTarget());
        var lod2MultiSurface = lod2MultiSurfaceFactory.createLOD2MultiSurface(gmlObject);
        if (lod2MultiSurface != null) {
            view.setLOD2MultiSurface(lod2MultiSurface);
        }

        var lod3MultiSurfaceFactory = new LOD3MultiSurfaceFactory(getTarget());
        var lod3MultiSurface = lod3MultiSurfaceFactory.createLOD3MultiSurface(gmlObject);
        if (lod3MultiSurface != null) {
            view.setLOD3MultiSurface(lod3MultiSurface);
        }

        view.setDefaultVisible();

        view.toggleLODView(Editor.getCityModelViewMode().getLOD());

        return view;
    }
}
