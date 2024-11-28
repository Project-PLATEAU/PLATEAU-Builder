package org.plateaubuilder.core.citymodel.attribute.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;

public class AttributeSchemaManagerFactory {
    private static final Map<ModelType, Supplier<AttributeSchemaManager>> managerMap = new HashMap<>();
    private static final AttributeSchemaManager BuildingSchemaManager = null;

    static {
        managerMap.put(ModelType.BUILDING, BuildingSchemaManager::new);
        managerMap.put(ModelType.ROAD, RoadSchemaManager::new);
        managerMap.put(ModelType.LAND_USE, LandUseSchemaManager::new);
        managerMap.put(ModelType.SOLITARY_VEGETATION_OBJECT, SolitaryVegetationObjectSchemaManager::new);
        managerMap.put(ModelType.WATER_BODY, WaterBodySchemaManager::new);
        managerMap.put(ModelType.CITY_FURNITURE, CityFurnitureSchemaManager::new);
        managerMap.put(ModelType.PLANT_COVER, PlantCoverSchemaManager::new);
    }

    public static AttributeSchemaManager getSchemaManager(ModelType modelType) {
        Supplier<AttributeSchemaManager> managerSupplier = managerMap.get(modelType);
        if (managerSupplier != null) {
            return managerSupplier.get();
        }
        throw new IllegalArgumentException("Unknown model type: " + modelType);
    }

    public static AttributeSchemaManager getSchemaManager(AbstractCityObject feature) {
        if (feature instanceof AbstractBuilding) {
            return new BuildingSchemaManager();
        } else if (feature instanceof Road) {
            return new RoadSchemaManager();
        } else if (feature instanceof LandUse) {
            return new LandUseSchemaManager();
        } else if (feature instanceof WaterBody) {
            return new WaterBodySchemaManager();
        } else if (feature instanceof SolitaryVegetationObject) {
            return new SolitaryVegetationObjectSchemaManager();
        } else if (feature instanceof CityFurniture) {
            return new CityFurnitureSchemaManager();
        } else if (feature instanceof PlantCover) {
            return new PlantCoverSchemaManager();
        } else {
            return new UrbanPlanningAreaSchemaManager();
        }
    }
}