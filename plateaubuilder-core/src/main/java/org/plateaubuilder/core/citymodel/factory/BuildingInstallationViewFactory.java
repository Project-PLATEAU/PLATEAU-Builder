package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.plateaubuilder.core.citymodel.BuildingInstallationView;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.editor.Editor;

public class BuildingInstallationViewFactory extends AbstractFeatureViewFactory<BuildingInstallation, BuildingInstallation> {
    public BuildingInstallationViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<BuildingInstallation> create(BuildingInstallation gmlObject) {
        var view = new BuildingInstallationView(gmlObject);
        view.setId(gmlObject.getId());

        var lod2GeometryFactory = new LOD2GeometryFactory(getTarget());
        var lod2Geometry = lod2GeometryFactory.createLOD2Geometry(gmlObject);
        if (lod2Geometry != null) {
            view.setLOD2Geometry(lod2Geometry);
        }

        var lod3GeometryFactory = new LOD3GeometryFactory(getTarget());
        var lod3Geometry = lod3GeometryFactory.createLOD3Geometry(gmlObject);
        if (lod3Geometry != null) {
            view.setLOD3Geometry(lod3Geometry);
        }

        view.setDefaultVisible();

        view.toggleLODView(Editor.getCityModelViewMode().getLOD());

        return view;
    }
}
