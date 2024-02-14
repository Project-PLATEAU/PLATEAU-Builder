package org.plateau.citygmleditor.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;

public class CityObjectMemberFactory extends CityGMLFactory {
    public CityObjectMemberFactory(CityModelView target) {
        super(target);
    }

    public BuildingView createBuilding(AbstractBuilding gmlObject) {
        var building = new BuildingView(gmlObject);
        building.setId(gmlObject.getId());

        var lod1SolidFactory = new LOD1SolidFactory(getTarget());
        var lod1Solid = lod1SolidFactory.createLOD1Solid(gmlObject);
        if (lod1Solid != null)
            building.setLOD1Solid(lod1Solid);

        var lod2SolidFactory = new LOD2SolidFactory(getTarget());
        var lod2Solid = lod2SolidFactory.createLOD2Solid(gmlObject);
        if (lod2Solid != null) {
            if (lod1Solid != null)
                lod1Solid.setVisible(false);

            building.setLOD2Solid(lod2Solid);
        }

        var lod3SolidFactory = new LOD3SolidFactory(getTarget());
        var lod3Solid = lod3SolidFactory.createLOD3Solid(gmlObject);
        if (lod3Solid != null) {
            if (lod1Solid != null)
                lod1Solid.setVisible(false);
            if (lod2Solid != null)
                lod2Solid.setVisible(false);
            building.setLOD3Solid(lod3Solid);
        }

         // </bldg:outerBuildingInstallation>
        for (var outerBuildingInstallation : gmlObject.getOuterBuildingInstallation()) {
            var geometryFactory = new GeometryFactory(getTarget());
            var buildingInstallationView = geometryFactory.cretateBuildingInstallationView(outerBuildingInstallation.getObject());
            if (buildingInstallationView != null) {
                building.addBuildingInstallationView(buildingInstallationView);
            }
        }

        // <bldg:consistsOfBuildingPart>
        for (var consistsOfBuildingPart : gmlObject.getConsistsOfBuildingPart()) {
            var buildingPart = createBuilding(consistsOfBuildingPart.getBuildingPart());
            building.addBuildingPart(buildingPart);
        }

        building.toggleLODView(CityGMLEditorApp.getCityModelViewMode().getLOD());

        return building;
    }
}
