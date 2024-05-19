package org.plateaubuilder.core.citymodel.attribute;

public abstract class AttributeHandler {
    public abstract String getName();

    public abstract String getValue();

    public abstract void setValue(String value);

    public abstract String getUom();

    public abstract void setUom(String uom);

    public abstract String getCodeSpace();

    public abstract void setCodeSpace(String codeSpace);

    public abstract String getType();

    public abstract Object getContent();
}