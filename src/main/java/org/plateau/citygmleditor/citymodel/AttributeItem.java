package org.plateau.citygmleditor.citymodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttributeItem {
    private StringProperty key;
    private StringProperty value;
    private boolean isEditable;

    public StringProperty keyProperty() {
        return key;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public AttributeItem(String key, String value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
        this.isEditable = true;
    }

    public AttributeItem(String key, String value, boolean isEditable) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
        this.isEditable = isEditable;
    }

}
