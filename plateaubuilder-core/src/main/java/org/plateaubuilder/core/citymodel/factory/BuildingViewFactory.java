package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateaubuilder.core.citymodel.BuildingInstallationView;
import org.plateaubuilder.core.citymodel.BuildingView;
import org.plateaubuilder.core.citymodel.CityModelGroup;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.ManagedGMLView;
import org.plateaubuilder.core.editor.Editor;

public class BuildingViewFactory
        extends AbstractFeatureViewFactory<AbstractBuilding, AbstractBuilding> {
    public BuildingViewFactory(CityModelGroup group, CityModelView target) {
        super(group, target);
    }

    @Override
    public ManagedGMLView<AbstractBuilding> create(AbstractBuilding gmlObject) {
        var view = new BuildingView(gmlObject);
        view.setId(gmlObject.getId());

        var lod1SolidFactory = new LOD1SolidFactory(getTarget());
        var lod1Solid = lod1SolidFactory.createLOD1Solid(gmlObject);
        if (lod1Solid != null) {
            view.setLOD1Solid(lod1Solid);
        }

        var lod2SolidFactory = new LOD2SolidFactory(getTarget());
        var lod2Solid = lod2SolidFactory.createLOD2Solid(gmlObject);
        if (lod2Solid != null) {
            view.setLOD2Solid(lod2Solid);
        }

        var lod3SolidFactory = new LOD3SolidFactory(getTarget());
        var lod3Solid = lod3SolidFactory.createLOD3Solid(gmlObject);
        if (lod3Solid != null) {
            view.setLOD3Solid(lod3Solid);
        }

        view.setDefaultVisible();

        // </bldg:outerBuildingInstallation>
        for (var outerBuildingInstallation : gmlObject.getOuterBuildingInstallation()) {
            var buildingInstallationViewFactory = new BuildingInstallationViewFactory(getGroup(), getTarget());
            var buildingInstallationView = buildingInstallationViewFactory.create(outerBuildingInstallation.getObject());
            if (buildingInstallationView != null) {
                view.addBuildingInstallationView((BuildingInstallationView) buildingInstallationView);
            }
        }

        // <bldg:consistsOfBuildingPart>
        for (var consistsOfBuildingPart : gmlObject.getConsistsOfBuildingPart()) {
            var buildingPart = create(consistsOfBuildingPart.getBuildingPart());
            view.addBuildingPart((BuildingView) buildingPart);
        }

        view.toggleLODView(Editor.getCityModelViewMode().getLOD());

        return view;
    }
}
