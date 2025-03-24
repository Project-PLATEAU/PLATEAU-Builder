package org.plateaubuilder.core.citymodel.attribute;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttributeValue {
    private StringProperty name;
    private StringProperty description;

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public AttributeValue(String name, String description) {
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
    }
}
