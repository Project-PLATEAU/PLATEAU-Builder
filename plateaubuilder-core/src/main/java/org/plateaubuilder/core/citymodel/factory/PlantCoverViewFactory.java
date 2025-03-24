package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.citymodel.PlantCoverView;
import org.plateaubuilder.core.editor.Editor;

public class PlantCoverViewFactory extends AbstractFeatureViewFactory<PlantCover, PlantCover> {
    public PlantCoverViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<PlantCover> create(PlantCover gmlObject) {
        var view = new PlantCoverView(gmlObject);
        view.setId(gmlObject.getId());

        var lod1MultiSolidFactory = new LOD1MultiSolidFactory(getTarget());
        var lod1MultiSolid = lod1MultiSolidFactory.createLOD1MultiSolid(gmlObject);
        if (lod1MultiSolid != null) {
            view.setLOD1MultiSolid(lod1MultiSolid);
        }

        var lod2MultiSolidFactory = new LOD2MultiSolidFactory(getTarget());
        var lod2MultiSolid = lod2MultiSolidFactory.createLOD2MultiSolid(gmlObject);
        if (lod2MultiSolid != null) {
            view.setLOD2MultiSolid(lod2MultiSolid);
        }

        var lod2MultiSurfaceFactory = new LOD2MultiSurfaceFactory(getTarget());
        var lod2MultiSurface = lod2MultiSurfaceFactory.createLOD2MultiSurface(gmlObject);
        if (lod2MultiSurface != null) {
            view.setLOD2MultiSurface(lod2MultiSurface);
        }

        var lod3MultiSolidFactory = new LOD3MultiSolidFactory(getTarget());
        var lod3MultiSolid = lod3MultiSolidFactory.createLOD3MultiSolid(gmlObject);
        if (lod3MultiSolid != null) {
            view.setLOD3MultiSolid(lod3MultiSolid);
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
