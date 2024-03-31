package org.plateau.plateaubuilder.plateaubuilder.fxml;

import org.citygml4j.model.citygml.CityGMLClass;

public class UIConstants {
    public static String buildingTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
            case BUILDING: return "建築物(bldg:Building)";
            case BUILDING_WALL_SURFACE: return "壁面(bldg:WallSurface)";
            case BUILDING_ROOF_SURFACE: return "屋根(bldg:RoofSurface)";
            case BUILDING_GROUND_SURFACE: return "地面(bldg:GroundSurface)";
            case OUTER_BUILDING_CEILING_SURFACE: return "屋外天井面(bldg:OuterCeilingSurface)";
            case OUTER_BUILDING_FLOOR_SURFACE: return "屋外床面(bldg:OuterFloorSurface)";
            case BUILDING_WINDOW: return "窓(bldg:Window)";
            case BUILDING_DOOR: return "ドア(bldg:Door)";
            case BUILDING_PART: return "建築物部品(bldg:BuildingPart)";
            case BUILDING_CLOSURE_SURFACE: return "閉鎖面(bldg:ClosureSurface)";
            case BUILDING_INSTALLATION: return "屋外付属物(bldg:BuildingInstallation)";
        }

        throw new IllegalArgumentException();
    }

    public static String buildingTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
            case BUILDING: return "建築物";
            case BUILDING_WALL_SURFACE: return "壁面";
            case BUILDING_ROOF_SURFACE: return "屋根";
            case BUILDING_GROUND_SURFACE: return "地面";
            case OUTER_BUILDING_CEILING_SURFACE: return "屋外天井面";
            case OUTER_BUILDING_FLOOR_SURFACE: return "屋外床面";
            case BUILDING_WINDOW: return "窓";
            case BUILDING_DOOR: return "ドア";
            case BUILDING_PART: return "建築物部品";
            case BUILDING_CLOSURE_SURFACE: return "閉鎖面";
            case BUILDING_INSTALLATION: return "屋外付属物";
        }

        throw new IllegalArgumentException();
    }
}
