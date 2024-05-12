package org.plateaubuilder.core.citymodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AttributeItem {
    private boolean isEditable;
    private AttributeHandler handler;

    /**
     * 属性情報を所持するクラス
     *
     * @param handler 属性本体
     */
    public AttributeItem(AttributeHandler handler) {

        this.handler = handler;
        this.isEditable = true;
    }

    public String getName() {
        if (handler != null) {
            return handler.getName();
        } else {
            return "null";
        }
    }

    public String getValue() {
        return handler.getValue();
    }

    public void setValue(String value) {
        handler.setValue(value);
    }

    public String getUom() {
        return handler.getUom();
    }

    public void setUom(String uom) {
        handler.setUom(uom);
    }

    public String getCodeSpace() {
        return handler.getCodeSpace();
    }

    public void setCodeSpace(String codeSpace) {
        handler.setCodeSpace(codeSpace);
    }

    public Object getContent() {
        return handler.getContent();
    }

    public String getType() {
        return handler.getType();
    }
}
