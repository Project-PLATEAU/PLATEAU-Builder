package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.citygml4j.model.citygml.generics.AbstractGenericAttribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * GenericAttribute属性の情報を操作するためのクラス
 */
public class GenericAttributeHandler extends AttributeHandler<AbstractGenericAttribute> {
    private AbstractGenericAttribute attribute;
    private String value;
    private String uom;
    private String codeSpace;

    public GenericAttributeHandler(AbstractGenericAttribute attribute, String name, String value, String uom,
            String codeSpace) {
        super(attribute, name);
        this.attribute = attribute;
        this.value = value;
        this.codeSpace = codeSpace;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String value) {

    }

    @Override
    public String getUom() {
        return uom;
    }

    @Override
    public void setUom(String uom) {

    }

    @Override
    public String getCodeSpace() {
        return codeSpace;
    }

    @Override
    public void setCodeSpace(String codeSpace) {

    }

    @Override
    public AbstractGenericAttribute getContent() {
        return attribute;
    }
}