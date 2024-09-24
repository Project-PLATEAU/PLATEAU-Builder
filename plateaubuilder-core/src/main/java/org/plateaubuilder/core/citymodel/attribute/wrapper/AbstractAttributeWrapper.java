package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateaubuilder.core.citymodel.attribute.manager.BuildingSchemaManager;

/**
 * 各属性ごとの追加・削除などの処理を実装するクラスの抽象クラス
 */
public abstract class AbstractAttributeWrapper {
    protected String name;
    protected String type;
    protected String min;
    protected String max;

    /**
     * 属性情報を初期化する
     * 
     * @param attributeName 属性名
     */
    protected void initialize(String attributeName) {
        this.name = BuildingSchemaManager.getAttributeName(attributeName);
        this.type = BuildingSchemaManager.getAttributeType(attributeName);
        this.min = BuildingSchemaManager.getAttributeMin(attributeName);
        this.max = BuildingSchemaManager.getAttributeMax(attributeName);
    }

    /**
     * 属性情報を初期化する
     * 
     * @param attributeName       属性名
     * @param parentAttributeName 親属性名
     */
    protected void initialize(String attributeName, String parentAttributeName) {
        this.name = BuildingSchemaManager.getChildAttributeName(parentAttributeName, attributeName);
        this.type = BuildingSchemaManager.getChildAttributeType(parentAttributeName, attributeName);
        this.min = BuildingSchemaManager.getChildAttributeMax(parentAttributeName, attributeName);
        this.max = BuildingSchemaManager.getChildAttributeMin(parentAttributeName, attributeName);
    }

    public String getName() {
        return name;
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

    public void setUom(Object obj, String value) {
        // サブクラスで必要に応じて実装
    }

    public String getUom(Object obj) {
        // サブクラスで必要に応じて実装
        return null;
    }

    public String getCodeSpace(Object obj) {
        // サブクラスで必要に応じて実装
        return null;
    }

    public void setCodeSpace(Object obj, String value) {
        // サブクラスで必要に応じて実装
    }

    public abstract void setValue(Object obj, String value);

    public abstract String getValue(Object obj);

    /**
     * 属性を地物から削除する抽象メソッド
     */
    public abstract void remove(Object obj);

    /**
     * 属性を地物に追加する抽象メソッド
     */
    public abstract void add(Object obj, String value);
}