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
import org.citygml4j.model.citygml.core.AddressProperty;
import org.citygml4j.model.xal.Country;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AddressWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.ClazzWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CountryNameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.CreationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.DescriptionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.LocalityNameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.MeasuredHeightWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NameWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.RoofTypeWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.StoreysAboveGroundWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.StoreysBelowGroundWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.TerminationDateWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.UsageWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.YearOfConstructionWrapper;
import org.plateaubuilder.core.citymodel.attribute.wrapper.YearOfDemolitionWrapper;

import javafx.scene.control.TreeItem;

/**
 * Bldg（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class BuildingSchemaManager implements AttributeSchemaManager {
        private static final JsonObject ATTRIBUTE_BUILDING_CONFIG;
        private static final ModelType modelType = ModelType.BUILDING;
        static {
                ATTRIBUTE_BUILDING_CONFIG = loadSchema("/bldgAttributeSchema.json");
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {
                AbstractBuilding selectedBuilding = (AbstractBuilding) selectedFeature.getGML();

                if (selectedBuilding.isSetDescription()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("description")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetName()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("name")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetCreationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("creationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetTerminationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("terminationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetClazz()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("class")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetUsage()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("usage")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetYearOfConstruction()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("yearOfConstruction")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetYearOfDemolition()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("yearOfDemolition")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetRoofType()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("roofType")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetMeasuredHeight()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("measuredHeight")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetStoreysAboveGround()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("storeysAboveGround")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetStoreysBelowGround()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("storeysBelowGround")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetAddress()) {
                        var addressAttributeItem = new AttributeItem(
                                        new AttributeHandler<AbstractBuilding>(selectedBuilding,
                                                        getAttributeName("address")));
                        TreeItem addressTreeItem = new TreeItem<>(addressAttributeItem);
                        AddressProperty addressProperty = selectedBuilding.getAddress().get(0);

                        if (addressProperty.getAddress() != null && addressProperty.getAddress().isSetXalAddress()
                                        && addressProperty.getAddress().getXalAddress() != null) {
                                Country country = addressProperty.getAddress().getXalAddress()
                                                .getAddressDetails().getCountry();
                                if (country.isSetCountryName()) {
                                        var countryAttributeItem = new TreeItem<>(
                                                        new AttributeItem(new AttributeHandler<AbstractBuilding>(
                                                                        selectedBuilding,
                                                                        getChildAttributeName("address",
                                                                                        "CountryName"))));
                                        addressTreeItem.getChildren().add(countryAttributeItem);
                                }
                                if (country.isSetLocality()) {
                                        var localityAttributeItem = new TreeItem<>(
                                                        new AttributeItem(new AttributeHandler<AbstractBuilding>(
                                                                        selectedBuilding,
                                                                        getChildAttributeName("address",
                                                                                        "LocalityName"))));
                                        addressTreeItem.getChildren().add(localityAttributeItem);
                                }

                        }
                        root.getChildren().add(addressTreeItem);
                }
        }

        public String getAttributeName(String attributeKey) {
                return ATTRIBUTE_BUILDING_CONFIG.getJsonObject(attributeKey).getString("name");
        }

        public String getAttributeType(String attributeKey) {
                return ATTRIBUTE_BUILDING_CONFIG.getJsonObject(attributeKey).getString("type");
        }

        public String getAttributeMin(String attributeKey) {
                return ATTRIBUTE_BUILDING_CONFIG.getJsonObject(attributeKey).getString("min");
        }

        public String getAttributeMax(String attributeKey) {
                return ATTRIBUTE_BUILDING_CONFIG.getJsonObject(attributeKey).getString("max");
        }

        public String getAttributeUom(String attributeKey) {
                JsonObject attributeObject = ATTRIBUTE_BUILDING_CONFIG.getJsonObject(attributeKey);

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
                JsonObject attributeObject = ATTRIBUTE_BUILDING_CONFIG.getJsonObject(parentAttributeKey);
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
                for (String key : ATTRIBUTE_BUILDING_CONFIG.keySet()) {
                        JsonObject parentObject = ATTRIBUTE_BUILDING_CONFIG.getJsonObject(key);
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

                JsonObject parentObject = ATTRIBUTE_BUILDING_CONFIG.getJsonObject(parentAttributeName);

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
                AbstractBuilding building = (AbstractBuilding) model;
                AttributeItem attributeItem;
                switch (addAttributeName) {
                        case "gml:description":
                                new DescriptionWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("description")));
                                break;
                        case "gml:name":
                                new NameWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("name")));
                                break;
                        case "core:creationDate":
                                new CreationDateWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("creationDate")));
                                break;
                        case "core:terminationDate":
                                new TerminationDateWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("terminationDate")));
                                break;
                        case "bldg:class":
                                new ClazzWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("class")));
                                break;
                        case "bldg:usage":
                                new UsageWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("usage")));
                                break;
                        case "bldg:yearOfConstruction":
                                new YearOfConstructionWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("yearOfConstruction")));
                                break;
                        case "bldg:yearOfDemolition":
                                new YearOfDemolitionWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("yearOfDemolition")));
                                break;
                        case "bldg:roofType":
                                new RoofTypeWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("roofType")));
                                break;
                        case "bldg:measuredHeight":
                                new MeasuredHeightWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("measuredHeight")));
                                break;
                        case "bldg:storeysBelowGround":
                                new StoreysBelowGroundWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("storeysBelowGround")));
                                break;
                        case "bldg:storeysAboveGround":
                                new StoreysAboveGroundWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("storeysAboveGround")));
                                break;
                        case "bldg:address":
                                new AddressWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("address")));
                                break;
                        case "xAL:CountryName":
                                new CountryNameWrapper(modelType).add(building, value);
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("CountryName")));
                                break;
                        case "xAL:LocalityName":
                                attributeItem = new AttributeItem(
                                                new AttributeHandler<AbstractBuilding>(building,
                                                                getAttributeName("LocalityName")));
                                break;
                        default:
                                return null;
                }
                return attributeItem;
        }

        // スキーマをロードするメソッド
        private static JsonObject loadSchema(String path) {
                try (InputStream fis = BuildingSchemaManager.class.getResourceAsStream(path);
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
                AbstractBuilding selectedBuilding = (AbstractBuilding) selectedFeature.getGML();

                selectedBuilding.unsetDescription();
                selectedBuilding.unsetName();
                selectedBuilding.unsetTerminationDate();
                selectedBuilding.unsetCreationDate();
                selectedBuilding.unsetClazz();
                selectedBuilding.unsetUsage();
                selectedBuilding.unsetYearOfConstruction();
                selectedBuilding.unsetYearOfDemolition();
                selectedBuilding.unsetRoofType();
                selectedBuilding.unsetMeasuredHeight();
                selectedBuilding.unsetStoreysAboveGround();
                selectedBuilding.unsetStoreysBelowGround();
                // selectedBuilding.unsetAddress();

                selectedBuilding.unsetGenericApplicationPropertyOfAbstractBuilding();
                return getADEComponents(selectedFeature);
        }

        public List<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
                AbstractBuilding selectedBuilding = (AbstractBuilding) selectedFeature.getGML();
                return selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding();
        }
}
