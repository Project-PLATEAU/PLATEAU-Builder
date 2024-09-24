package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.w3c.dom.Node;

/**
 * 地物に対する属性追加や地物が持つ属性の削除・編集などの処理を行うクラス
 * 属性事に追加・削除などのメソッドが異なるため、それらの操作を抽象化し提供します。
 */
public class AttributeHandler {
    private AbstractBuilding building;
    private AbstractAttributeWrapper wrapper;
    private String name;
    private String type;
    private String min;
    private String max;
    private String value;

    /**
     * 地物に対する属性追加や地物が持つ属性の削除・編集などの処理を行うクラス
     * 属性名に合わせて、各属性事の属性操作処理を持つクラスをセットします。
     *
     * @param building      地物（AbstractBuilding）
     * @param attributeName 属性名
     */
    public AttributeHandler(AbstractBuilding building, String attributeName) {
        this.building = building;
        switch (attributeName) {
            case "gml:description":
                wrapper = new DescriptionWrapper();
                break;
            case "gml:name":
                wrapper = new NameWrapper();
                break;
            case "core:creationDate":
                wrapper = new CreationDateWrapper();
                break;
            case "core:terminationDate":
                wrapper = new TerminationDateWrapper();
                break;
            case "bldg:class":
                wrapper = new ClazzWrapper();
                break;
            case "bldg:usage":
                wrapper = new UsageWrapper();
                break;
            case "bldg:yearOfConstruction":
                wrapper = new YearOfConstructionWrapper();
                break;
            case "bldg:roofType":
                wrapper = new RoofTypeWrapper();
                break;
            case "bldg:measuredHeight":
                wrapper = new MeasuredHeightWrapper();
                break;
            case "bldg:storeysBelowGround":
                wrapper = new StoreysBelowGroundWrapper();
                break;
            case "bldg:storeysAboveGround":
                wrapper = new StoreysAboveGroundWrapper();
                break;
            case "bldg:address":
                wrapper = new AddressWrapper();
                break;
            case "xAL:CountryName":
                wrapper = new CountryNameWrapper();
                break;
            case "xAL:LocalityName":
                wrapper = new LocalityNameWrapper();
                break;
            default:
                return;
        }
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

    public String getName() {
        return name;
    }

    public String getValue() {
        return wrapper.getValue(building);
    }

    public void setValue(String value) {
        wrapper.setValue(building, value);
    }

    public String getUom() {
        return wrapper.getUom(building);
    }

    public void setUom(String uom) {
        wrapper.setUom(building, uom);
    }

    public String getCodeSpace() {
        return wrapper.getCodeSpace(building);
    }

    public void setCodeSpace(String codeSpace) {
        wrapper.setCodeSpace(building, codeSpace);
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
    public Object getContent() {
        return building;
    }

    /**
     * 属性を地物から削除します
     */
    public void remove() {
        wrapper.remove(building);
    }

    /**
     * 属性を地物に追加します
     */
    public void add() {
        wrapper.add(building, value);
    }
}
