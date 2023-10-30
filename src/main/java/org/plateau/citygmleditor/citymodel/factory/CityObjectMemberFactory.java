package org.plateau.citygmleditor.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.plateau.citygmleditor.citymodel.Building;
import org.plateau.citygmleditor.citymodel.CityModel;

import java.util.Random;

public class CityObjectMemberFactory extends CityGMLFactory {
    protected CityObjectMemberFactory(CityModel target) {
        super(target);
    }

    public Building createBuilding(AbstractBuilding gmlObject) {
        var building = new Building(gmlObject);
        building.setId(gmlObject.getId());

        var geometryFactory = new GeometryFactory(getTarget());
        var lod1Solid = geometryFactory.createLOD1Solid((Solid) gmlObject.getLod1Solid().getObject());
        building.setLOD1Solid(lod1Solid);
        var lod2Solid = geometryFactory.createLOD2Solid(gmlObject);
        if (lod2Solid != null) {
            lod1Solid.setVisible(false);

            building.setLOD2Solid(lod2Solid);

//            Random rand = new Random();
//            int num = rand.nextInt(100);
//            if (num > 16)
//                lod2Solid.setVisible(false);
        }

        return building;
    }
}
