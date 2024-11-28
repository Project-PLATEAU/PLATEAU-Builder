package org.plateaubuilder.core.citymodel.attribute.wrapper;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * uomなどのElementで操作を行う属性の情報を操作するためのクラス
 */
public class SimpleAttributeHandler extends AttributeHandler {
    private String value;
    private String uom;
    private String codeSpace;

    public SimpleAttributeHandler(String name, String value, String uom, String codeSpace) {
        super(name);
        this.value = value;
        this.uom = uom;
        this.codeSpace = codeSpace;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getUom() {
        return uom;
    }

    @Override
    public String getCodeSpace() {
        return codeSpace;
    }
}