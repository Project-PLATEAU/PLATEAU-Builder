package org.plateau.citygmleditor.citymodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CodeSpaceValue {
    private StringProperty id;
    private StringProperty description;
    private StringProperty name;

    public StringProperty idProperty() {
        return id;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public CodeSpaceValue(String id, String description, String name) {
        this.id = new SimpleStringProperty(id);
        this.description = new SimpleStringProperty(description);
        this.name = new SimpleStringProperty(name);
    }
}
