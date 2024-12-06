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
import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.citygml4j.model.citygml.generics.DateAttribute;
import org.citygml4j.model.citygml.generics.DoubleAttribute;
import org.citygml4j.model.citygml.generics.GenericAttributeSet;
import org.citygml4j.model.citygml.generics.IntAttribute;
import org.citygml4j.model.citygml.generics.MeasureAttribute;
import org.citygml4j.model.citygml.generics.StringAttribute;
import org.citygml4j.model.citygml.generics.UriAttribute;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.ClazzWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CreationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.DescriptionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.FunctionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.GenericAttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.TerminationDateWrapper;

import javafx.scene.control.TreeItem;

/**
 * WaterBody（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class WaterBodySchemaManager implements AttributeSchemaManager {
        private static final JsonObject ATTRIBUTE_WATERBODY_CONFIG; // Road用のスキーマ
        private static final ModelType modelType = ModelType.WATER_BODY;

        static {
                ATTRIBUTE_WATERBODY_CONFIG = loadSchema("/WaterBodyAttributeSchema.json");
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {
                WaterBody selectedModel = (WaterBody) selectedFeature.getGML();

                if (selectedModel.isSetDescription()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<WaterBody>(selectedModel,
                                                        getAttributeName("description")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetName()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<WaterBody>(selectedModel,
                                                        getAttributeName("name")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetCreationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<WaterBody>(selectedModel,
                                                        getAttributeName("creationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetTerminationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<WaterBody>(selectedModel,
                                                        getAttributeName("terminationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetClazz()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<WaterBody>(selectedModel,
                                                        getAttributeName("class")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetFunction()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<WaterBody>(selectedModel,
                                                        getAttributeName("function")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetGenericAttribute()) {
                        List<AbstractGenericAttribute> genericAttributes = ((WaterBody) selectedModel)
                                        .getGenericAttribute();
                        setGenericAttribute(genericAttributes, root);
                }
        }

        // AbstractGenericAttributeを再帰的に検索し、TreeItemに追加します
        public void setGenericAttribute(List<AbstractGenericAttribute> genericAttributes,
                        TreeItem<AttributeItem> root) {
                String name = "";
                String value = "";
                String type = "";
                String uom = "";
                String codeSpace = "";
                AttributeItem attributeItem = null;
                for (AbstractGenericAttribute abstractGenericAttribute : genericAttributes) {
                        if (abstractGenericAttribute instanceof IntAttribute) {
                                IntAttribute attribute = (IntAttribute) abstractGenericAttribute;
                                name = attribute.getName();
                                value = String.valueOf(attribute.getValue());
                                attributeItem = new AttributeItem(
                                                new GenericAttributeHandler(attribute,
                                                                name, value, uom, codeSpace));
                        } else if (abstractGenericAttribute instanceof DateAttribute) {
                                DateAttribute attribute = (DateAttribute) abstractGenericAttribute;
                                name = attribute.getName();
                                value = String.valueOf(attribute.getValue());
                                attributeItem = new AttributeItem(
                                                new GenericAttributeHandler(attribute,
                                                                name, value, uom, codeSpace));
                        } else if (abstractGenericAttribute instanceof DoubleAttribute) {
                                DoubleAttribute attribute = (DoubleAttribute) abstractGenericAttribute;
                                name = attribute.getName();
                                value = String.valueOf(attribute.getValue());
                                attributeItem = new AttributeItem(
                                                new GenericAttributeHandler(attribute,
                                                                name, value, uom, codeSpace));
                        } else if (abstractGenericAttribute instanceof MeasureAttribute) {
                                MeasureAttribute attribute = (MeasureAttribute) abstractGenericAttribute;
                                name = attribute.getName();
                                value = String.valueOf(attribute.getValue().getValue());
                                uom = String.valueOf(attribute.getValue().getUom());
                                attributeItem = new AttributeItem(
                                                new GenericAttributeHandler(attribute,
                                                                name, value, uom, codeSpace));
                        } else if (abstractGenericAttribute instanceof StringAttribute) {
                                StringAttribute attribute = (StringAttribute) abstractGenericAttribute;
                                name = attribute.getName();
                                value = String.valueOf(attribute.getValue());
                                attributeItem = new AttributeItem(
                                                new GenericAttributeHandler(attribute,
                                                                name, value, uom, codeSpace));
                        } else if (abstractGenericAttribute instanceof UriAttribute) {
                                UriAttribute attribute = (UriAttribute) abstractGenericAttribute;
                                name = attribute.getName();
                                value = String.valueOf(attribute.getValue());
                                attributeItem = new AttributeItem(
                                                new GenericAttributeHandler(attribute,
                                                                name, value, uom, codeSpace));
                        } else if (abstractGenericAttribute instanceof GenericAttributeSet) {
                                GenericAttributeSet attribute = (GenericAttributeSet) abstractGenericAttribute;
                                name = attribute.getName();
                                codeSpace = attribute.getCodeSpace();
                                var item = new TreeItem<>(
                                                new AttributeItem(
                                                                new GenericAttributeHandler(attribute,
                                                                                name, value, uom, codeSpace)));
                                root.getChildren().add(item);
                                setGenericAttribute(attribute.getGenericAttribute(), item);
                        }
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
        }

        public String getAttributeName(String attributeKey) {
                return ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(attributeKey).getString("name");
        }

        public String getAttributeType(String attributeKey) {
                return ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(attributeKey).getString("type");
        }

        public String getAttributeMin(String attributeKey) {
                return ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(attributeKey).getString("min");
        }

        public String getAttributeMax(String attributeKey) {
                return ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(attributeKey).getString("max");
        }

        public String getAttributeUom(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(attributeKey);

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
                JsonObject attributeObject = ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(parentAttributeKey);
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
                for (String key : ATTRIBUTE_WATERBODY_CONFIG.keySet()) {
                        JsonObject parentObject = ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(key);
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
                JsonObject parentObject = ATTRIBUTE_WATERBODY_CONFIG.getJsonObject(parentAttributeName);
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
                WaterBody waterBody = (WaterBody) model;
                AttributeItem attributeItem;
                switch (addAttributeName) {
                        case "gml:description":
                                new DescriptionWrapper(modelType).add(waterBody, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<WaterBody>(waterBody,
                                                                getAttributeName("description")));
                                break;
                        case "gml:name":
                                new NameWrapper(modelType).add(waterBody, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<WaterBody>(waterBody,
                                                                getAttributeName("name")));
                                break;
                        case "core:creationDate":
                                new CreationDateWrapper(modelType).add(waterBody, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<WaterBody>(waterBody,
                                                                getAttributeName("creationDate")));
                                break;
                        case "core:terminationDate":
                                new TerminationDateWrapper(modelType).add(waterBody, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<WaterBody>(waterBody,
                                                                getAttributeName("terminationDate")));
                                break;
                        case "wtl:class":
                                new ClazzWrapper(modelType).add(waterBody, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<WaterBody>(waterBody,
                                                                getAttributeName("class")));
                                break;
                        case "wtl:function":
                                new FunctionWrapper(modelType).add(waterBody, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<WaterBody>(waterBody,
                                                                getAttributeName("function")));
                                break;
                        default:
                                return null;
                }
                return attributeItem;
        }

        // スキーマをロードするメソッド
        private static JsonObject loadSchema(String path) {
                try (InputStream fis = WaterBodySchemaManager.class.getResourceAsStream(path);
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
                WaterBody selectedModel = (WaterBody) selectedFeature.getGML();

                selectedModel.unsetDescription();
                selectedModel.unsetName();
                selectedModel.unsetTerminationDate();
                selectedModel.unsetCreationDate();
                selectedModel.unsetClazz();
                selectedModel.unsetFunction();
                selectedModel.unsetGenericApplicationPropertyOfWaterBody();

                return getADEComponents(selectedFeature);
        }

        public List<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
                WaterBody selectedBuilding = (WaterBody) selectedFeature.getGML();
                return selectedBuilding.getGenericApplicationPropertyOfWaterBody();
        }
}
