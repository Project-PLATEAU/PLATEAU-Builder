package org.plateau.citygmleditor.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;

public class CityObjectMemberFactory extends CityGMLFactory {
    protected CityObjectMemberFactory(CityModelView target) {
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
            var buildingInstallationViewLOD2 = geometryFactory.cretateBuildingInstallationView(outerBuildingInstallation.getObject(), 2);
            var buildingInstallationViewLOD3 = geometryFactory.cretateBuildingInstallationView(outerBuildingInstallation.getObject(), 3);
            if (buildingInstallationViewLOD2 != null) {
                buildingInstallationViewLOD2.setLOD(2);
                building.addBuildingInstallationView(buildingInstallationViewLOD2);
            }
            if (buildingInstallationViewLOD3 != null) {
                buildingInstallationViewLOD3.setLOD(3);
                building.addBuildingInstallationView(buildingInstallationViewLOD3);
            }
        }

        // <bldg:consistsOfBuildingPart>
        for (var buildingPart : gmlObject.getConsistsOfBuildingPart()) {
            
            var partlod1SolidFactory = new LOD1SolidFactory(getTarget());
            var partlod1Solid = partlod1SolidFactory.createLOD1Solid(buildingPart.getBuildingPart());
            if (partlod1Solid != null)
                building.addBuildingPart(partlod1Solid);
                
            var partlod2SolidFactory = new LOD2SolidFactory(getTarget());
            var partlod2Solid = partlod2SolidFactory.createLOD2Solid(buildingPart.getBuildingPart());
            if (partlod2Solid != null)
                building.addBuildingPart(partlod2Solid);
                
            var partlod3SolidFactory = new LOD3SolidFactory(getTarget());
            var partlod3Solid = partlod3SolidFactory.createLOD3Solid(buildingPart.getBuildingPart());
            if (partlod3Solid != null)
                building.addBuildingPart(partlod3Solid);
        }

        building.toggleLODView(CityGMLEditorApp.getCityModelViewMode().getLOD());

        return building;
    }
}
