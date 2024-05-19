package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.measures.Length;
import org.w3c.dom.Node;

import javafx.scene.control.TreeItem;

public class MeasuredHeightManager {
    private static String type = "gml:LengthType";
    private static String name = "measuredHeight";

    public static void setMeasuredHeight(AbstractBuilding building, String value, String uom) {
        building.setMeasuredHeight(new Length());
        building.getMeasuredHeight().setValue(Double.parseDouble(value));
        building.getMeasuredHeight().setUom(uom);
    }

    public static AttributeItem addMeasureHeightToTreeView(AbstractBuilding selectedBuilding,
            TreeItem<AttributeItem> root) {
        var attributeItem = new AttributeItem(new MeasuredHeightHandler(selectedBuilding));
        root.getChildren().add(new TreeItem<>(attributeItem));
        return attributeItem;
    }

    public static void setMeasuredHeightValue(AbstractBuilding building, String value) {
        building.getMeasuredHeight().setValue(Double.parseDouble(value));
    }

    public static void setMeasuredHeightUom(AbstractBuilding building, String uom) {
        building.getMeasuredHeight().setUom(uom);
    }

    public static String getMeasuredHeightValue(AbstractBuilding building) {
        return String.valueOf(building.getMeasuredHeight().getValue());
    }

    public static String getMeasuredHeightUom(AbstractBuilding building) {
        return building.getMeasuredHeight().getUom();
    }

    public static void removeMeasuredHeight(AbstractBuilding building) {
        building.unsetMeasuredHeight();
    }

    public static String getName() {
        return name;
    }

    public static String getType() {
        return type;
    }
}
