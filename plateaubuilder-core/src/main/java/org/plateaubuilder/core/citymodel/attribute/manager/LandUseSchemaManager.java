package org.plateaubuilder.core.citymodel.attribute.manager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.ClazzWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CreationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.DescriptionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.TerminationDateWrapper;

import javafx.scene.control.TreeItem;

/**
 * LandUse（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class LandUseSchemaManager implements AttributeSchemaManager {
        private static final JsonObject ATTRIBUTE_LANDUSE_CONFIG; // LandUse用のスキーマ
        private static final ModelType modelType = ModelType.LAND_USE;
        static {
                ATTRIBUTE_LANDUSE_CONFIG = loadSchema("/LandUseAttributeSchema.json");
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {
                LandUse selectedLandUse = (LandUse) selectedFeature.getGML();

                if (selectedLandUse.isSetDescription()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<LandUse>(selectedLandUse,
                                                        getAttributeName("description")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedLandUse.isSetName()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<LandUse>(selectedLandUse, getAttributeName("name")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedLandUse.isSetCreationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<LandUse>(selectedLandUse,
                                                        getAttributeName("creationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedLandUse.isSetTerminationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<LandUse>(selectedLandUse,
                                                        getAttributeName("terminationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedLandUse.isSetClazz()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<LandUse>(selectedLandUse, getAttributeName("class")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
        }

        public String getAttributeName(String attributeKey) {
                return ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(attributeKey).getString("name");
        }

        public String getAttributeType(String attributeKey) {
                return ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(attributeKey).getString("type");
        }

        public String getAttributeMin(String attributeKey) {
                return ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(attributeKey).getString("min");
        }

        public String getAttributeMax(String attributeKey) {
                return ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(attributeKey).getString("max");
        }

        public String getAttributeUom(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(attributeKey);

                if (attributeObject != null && attributeObject.containsKey("uom")) {
                        return attributeObject.getString("uom");
                } else {
                        return null;
                }
        }

        public String getChildAttributeName(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("name") : null;
        }

        public String getChildAttributeType(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("type") : null;
        }

        public String getChildAttributeMin(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("min") : null;
        }

        public String getChildAttributeMax(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("max") : null;
        }

        private JsonObject getChildAttribute(String parentAttributeKey, String childKey) {
                JsonObject attributeObject = ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(parentAttributeKey);
                if (attributeObject != null && attributeObject.containsKey("children")) {
                        JsonObject children = attributeObject.getJsonObject("children");
                        if (children != null && children.containsKey(childKey)) {
                                return children.getJsonObject(childKey);
                        }
                }
                return null;
        }

        /**
         * 対象属性の配下の追加可能な属性の名前リストを返します
         *
         * @param parentAttributeName ベースとなる属性の名前
         * @param addedAttributeNames 追加済の属性の名前リスト
         * @return addableAttributeNameList 追加可能な属性の名前リスト
         */
        public ArrayList<String> getAttributeNameList(String parentAttributeName,
                        ArrayList<String> addedAttributeNames) {
                ArrayList<String> addableAttributeNameList = new ArrayList<>();
                if (parentAttributeName.matches("root")) {
                        ArrayList<String> attributeNameList = getParentNames();
                        for (String attributeName : attributeNameList) {
                                if (getAttributeMax(attributeName.split(":")[1]).matches("unbounded") || Integer
                                                .parseInt(getAttributeMax(attributeName.split(":")[1])) > Collections
                                                                .frequency(addedAttributeNames, attributeName)) {
                                        addableAttributeNameList.add(attributeName);
                                }
                        }
                } else {
                        ArrayList<String> attributeNameList = getChildNames(parentAttributeName.split(":")[1],
                                        addedAttributeNames);
                        addableAttributeNameList = attributeNameList;
                }

                return addableAttributeNameList;
        }

        /**
         * 属性の名前リストを返します（子属性は考慮しない）
         *
         * @return parentNames 属性の名前リスト
         */
        public ArrayList<String> getParentNames() {
                ArrayList<String> attributeNames = new ArrayList<>();
                for (String key : ATTRIBUTE_LANDUSE_CONFIG.keySet()) {
                        JsonObject parentObject = ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(key);
                        if (parentObject != null && parentObject.containsKey("name")) {
                                attributeNames.add(parentObject.getString("name"));
                        }
                }
                return attributeNames;
        }

        /**
         * 追加可能な子属性の名前リストを返します
         *
         * @param parentAttributeName ベースとなる属性の名前
         * @param addedAttributeNames 追加済の属性の名前リスト
         * @return childNames 追加可能な子属性の名前リスト
         */
        public ArrayList<String> getChildNames(String parentAttributeName,
                        ArrayList<String> addedAttributeNames) {
                ArrayList<String> childNames = new ArrayList<>();
                JsonObject parentObject = ATTRIBUTE_LANDUSE_CONFIG.getJsonObject(parentAttributeName);
                if (parentObject == null) {
                        return childNames;
                }
                JsonObject childrenObject = parentObject.getJsonObject("children");
                for (String childKey : childrenObject.keySet()) {
                        JsonObject childObject = childrenObject.getJsonObject(childKey);
                        String childAttributeName = childObject.getString("name");
                        if (getChildAttributeMax(parentAttributeName, childAttributeName.split(":")[1])
                                        .matches("unbounded")
                                        || Integer.parseInt(getChildAttributeMax(parentAttributeName,
                                                        childAttributeName.split(":")[1])) > Collections.frequency(
                                                                        addedAttributeNames,
                                                                        childAttributeName)) {
                                childNames.add(childAttributeName);
                        }
                }
                return childNames;
        }

        /**
         * 地物に属性を追加します
         *
         * @param model            地物
         * @param addAttributeName 属性名
         * @param value            値
         */
        public AttributeItem addAttribute(AbstractCityObject model, String addAttributeName, String value) {
                LandUse landUse = (LandUse) model;
                AttributeItem attributeItem;
                switch (addAttributeName) {
                        case "gml:description":
                                new DescriptionWrapper(modelType).add(landUse, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<LandUse>(landUse,
                                                                getAttributeName("description")));
                                break;
                        case "gml:name":
                                new NameWrapper(modelType).add(landUse, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<LandUse>(landUse,
                                                                getAttributeName("name")));
                                break;
                        case "core:creationDate":
                                new CreationDateWrapper(modelType).add(landUse, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<LandUse>(landUse,
                                                                getAttributeName("creationDate")));
                                break;
                        case "core:terminationDate":
                                new TerminationDateWrapper(modelType).add(landUse, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<LandUse>(landUse,
                                                                getAttributeName("terminationDate")));
                                break;
                        case "luse:class":
                                new ClazzWrapper(modelType).add(landUse, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<LandUse>(landUse,
                                                                getAttributeName("class")));
                                break;
                        default:
                                return null;
                }
                return attributeItem;
        }

        // スキーマをロードするメソッド
        private static JsonObject loadSchema(String path) {
                try (InputStream fis = LandUseSchemaManager.class.getResourceAsStream(path);
                                JsonReader reader = Json.createReader(new InputStreamReader(fis))) {
                        if (fis == null) {
                                throw new IllegalStateException(path + " not found");
                        }
                        return reader.readObject();
                } catch (Exception e) {
                        e.printStackTrace();
                        return Json.createObjectBuilder().build(); // エラー時に空のJSONオブジェクトを返す
                }
        }

        /**
         * 地物の属性を初期化します
         *
         * @param selectedFeature 地物のIFeatureView
         */
        public List<ADEComponent> initializeAttributes(IFeatureView selectedFeature) {
                LandUse selectedModel = (LandUse) selectedFeature.getGML();

                selectedModel.unsetDescription();
                selectedModel.unsetName();
                selectedModel.unsetTerminationDate();
                selectedModel.unsetCreationDate();
                selectedModel.unsetClazz();
                selectedModel.unsetGenericApplicationPropertyOfLandUse();
                return getADEComponents(selectedFeature);
        }

        public List<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
                LandUse selectedModel = (LandUse) selectedFeature.getGML();
                return selectedModel.getGenericApplicationPropertyOfLandUse();
        }
}
