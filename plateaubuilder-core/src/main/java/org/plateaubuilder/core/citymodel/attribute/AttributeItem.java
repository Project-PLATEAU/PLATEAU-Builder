package org.plateaubuilder.core.citymodel.attribute;

import org.citygml4j.util.internal.xml.SystemIDResolver;
import org.plateaubuilder.core.citymodel.attribute.wrapper.AttributeHandler;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Editor上で地物が持つ属性情報を操作するためのクラス
 */
public class AttributeItem {

    private AttributeHandler attributeHandler;
    private Boolean isEditable;

    /**
     * * @param attributeHandler 属性
     */
    public AttributeItem(AttributeHandler attributeHandler) {
        this.attributeHandler = attributeHandler;
        this.isEditable = true;
    }

    public String getName() {
        if (attributeHandler != null) {
            return attributeHandler.getName();
        } else {
            return "null";
        }
    }

    public String getValue() {
        return attributeHandler.getValue();
    }

    public void setValue(String value) {
        attributeHandler.setValue(value);
    }

    public String getUom() {
        return attributeHandler.getUom();
    }

    public void setUom(String uom) {
        attributeHandler.setUom(uom);
    }

    public String getCodeSpace() {
        return attributeHandler.getCodeSpace();
    }

    public void setCodeSpace(String codeSpace) {
        attributeHandler.setCodeSpace(codeSpace);
    }

    public Object getContent() {
        return attributeHandler.getContent();
    }

    public String getType() {
        return attributeHandler.getType();
    }

    public void remove() {
        attributeHandler.remove();
    }

    // public void add() {
    // attributeHandler.add();
    // }
    protected AttributeHandler getAttributeHandler() {
        return attributeHandler;
    }
}
