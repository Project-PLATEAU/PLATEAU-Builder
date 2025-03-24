package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManagerFactory;
import org.plateaubuilder.core.citymodel.attribute.manager.ModelType;

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
     * @param modelType     地物の型
     * @param attributeName 属性名
     */
    protected void initialize(ModelType modelType, String attributeName) {
        AttributeSchemaManager schemaManager = AttributeSchemaManagerFactory.getSchemaManager(modelType);
        initialize(schemaManager, attributeName);
    }

    /**
     * 属性情報を初期化する
     * 
     * @param modelType           地物の型
     * @param attributeName       属性名
     * @param parentAttributeName 親属性名
     */
    protected void initialize(ModelType modelType, String attributeName, String parentAttributeName) {
        AttributeSchemaManager schemaManager = AttributeSchemaManagerFactory.getSchemaManager(modelType);
        initialize(schemaManager, attributeName, parentAttributeName);
    }

    protected void initialize(AttributeSchemaManager schemaManager, String attributeName) {
        this.name = schemaManager.getAttributeName(attributeName);
        this.type = schemaManager.getAttributeType(attributeName);
        this.min = schemaManager.getAttributeMin(attributeName);
        this.max = schemaManager.getAttributeMax(attributeName);
    }

    protected void initialize(AttributeSchemaManager schemaManager, String attributeName, String parentAttributeName) {
        this.name = schemaManager.getChildAttributeName(parentAttributeName, attributeName);
        this.type = schemaManager.getChildAttributeType(parentAttributeName, attributeName);
        this.min = schemaManager.getChildAttributeMin(parentAttributeName, attributeName);
        this.max = schemaManager.getChildAttributeMax(parentAttributeName, attributeName);
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