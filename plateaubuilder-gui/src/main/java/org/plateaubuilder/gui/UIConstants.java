package org.plateaubuilder.gui;

import java.util.HashMap;
import java.util.Map;

import org.citygml4j.model.citygml.CityGMLClass;

public class UIConstants {
    public static final String buildingTypeDescription(CityGMLClass clazz) {
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

    public static final String buildingTypeDescriptionShort(CityGMLClass clazz) {
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

    public static final String buildingInstallationTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
        case BUILDING_INSTALLATION:
            return "屋外付属物(bldg:BuildingInstallation)";
        }

        throw new IllegalArgumentException();
    }

    public static final String uildingInstallationTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
        case BUILDING_INSTALLATION:
            return "屋外付属物";
        }

        throw new IllegalArgumentException();
    }

    public static final String roadTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
            case TRANSPORTATION_COMPLEX: return "TRANSPORTATION_COMPLEX";
            case AUXILIARY_TRAFFIC_AREA: return "AUXILIARY_TRAFFIC_AREA";
            case RAILWAY: return "RAILWAY";
            case ROAD: return "道路(tran:Road)";
            case SQUARE: return "SQUARE";
            case TRACK: return "TRACK";
            case TRAFFIC_AREA: return "通行可能な領域(tran::TrafficArea)";
            case AUXILIARY_TRAFFIC_AREA_PROPERTY: return "補助する役割をもつ領域(tran::AuxiliaryTrafficAre)";
            case TRAFFIC_AREA_PROPERTY: return "TRAFFIC_AREA_PROPERTY";
        }

        throw new IllegalArgumentException();
    }

    public static final String roadTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
            case TRANSPORTATION_COMPLEX: return "TRANSPORTATION_COMPLEX";
            case AUXILIARY_TRAFFIC_AREA: return "AUXILIARY_TRAFFIC_AREA";
            case RAILWAY: return "RAILWAY";
            case ROAD: return "道路";
            case SQUARE: return "SQUARE";
            case TRACK: return "TRACK";
            case TRAFFIC_AREA: return "通行可能な領域";
            case AUXILIARY_TRAFFIC_AREA_PROPERTY: return "補助する役割をもつ領域";
            case TRAFFIC_AREA_PROPERTY: return "TRAFFIC_AREA_PROPERTY";
        }

        throw new IllegalArgumentException();
    }

    public static final String landUseTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
        case LAND_USE:
            return "土地利用用途(luse:LandUse)";
        }

        throw new IllegalArgumentException();
    }

    public static final String landUseTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
        case LAND_USE:
            return "土地利用用途";
        }

        throw new IllegalArgumentException();
    }

    public static final String waterBodyTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
        case WATER_BODY:
            return "浸水想定区域(wtr:WaterBody)";
        }

        throw new IllegalArgumentException();
    }

    public static final String waterBodyTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
        case LAND_USE:
            return "浸水想定区域";
        }

        throw new IllegalArgumentException();
    }

    public static final String solitaryVegetationObjectTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
        case SOLITARY_VEGETATION_OBJECT:
            return "植生(veg:SolitaryVegetationObject)";
        }

        throw new IllegalArgumentException();
    }

    public static final String solitaryVegetationObjectTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
        case SOLITARY_VEGETATION_OBJECT:
            return "植生";
        }

        throw new IllegalArgumentException();
    }

    public static final String plantCoverTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
        case PLANT_COVER:
            return "植生(veg:PlantCover)";
        }

        throw new IllegalArgumentException();
    }

    public static final String plantCoverTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
        case PLANT_COVER:
            return "植生";
        }

        throw new IllegalArgumentException();
    }

    public static final String cityFurnitureTypeDescription(CityGMLClass clazz) {
        switch (clazz) {
        case CITY_FURNITURE:
            return "都市設備(frn:CityFurniture)";
        }

        throw new IllegalArgumentException();
    }

    public static final String cityFurnitureTypeDescriptionShort(CityGMLClass clazz) {
        switch (clazz) {
        case CITY_FURNITURE:
            return "都市設備";
        }

        throw new IllegalArgumentException();
    }

    private static final Map<String, String> genericComponentTypeDescription = new HashMap<String, String>() {
        {
            put("urf:UrbanPlanningArea", "都市計画区域");
            put("urf:QuasiUrbanPlanningArea", "準都市計画区域");
            put("urf:AreaClassification", "区域区分");
            put("urf:DistrictsAndZones", "地域地区");
            put("urf:UseDistrict", "用途地域");
            put("urf:SpecialUseDistrict", "特別用途地区");
            put("urf:SpecialUseRestrictionDistrict", "特定用途制限地域");
            put("urf:ExceptionalFloorAreaRateDistrict", "特例容積率適用地区");
            put("urf:HighRiseResidentialAttractionDistrict", "高層住居誘導地区");
            put("urf:HeightControlDistrict", "高度地区");
            put("urf:HighLevelUseDistrict", "高度利用地区");
            put("urf:SpecifiedBlock", "特定街区");
            put("urf:SpecialUrbanRenaissanceDistrict", "都市再生特別地区");
            put("urf:HousingControlArea", "居住調整地域");
            put("urf:ResidentialEnvironmentImprovementDistrict", "居住環境向上用途誘導地区");
            put("urf:SpecialUseAttractionDistrict", "特定用途誘導地区");
            put("urf:FirePreventionDistrict", "防火地域又は準防火地域");
            put("urf:SpecifiedDisasterPreventionBlockImprovementZone", "特定防災街区整備地区");
            put("urf:LandscapeZone", "景観地区");
            put("urf:ScenicDistrict", "風致地区");
            put("urf:ParkingPlaceDevelopmentZone", "駐車場整備地区");
            put("urf:PortZone", "臨港地区");
            put("urf:SpecialZoneForPreservationOfHistoricalLandscape", "歴史的風土特別保存地区");
            put("urf:ZoneForPreservationOfHistoricalLandscape", "第一種歴史的風土保存地区又は第二種歴史的風土保存地区");
            put("urf:GreenSpaceConservationDistrict", "緑地保全地域");
            put("urf:SpecialGreenSpaceConservationDistrict", "特別緑地保全地域");
            put("urf:TreePlantingDistrict", "緑化地域");
            put("urf:DistributionBusinessZone", "流通業務地区");
            put("urf:ProductiveGreenZone", "生産緑地地区");
            put("urf:ConservationZoneForClustersOfTraditionalStructures", "伝統的建造物群保存地区");
            put("urf:AircraftNoiseControlZoneurf:AircraftNoiseControlZone", "航空機騒音障害防止地区又は航空機騒音障害防止特別地区");
            put("urf:ProjectPromotionArea", "促進区域");
            put("urf:UrbanRedevelopmentPromotionArea", "市街地再開発促進区域");
            put("urf:LandReadjustmentPromotionArea", "土地区画整理促進区域");
            put("urf:ResidentialBlockConstructionPromotionArea", "住宅街区整備促進区域");
            put("urf:LandReadjustmentPromotionAreasForCoreBusinessUrbanDevelopment", "拠点業務市街地整備土地区画整理促進区域");
            put("urf:UnusedLandUsePromotionArea", "遊休土地転換利用促進地区");
            put("urf:UrbanDisasterRecoveryPromotionArea", "被災市街地復興推進地域");
            put("urf:UrbanFacility", "都市施設");
            put("urf:TrafficFacility", "交通施設");
            put("urf:OpenSpaceForPublicUse", "公共空地");
            put("urf:SupplyFacility", "供給施設及び処理施設");
            put("urf:TreatmentFacility", "供給施設及び処理施設");
            put("urf:Waterway", "水路");
            put("urf:EducationalAndCulturalFacility", "教育文化施設");
            put("urf:MedicalFacility", "医療施設及び社会福祉施設");
            put("urf:SocialWelfareFacility", "医療施設及び社会福祉施設");
            put("urf:MarketsSlaughterhousesCrematoria", "市場、と畜場、火葬場");
            put("urf:CollectiveHousingFacilities", "一団地の住宅施設");
            put("urf:CollectiveGovernmentAndPublicOfficeFacilities", "一団地の官公庁施設");
            put("urf:DistributionBusinessPark", "流通業務団地");
            put("urf:CollectiveFacilitiesForTsunamiDisasterPrevention", "一団地の津波防災拠点市街地形成施設");
            put("urf:CollectiveFacilitiesForReconstructionAndRevitalization", "一団地の復興再生拠点市街地形成施設");
            put("urf:CollectiveFacilitiesForReconstruction", "一団地の復興拠点市街地形成施設");
            put("urf:CollectiveUrbanDisasterPreventionFacilities", "一団地の都市安全確保拠点施設");
            put("urf:UrbanFacilityStipulatedByCabinetOrder", "政令で定める都市施設");
            put("urf:TelecommunicationFacility", "電気通信施設");
            put("urf:WindProtectionFacility", "防風施設");
            put("urf:FireProtectionFacility", "防火施設");
            put("urf:TideFacility", "防潮施設");
            put("urf:FloodPreventionFacility", "防水施設");
            put("urf:SnowProtectionFacility", "防雪施設");
            put("urf:SandControlFacility", "防砂施設");
            put("urf:UrbanDevelopmentProject", "市街地開発事業");
            put("urf:LandReadjustmentProject", "土地区画整理事業");
            put("urf:NewHousingAndUrbanDevelopmentProject", "新住宅市街地開発事業");
            put("urf:IndustrialParkDevelopmentProject", "工業団地造成事業");
            put("urf:UrbanRedevelopmentProject", "市街地再開発事業");
            put("urf:NewUrbanInfrastructureProject", "新都市基盤整備事業");
            put("urf:ResidentialBlockConstructionProject", "住宅街区整備事業");
            put("urf:DisasterPreventionBlockImprovementProject", "防災街区整備事業");
            put("urf:UrbanRenewalProject", "市街地改造事業");
            put("urf:ScheduledAreaForUrbanDevelopmentProject", "市街地開発事業等の予定区域");
            put("urf:ScheduledAreaForNewHousingAndUrbanDevelopmentProjects", "新住宅市街地開発事業の予定区域");
            put("urf:ScheduledAreaForIndustrialParkDevelopmentProjects", "工業団地造成事業の予定区域");
            put("urf:ScheduledAreaForNewUrbanInfrastructureProjects", "新都市基盤整備事業の予定区域");
            put("urf:ScheduledAreaForCollectiveHousingFacilities", "一団地の住宅施設の予定区域");
        }
    };

    public static final String genericComponentTypeDescription(String nodeName) {
        var description = genericComponentTypeDescription.get(nodeName);
        return String.format("%s(%s)", description, nodeName);
    }

    public static final String genericComponentTypeDescriptionShort(String nodeName) {
        var description = genericComponentTypeDescription.get(nodeName);
        return description;
    }
}
