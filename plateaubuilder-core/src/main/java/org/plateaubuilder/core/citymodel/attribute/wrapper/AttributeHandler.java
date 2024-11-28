package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.w3c.dom.Node;

/**
 * 地物に対する属性追加や地物が持つ属性の削除・編集などの処理を行うクラス
 * 属性事に追加・削除などのメソッドが異なるため、それらの操作を抽象化し提供します。
 */
public class AttributeHandler<T> {
    private T model;
    private AbstractAttributeWrapper wrapper;
    private String name;
    private String type;
    private String min;
    private String max;

    /**
     * 地物に対する属性追加や地物が持つ属性の削除・編集などの処理を行うクラス
     * 属性名に合わせて、各属性事の属性操作処理を持つクラスをセットします。
     *
     * @param model         地物
     * @param attributeName 属性名
     */
    public AttributeHandler(T model, String attributeName) {
        this.model = model;
        if (model instanceof AbstractBuilding) {
            initializeForBuilding((AbstractBuilding) model, attributeName);
        } else if (model instanceof TransportationComplex) {
            initializeForRoad((TransportationComplex) model, attributeName);
        } else if (model instanceof LandUse) {
            initializeForLandUse((LandUse) model, attributeName);
        } else if (model instanceof WaterBody) {
            initializeForWaterBody((WaterBody) model, attributeName);
        } else if (model instanceof SolitaryVegetationObject) {
            initializeForSolitaryVegetationObject((SolitaryVegetationObject) model, attributeName);
        } else if (model instanceof PlantCover) {
            initializeForPlantCover((PlantCover) model, attributeName);
        } else if (model instanceof CityFurniture) {
            initializeForCityFurniture((CityFurniture) model, attributeName);
        } else if (model instanceof ADEGenericComponent) {
        } else if (model instanceof AbstractGenericAttribute) {
            name = attributeName;
        }
    }

    private void initializeForBuilding(AbstractBuilding building, String attributeName) {
        initialize(ModelType.BUILDING, attributeName);
        setCommonAttributes();
    }

    private void initializeForRoad(TransportationComplex road, String attributeName) {
        initialize(ModelType.ROAD, attributeName);
        setCommonAttributes();
    }

    private void initializeForLandUse(LandUse landUse, String attributeName) {
        initialize(ModelType.LAND_USE, attributeName);
        setCommonAttributes();
    }

    private void initializeForSolitaryVegetationObject(SolitaryVegetationObject solitaryVegetationObject,
            String attributeName) {
        initialize(ModelType.SOLITARY_VEGETATION_OBJECT, attributeName);
        setCommonAttributes();
    }

    private void initializeForPlantCover(PlantCover plantCover,
            String attributeName) {
        initialize(ModelType.PLANT_COVER, attributeName);
        setCommonAttributes();
    }

    private void initializeForWaterBody(WaterBody WaterBody,
            String attributeName) {
        initialize(ModelType.WATER_BODY, attributeName);
        setCommonAttributes();
    }

    private void initializeForCityFurniture(CityFurniture CityFurniture,
            String attributeName) {
        initialize(ModelType.CITY_FURNITURE, attributeName);
        setCommonAttributes();
    }

    private void initialize(ModelType modelType, String attributeName) {
        switch (attributeName) {
            case "gml:description":
                wrapper = new DescriptionWrapper(modelType);
                break;
            case "gml:name":
                wrapper = new NameWrapper(modelType);
                break;
            case "core:creationDate":
                wrapper = new CreationDateWrapper(modelType);
                break;
            case "core:terminationDate":
                wrapper = new TerminationDateWrapper(modelType);
                break;
            case "bldg:class":
            case "tran:class":
            case "luse:class":
            case "veg:class":
            case "wtr:class":
            case "frn:class":
                wrapper = new ClazzWrapper(modelType);
                break;
            case "bldg:usage":
            case "tran:usage":
            case "frn:usage":
                wrapper = new UsageWrapper(modelType);
                break;
            case "bldg:measuredHeight":
                wrapper = new MeasuredHeightWrapper(modelType);
                break;
            case "bldg:yearOfConstruction":
                wrapper = new YearOfConstructionWrapper(modelType);
                break;
            case "bldg:yearOfDemolition":
                wrapper = new YearOfDemolitionWrapper(modelType);
                break;
            case "bldg:roofType":
                wrapper = new RoofTypeWrapper(modelType);
                break;
            case "bldg:storeysBelowGround":
                wrapper = new StoreysBelowGroundWrapper(modelType);
                break;
            case "bldg:storeysAboveGround":
                wrapper = new StoreysAboveGroundWrapper(modelType);
                break;
            case "bldg:address":
                wrapper = new AddressWrapper(modelType);
                break;
            case "xAL:CountryName":
                wrapper = new CountryNameWrapper(modelType);
                break;
            case "xAL:LocalityName":
                wrapper = new LocalityNameWrapper(modelType);
                break;
            case "tran:function":
            case "veg:function":
            case "wtr:function":
            case "frn:function":
                wrapper = new FunctionWrapper(modelType);
                break;
            case "veg:height":
                wrapper = new HeightWrapper(modelType);
                break;
            case "veg:trunkDiameter":
                wrapper = new TrunkDiameterWrapper(modelType);
                break;
            case "veg:crownDiameter":
                wrapper = new CrownDiameterWrapper(modelType);
                break;
            case "veg:averageHeight":
                wrapper = new AverageHeightWrapper(modelType);
                break;
            default:
                System.out.println("default");
        }
    }

    private void setCommonAttributes() {
        name = wrapper.getName();
        type = wrapper.getType();
        min = wrapper.getMin();
        max = wrapper.getMax();
    }

    public AttributeHandler(Node node, String type) {
    }

    public AttributeHandler(IFeatureView building, String name) {
        this.name = name;
    }

    public AttributeHandler(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return wrapper.getValue(model);
    }

    public void setValue(String value) {
        wrapper.setValue(model, value);
    }

    public String getUom() {
        return wrapper.getUom(model);
    }

    public void setUom(String uom) {
        wrapper.setUom(model, uom);
    }

    public String getCodeSpace() {
        return wrapper.getCodeSpace(model);
    }

    public void setCodeSpace(String codeSpace) {
        wrapper.setCodeSpace(model, codeSpace);
    }

    public String getType() {
        return type;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    /**
     * 地物情報を取得します
     */
    public T getContent() {
        return model;
    }

    /**
     * 属性を地物から削除します
     */
    public void remove() {
        wrapper.remove(model);
    }

    /**
     * 属性を地物に追加します
     */
    public void add(String value) {
        wrapper.add(model, value);
    }
}
