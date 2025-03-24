package org.plateaubuilder.core.citymodel.attribute;

public class AttributeErrorInfo {
    private String attributeName;
    private boolean isEmpty;
    private boolean hasInvalidValue;
    private String invalidType;

    public AttributeErrorInfo(String attributeName) {
        this.attributeName = attributeName;
        this.isEmpty = false;
        this.hasInvalidValue = false;
        this.invalidType = null;
    }

    public AttributeErrorInfo(String attributeName, boolean isEmpty, boolean hasInvalidValue, String invalidType) {
        this.attributeName = attributeName;
        this.isEmpty = isEmpty;
        this.hasInvalidValue = hasInvalidValue;
        this.invalidType = invalidType;
    }

    // Getters
    public String getAttributeName() {
        return attributeName;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean hasInvalidValue() {
        return hasInvalidValue;
    }

    public String getInvalidType() {
        return invalidType;
    }

    // Setters
    public void setIsEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }

    public void setHasInvalidValue(boolean hasInvalidValue) {
        this.hasInvalidValue = hasInvalidValue;
    }

    public void setInvalidType(String invalidType) {
        this.invalidType = invalidType;
    }
}