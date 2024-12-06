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
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AverageHeightWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.ClazzWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CreationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.DescriptionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.FunctionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.GenericAttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.TerminationDateWrapper;

import javafx.scene.control.TreeItem;

/**
 * PlantCover（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class PlantCoverSchemaManager implements AttributeSchemaManager {
        private static final JsonObject ATTRIBUTE_PLANTCOVER_CONFIG; // PlantCover用のスキーマ
        private static final ModelType modelType = ModelType.PLANT_COVER;

        static {
                ATTRIBUTE_PLANTCOVER_CONFIG = loadSchema("/PlantCoverAttributeSchema.json");
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {
                PlantCover selectedModel = (PlantCover) selectedFeature.getGML();

                if (selectedModel.isSetCreationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<PlantCover>(selectedModel,
                                                        getAttributeName("creationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetTerminationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<PlantCover>(selectedModel,
                                                        getAttributeName("terminationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetClazz()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<PlantCover>(selectedModel,
                                                        getAttributeName("class")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedModel.isSetAverageHeight()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<PlantCover>(selectedModel,
                                                        getAttributeName("averageHeight")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
        }

        public String getAttributeName(String attributeKey) {
                return ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(attributeKey).getString("name");
        }

        public String getAttributeType(String attributeKey) {
                return ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(attributeKey).getString("type");
        }

        public String getAttributeMin(String attributeKey) {
                return ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(attributeKey).getString("min");
        }

        public String getAttributeMax(String attributeKey) {
                return ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(attributeKey).getString("max");
        }

        public String getAttributeUom(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(attributeKey);

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
                JsonObject attributeObject = ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(parentAttributeKey);
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
                for (String key : ATTRIBUTE_PLANTCOVER_CONFIG.keySet()) {
                        JsonObject parentObject = ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(key);
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
                JsonObject parentObject = ATTRIBUTE_PLANTCOVER_CONFIG.getJsonObject(parentAttributeName);
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
                PlantCover plantCover = (PlantCover) model;
                AttributeItem attributeItem;
                switch (addAttributeName) {
                        case "core:creationDate":
                                new CreationDateWrapper(modelType).add(plantCover, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<PlantCover>(plantCover,
                                                                getAttributeName("creationDate")));
                                break;
                        case "core:terminationDate":
                                new TerminationDateWrapper(modelType).add(plantCover, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<PlantCover>(plantCover,
                                                                getAttributeName("terminationDate")));
                                break;
                        case "veg:class":
                                new ClazzWrapper(modelType).add(plantCover, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<PlantCover>(plantCover,
                                                                getAttributeName("class")));
                                break;
                        case "veg:averageHeight":
                                new AverageHeightWrapper(modelType).add(plantCover, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<PlantCover>(plantCover,
                                                                getAttributeName("averageHeight")));
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
                PlantCover selectedModel = (PlantCover) selectedFeature.getGML();

                selectedModel.unsetTerminationDate();
                selectedModel.unsetCreationDate();
                selectedModel.unsetClazz();
                selectedModel.unsetAverageHeight();
                selectedModel.unsetGenericApplicationPropertyOfPlantCover();

                return getADEComponents(selectedFeature);
        }

        public List<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
                PlantCover selectedBuilding = (PlantCover) selectedFeature.getGML();
                return selectedBuilding.getGenericApplicationPropertyOfPlantCover();
        }
}
