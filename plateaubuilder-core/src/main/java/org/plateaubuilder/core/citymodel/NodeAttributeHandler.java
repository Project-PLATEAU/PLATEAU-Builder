package org.plateaubuilder.core.citymodel;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class NodeAttributeHandler extends AttributeHandler {
    private Node node;
    private String type;

    public NodeAttributeHandler(Node node, String type) {
        this.node = node;
        this.type = type;
    }

    @Override
    public String getName() {
        return node.getNodeName();
    }

    @Override
    public String getValue() {
        if (node.getFirstChild() == null) {
            return null;
        } else {
            return node.getFirstChild().getNodeValue();
        }
    }

    @Override
    public void setValue(String value) {
        node.setTextContent(value);
    }

    @Override
    public String getUom() {
        if (node instanceof Element) {
            return ((Element) node).getAttribute("uom");
        }
        return "";
    }

    @Override
    public void setUom(String uom) {
        if (node instanceof Element) {
            ((Element) node).setAttribute("uom", uom);
        }
    }

    @Override
    public String getCodeSpace() {
        String codeSpace = "";
        if (node instanceof Element) {
            Element targetElement = (Element) node;
            codeSpace = targetElement.getAttribute("codeSpace");
        }
        return codeSpace;
    }

    @Override
    public void setCodeSpace(String codeSpace) {
        Element targetElement = (Element) node;
        targetElement.setAttribute("codeSpace", "../../codelists/" + codeSpace);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Node getContent() {
        return node;
    }
}