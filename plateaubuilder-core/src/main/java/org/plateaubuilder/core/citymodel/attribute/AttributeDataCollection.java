package org.plateaubuilder.core.citymodel.attribute;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javafx.scene.control.TreeItem;

public class AttributeDataCollection {
    private Map<String, TreeItem<AttributeItem>> attributeDataCollection = new HashMap<>();

    /*
     * キー（地物ID）のリストを返します。
     */
    public Set<String> getKey() {
        return attributeDataCollection.keySet();
    }

    /*
     * 特定のモデルの属性情報を返します。
     */
    public TreeItem<AttributeItem> getData(String key) {
        return attributeDataCollection.get(key);
    }

    /*
     * 全情報を返します
     */ public Map<String, TreeItem<AttributeItem>> getAllData() {
        return attributeDataCollection;
    }

    /*
     * モデルデータを追加します。
     */
    public void add(String id, TreeItem<AttributeItem> attributeTree) {
        attributeDataCollection.put(id, attributeTree);
    }

}
