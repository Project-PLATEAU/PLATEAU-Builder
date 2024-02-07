package org.plateau.citygmleditor.citymodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttributeItem {
    private StringProperty key;
    private StringProperty value;
    private StringProperty uom;
    private StringProperty codeSpace;
    private boolean isEditable;

    public StringProperty keyProperty() {
        return key;
    }

    public StringProperty valueProperty() {
        return value;
    }

    public StringProperty uomProperty() {
        return uom;
    }

    public StringProperty codeSpaceProperty() {
        return codeSpace;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public AttributeItem(String key, String value, String uom, String codeSpace) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
        this.uom = new SimpleStringProperty(uom);
        this.codeSpace = new SimpleStringProperty(codeSpace);
        this.isEditable = true;
    }

    public AttributeItem(String key, String value, String uom, String codeSpace, boolean isEditable) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
        this.uom = new SimpleStringProperty(uom);
        this.codeSpace = new SimpleStringProperty(codeSpace);
        this.isEditable = isEditable;
    }

}
