package org.plateaubuilder.core.citymodel.attribute.manager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.citygml4j.model.citygml.building.AbstractBuilding;
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

import javafx.scene.control.TreeItem;

/**
 * Bldg（地物）に対する属性の追加や削除、表示などを行うためのクラス
 */
public class BuildingSchemaManager {
        private static JsonObject attributeConfig;

        static {
                try (InputStream fis = BuildingSchemaManager.class
                                .getResourceAsStream("/bldgAttributeSchema.json");
                                JsonReader reader = Json.createReader(new InputStreamReader(fis))) {
                        attributeConfig = reader.readObject();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        /**
         * 対象地物が持つ属性をTreeItemに追加します
         *
         * @param selectedFeature 地物
         * @param root            TreeItemのルート
         */
        public static void addAttributeToTreeView(IFeatureView selectedFeature,
                        TreeItem<AttributeItem> root) {

                AbstractBuilding selectedBuilding = (AbstractBuilding) selectedFeature.getGML();

                if (selectedBuilding.isSetDescription()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("description")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetName()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding, getAttributeName("name")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetCreationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("creationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetTerminationDate()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("terminationDate")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetClazz()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding, getAttributeName("class")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetUsage()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding, getAttributeName("usage")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetYearOfConstruction()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("yearOfConstruction")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetYearOfDemolition()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("yearOfDemolition")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetRoofType()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding, getAttributeName("roofType")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetMeasuredHeight()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("measuredHeight")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetStoreysAboveGround()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("storeysAboveGround")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetStoreysBelowGround()) {
                        var attributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("storeysBelowGround")));
                        root.getChildren().add(new TreeItem<>(attributeItem));
                }
                if (selectedBuilding.isSetAddress()) {
                        var addressAttributeItem = new AttributeItem(
                                        new AttributeHandler(selectedBuilding,
                                                        getAttributeName("address")));
                        TreeItem addressTreeItem = new TreeItem<>(addressAttributeItem);
                        AddressProperty addressProperty = selectedBuilding.getAddress().get(0);

                        if (addressProperty.getAddress() != null && addressProperty.getAddress().isSetXalAddress()
                                        && addressProperty.getAddress().getXalAddress() != null) {
                                Country country = addressProperty.getAddress().getXalAddress()
                                                .getAddressDetails().getCountry();
                                if (country.isSetCountryName()) {
                                        var countryAttributeItem = new TreeItem<>(
                                                        new AttributeItem(new AttributeHandler(
                                                                        selectedBuilding,
                                                                        getChildAttributeName("address",
                                                                                        "CountryName"))));
                                        addressTreeItem.getChildren().add(countryAttributeItem);
                                }
                                if (country.isSetLocality()) {
                                        var localityAttributeItem = new TreeItem<>(
                                                        new AttributeItem(new AttributeHandler(
                                                                        selectedBuilding,
                                                                        getChildAttributeName("address",
                                                                                        "LocalityName"))));
                                        addressTreeItem.getChildren().add(localityAttributeItem);
                                }

                        }
                        root.getChildren().add(addressTreeItem);
                }
        }

        public static String getAttributeName(String attributeKey) {
                return attributeConfig.getJsonObject(attributeKey).getString("name");
        }

        public static String getAttributeType(String attributeKey) {
                return attributeConfig.getJsonObject(attributeKey).getString("type");
        }

        public static String getAttributeMin(String attributeKey) {
                return attributeConfig.getJsonObject(attributeKey).getString("min");
        }

        public static String getAttributeMax(String attributeKey) {
                return attributeConfig.getJsonObject(attributeKey).getString("max");
        }

        public static String getChildAttributeName(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("name") : null;
        }

        public static String getChildAttributeType(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("type") : null;
        }

        public static String getChildAttributeMin(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("min") : null;
        }

        public static String getChildAttributeMax(String parentAttributeKey, String childAttributeName) {
                JsonObject childObject = getChildAttribute(parentAttributeKey, childAttributeName);
                return childObject != null ? childObject.getString("max") : null;
        }

        private static JsonObject getChildAttribute(String parentAttributeKey, String childKey) {
                JsonObject attributeObject = attributeConfig.getJsonObject(parentAttributeKey);
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
        public static ArrayList<String> getBldgAttributeName(String parentAttributeName,
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
        public static ArrayList<String> getParentNames() {
                ArrayList<String> attributeNames = new ArrayList<>();
                for (String key : attributeConfig.keySet()) {
                        JsonObject parentObject = attributeConfig.getJsonObject(key);
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
        public static ArrayList<String> getChildNames(String parentAttributeName,
                        ArrayList<String> addedAttributeNames) {
                ArrayList<String> childNames = new ArrayList<>();
                JsonObject parentObject = attributeConfig.getJsonObject(parentAttributeName);
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
         * @param building         地物（AbstractBuilding）
         * @param addAttributeName 属性名
         * @param value            値
         */
        public static void addAttribute(AbstractBuilding building, String addAttributeName, String value) {
                switch (addAttributeName) {
                        case "gml:description":
                                new DescriptionWrapper().add(building, value);
                                break;
                        case "gml:name":
                                new NameWrapper().add(building, value);
                                break;
                        case "core:creationDate":
                                new CreationDateWrapper().add(building, value);
                                break;
                        case "core:terminationDate":
                                new TerminationDateWrapper().add(building, value);
                                break;
                        case "bldg:class":
                                new ClazzWrapper().add(building, value);
                                break;
                        case "bldg:usage":
                                new UsageWrapper().add(building, value);
                                break;
                        case "bldg:yearOfConstruction":
                                new YearOfConstructionWrapper().add(building, value);
                                break;
                        case "bldg:roofType":
                                new RoofTypeWrapper().add(building, value);
                                break;
                        case "bldg:measuredHeight":
                                new MeasuredHeightWrapper().add(building, value);
                                break;
                        case "bldg:storeysBelowGround":
                                new StoreysBelowGroundWrapper().add(building, value);
                                break;
                        case "bldg:storeysAboveGround":
                                new StoreysAboveGroundWrapper().add(building, value);
                                break;
                        case "bldg:address":
                                new AddressWrapper().add(building, value);
                                break;
                        case "xAL:CountryName":
                                new CountryNameWrapper().add(building, value);
                                break;
                        case "xAL:LocalityName":
                                new LocalityNameWrapper().add(building, value);
                                break;
                        default:
                                return;
                }
        }
}
