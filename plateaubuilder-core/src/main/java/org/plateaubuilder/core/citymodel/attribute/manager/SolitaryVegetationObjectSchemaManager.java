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
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.ClazzWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CreationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CrownDiameterWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.FunctionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.HeightWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.TerminationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.TrunkDiameterWrapper;

import javafx.scene.control.TreeItem;

/**
 * SolitaryVegetationObject（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class SolitaryVegetationObjectSchemaManager implements AttributeSchemaManager {
        private static final JsonObject ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG; // Road用のスキーマ
        private static final ModelType modelType = ModelType.SOLITARY_VEGETATION_OBJECT;

        static {
                ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG = loadSchema(
                                "/SolitaryVegetationObjectAttributeSchema.json");
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {
                SolitaryVegetationObject selectedModel = (SolitaryVegetationObject) selectedFeature.getGML();

                if (selectedModel.isSetName()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("name")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetCreationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("creationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetTerminationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("terminationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetClazz()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("class")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetHeight()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("height")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetTrunkDiameter()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("trunkDiameter")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetCrownDiameter()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<SolitaryVegetationObject>(selectedModel,
                                                        getAttributeName("crownDiameter")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
        }

        public String getAttributeName(String attributeKey) {
                return ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(attributeKey).getString("name");
        }

        public String getAttributeType(String attributeKey) {
                return ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(attributeKey).getString("type");
        }

        public String getAttributeMin(String attributeKey) {
                return ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(attributeKey).getString("min");
        }

        public String getAttributeMax(String attributeKey) {
                return ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(attributeKey).getString("max");
        }

        public String getAttributeUom(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(attributeKey);

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
                JsonObject attributeObject = ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG
                                .getJsonObject(parentAttributeKey);
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
                for (String key : ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.keySet()) {
                        JsonObject parentObject = ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(key);
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
                JsonObject parentObject = ATTRIBUTE_SOLITARYVEGETATIONOBJECT_CONFIG.getJsonObject(parentAttributeName);
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
        public void addAttribute(AbstractCityObject model, String addAttributeName, String value) {
                SolitaryVegetationObject solitaryVegetationObject = (SolitaryVegetationObject) model;
                switch (addAttributeName) {
                        case "gml:name":
                                new NameWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "core:creationDate":
                                new CreationDateWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "core:terminationDate":
                                new TerminationDateWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "veg:class":
                                new ClazzWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "veg:function":
                                new FunctionWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "veg:height":
                                new HeightWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "veg:trunkDiameter":
                                new TrunkDiameterWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        case "veg:crownDiameter":
                                new CrownDiameterWrapper(modelType).add(solitaryVegetationObject, value);
                                break;
                        default:
                                return;
                }
        }

        // スキーマをロードするメソッド
        private static JsonObject loadSchema(String path) {
                try (InputStream fis = SolitaryVegetationObjectSchemaManager.class.getResourceAsStream(path);
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
                SolitaryVegetationObject selectedModel = (SolitaryVegetationObject) selectedFeature.getGML();

                selectedModel.unsetName();
                selectedModel.unsetTerminationDate();
                selectedModel.unsetCreationDate();
                selectedModel.unsetHeight();
                selectedModel.unsetTrunkDiameter();
                selectedModel.unsetCrownDiameter();
                selectedModel.unsetGenericApplicationPropertyOfSolitaryVegetationObject();
                return getADEComponents(selectedFeature);
        }

        public List<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
                SolitaryVegetationObject selectedBuilding = (SolitaryVegetationObject) selectedFeature.getGML();
                return selectedBuilding.getGenericApplicationPropertyOfSolitaryVegetationObject();
        }
}
