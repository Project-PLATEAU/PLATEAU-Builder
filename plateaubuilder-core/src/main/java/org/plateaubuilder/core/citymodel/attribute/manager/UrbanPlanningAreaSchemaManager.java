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
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;

import javafx.scene.control.TreeItem;

/**
 * UrbanPlanningArea（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class UrbanPlanningAreaSchemaManager implements AttributeSchemaManager {
        private static final JsonObject ATTRIBUTE_URBANPLANNINGAREA_CONFIG; // Building用のスキーマ
        private static final ModelType modelType = ModelType.URBAN_PLANNING_AREA;
        static {
                ATTRIBUTE_URBANPLANNINGAREA_CONFIG = loadSchema("/UrbanPlanningAreaAttributeSchema.json");
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {
                ADEGenericComponent selectedLandUse = (ADEGenericComponent) selectedFeature.getGML();
        }

        public String getAttributeName(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(attributeKey);
                if (attributeObject == null) {
                        return null;
                }
                return attributeObject.getString("name");
        }

        public String getAttributeType(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(attributeKey);
                if (attributeObject == null) {
                        return null;
                }
                return attributeObject.getString("type");
        }

        public String getAttributeMin(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(attributeKey);
                if (attributeObject == null) {
                        return null;
                }
                return attributeObject.getString("min");
        }

        public String getAttributeMax(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(attributeKey);
                if (attributeObject == null) {
                        return null;
                }
                return attributeObject.getString("max");
        }

        public String getAttributeUom(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(attributeKey);

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
                JsonObject attributeObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(parentAttributeKey);
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
                ArrayList<String> childNames = new ArrayList<>();
                JsonObject parentObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(parentAttributeName);
                JsonObject childrenObject = parentObject.getJsonObject("children");
                if (parentObject == null || childrenObject == null) {
                        return childNames;
                }
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
         * 属性の名前リストを返します（子属性は考慮しない）
         *
         * @return parentNames 属性の名前リスト
         */
        public ArrayList<String> getParentNames() {
                ArrayList<String> attributeNames = new ArrayList<>();
                for (String key : ATTRIBUTE_URBANPLANNINGAREA_CONFIG.keySet()) {
                        JsonObject parentObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(key);
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
                JsonObject parentObject = ATTRIBUTE_URBANPLANNINGAREA_CONFIG.getJsonObject(parentAttributeName);
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
                // switch (addAttributeName) {
                // case "gml:description":
                // new DescriptionWrapper(dataType).add(building, value);
                // break;
                // case "gml:name":
                // new NameWrapper(dataType).add(building, value);
                // break;
                // case "core:creationDate":
                // new CreationDateWrapper(dataType).add(building, value);
                // break;
                // case "core:terminationDate":
                // new TerminationDateWrapper(dataType).add(building, value);
                // break;
                // case "bldg:class":
                // new ClazzWrapper(dataType).add(building, value);
                // break;
                // case "bldg:usage":
                // new UsageWrapper(dataType).add(building, value);
                // break;
                // case "bldg:yearOfConstruction":
                // new YearOfConstructionWrapper(dataType).add(building, value);
                // break;
                // case "bldg:roofType":
                // new RoofTypeWrapper(dataType).add(building, value);
                // break;
                // case "bldg:measuredHeight":
                // new MeasuredHeightWrapper(dataType).add(building, value);
                // break;
                // case "bldg:storeysBelowGround":
                // new StoreysBelowGroundWrapper(dataType).add(building, value);
                // break;
                // case "bldg:storeysAboveGround":
                // new StoreysAboveGroundWrapper(dataType).add(building, value);
                // break;
                // case "bldg:address":
                // new AddressWrapper(dataType).add(building, value);
                // break;
                // case "xAL:CountryName":
                // new CountryNameWrapper(dataType).add(building, value);
                // break;
                // case "xAL:LocalityName":
                // new LocalityNameWrapper(dataType).add(building, value);
                // break;
                // default:
                // return;
                // }
                return null;
        }

        // スキーマをロードするメソッド
        private static JsonObject loadSchema(String path) {
                try (InputStream fis = UrbanPlanningAreaSchemaManager.class.getResourceAsStream(path);
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
                return getADEComponents(selectedFeature);
        }

        public List<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
                AbstractBuilding selectedBuilding = (AbstractBuilding) selectedFeature.getGML();
                return selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding();
        }
}
