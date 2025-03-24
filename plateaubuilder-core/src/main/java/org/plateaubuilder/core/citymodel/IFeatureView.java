package org.plateaubuilder.core.citymodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.w3c.dom.Element;

import javafx.scene.Node;

public interface IFeatureView {
    AbstractCityObject getGML();

    String getFeatureType();

    ILODView getLODView(int lod);

    void setLODView(int lod, ILODView lodView);

    Node getNode();

    void setVisible(boolean visible);

    boolean isFiltered();

    void setFiltered(boolean filtered);

    List<ADEComponent> getADEComponents();

    default String getId() {
        return getNode().getId();
    }

    default Node getParent() {
        return getNode().getParent();
    }

    default CityModelView getCityModelView() {
        var parent = getNode().getParent();
        if (parent instanceof CityModelView) {
            return (CityModelView) parent;
        } else if (parent instanceof IFeatureView) {
            return ((IFeatureView) parent).getCityModelView();
        }

        return null;
    }

    default List<String> getSupportedLODTypes() {
        return Arrays.asList("LOD1", "LOD2", "LOD3");
    }

    // TODO: 暫定で追加した(ほかのGMLのプロパティによっては共通化する)
    default boolean isSetMeasuredHeight() {
        return false;
    }

    default Length getMeasuredHeight() {
        return null;
    }

    default void setMeasuredHeight(Length length) {
    }

    default void unsetMeasuredHeight() {
    }

    default List<String> getTexturePaths() {
        return new ArrayList<String>();
    }

    default Object getAttribute(String key) {
        var attributePaths = key.split("_");
        if (attributePaths[0].startsWith("uro:")) {
            return getUroAttribute(attributePaths);
        } else if (attributePaths[0].startsWith("urf:")) {
            return getUrfAttribute(attributePaths);
        } else {
            return getAttribute(attributePaths);
        }
    }

    default Object getUroAttribute(String[] attributePaths) {
        var adeComponents = getADEComponents();
        for (var adeComponent : adeComponents) {
            var adeElement = (ADEGenericElement) adeComponent;
            var content = adeElement.getContent();
            var nodeName = content.getNodeName();
            if (nodeName.equals(attributePaths[0])) {
                return attributePaths.length == 1 ? content.getNodeValue() : getUroAttribute(content, attributePaths, 1);
            }
        }

        return null;
    }

    default Object getUroAttribute(Element parent, String[] attributePaths, int index) {
        var childNodes = parent.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[index])) {
                    return attributePaths.length == index + 1 ? element.getTextContent() : getUroAttribute(element, attributePaths, index + 1);
                }
            }
        }

        return null;
    }

    default Object getUrfAttribute(String[] attributePaths) {
        var rootElement = (ADEGenericElement) getADEComponents().get(0);
        var childNodes = rootElement.getContent().getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[0])) {
                    return attributePaths.length == 1 ? element.getTextContent() : getUrfAttribute(element, attributePaths, 1);
                }
            }
        }

        return null;
    }

    default Object getUrfAttribute(Element parent, String[] attributePaths, int index) {
        var childNodes = parent.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[index])) {
                    return attributePaths.length == index + 1 ? element.getTextContent() : getUrfAttribute(element, attributePaths, index + 1);
                }
            }
        }

        return null;
    }

    default Object getAttribute(String[] attributePaths) {
        return null;
    }

    default void setAttribute(String key, Object value) {
        var attributePaths = key.split("_");
        if (attributePaths[0].startsWith("uro:")) {
            setUroAttribute(attributePaths, value);
        } else if (attributePaths[0].startsWith("urf:")) {
            setUrfAttribute(attributePaths, value);
        } else {
            setAttribute(attributePaths, value);
        }
    }

    default void setUroAttribute(String[] attributePaths, Object value) {
        var adeComponents = getADEComponents();
        for (var adeComponent : adeComponents) {
            var adeElement = (ADEGenericElement) adeComponent;
            var content = adeElement.getContent();
            var nodeName = content.getNodeName();
            if (nodeName.equals(attributePaths[0])) {
                if (attributePaths.length == 1) {
                    content.setNodeValue(value.toString());
                } else {
                    setUroAttribute(content, attributePaths, value, 1);
                }
            }
        }
    }

    default void setUroAttribute(Element parent, String[] attributePaths, Object value, int index) {
        var childNodes = parent.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[index])) {
                    if (attributePaths.length == index + 1) {
                        element.setTextContent(value.toString());
                    } else {
                        setUroAttribute(element, attributePaths, value, index + 1);
                    }
                }
            }
        }
    }

    default void setUrfAttribute(String[] attributePaths, Object value) {
        var rootElement = (ADEGenericElement) getADEComponents().get(0);
        var childNodes = rootElement.getContent().getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[0])) {
                    if (attributePaths.length == 1) {
                        element.setTextContent(value.toString());
                    } else {
                        setUrfAttribute(element, attributePaths, value, 1);
                    }
                }
            }
        }
    }

    default void setUrfAttribute(Element parent, String[] attributePaths, Object value, int index) {
        var childNodes = parent.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[index])) {
                    if (attributePaths.length == index + 1) {
                        element.setTextContent(value.toString());
                    } else {
                        setUrfAttribute(element, attributePaths, value, index + 1);
                    }
                }
            }
        }

    }

    default void setAttribute(String[] attributePaths, Object value) {
    }

    default boolean isSetAttribute(String key) {
        var attributePaths = key.split("_");
        if (attributePaths[0].startsWith("uro:")) {
            return isSetUroAttribute(attributePaths);
        } else if (attributePaths[0].startsWith("urf:")) {
            return isSetUrfAttribute(attributePaths);
        } else {
            return isSetAttribute(attributePaths);
        }
    }

    default boolean isSetUroAttribute(String[] attributePaths) {
        var adeComponents = getADEComponents();
        for (var adeComponent : adeComponents) {
            var adeElement = (ADEGenericElement) adeComponent;
            var content = adeElement.getContent();
            var nodeName = content.getNodeName();
            if (nodeName.equals(attributePaths[0])) {
                return attributePaths.length == 1 ? content.getNodeValue() != null : isSetUroAttribute(content, attributePaths, 1);
            }
        }

        return false;
    }

    default boolean isSetUroAttribute(Element parent, String[] attributePaths, int index) {
        var childNodes = parent.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[index])) {
                    return attributePaths.length == index + 1 ? element.getTextContent() != null : isSetUroAttribute(element, attributePaths, index + 1);
                }
            }
        }

        return false;
    }

    default boolean isSetUrfAttribute(String[] attributePaths) {
        var rootElement = (ADEGenericElement) getADEComponents().get(0);
        var childNodes = rootElement.getContent().getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[0])) {
                    return attributePaths.length == 1 ? element.getTextContent() != null : isSetUrfAttribute(element, attributePaths, 1);
                }
            }
        }

        return false;
    }

    default boolean isSetUrfAttribute(Element parent, String[] attributePaths, int index) {
        var childNodes = parent.getChildNodes();
        for (var i = 0; i < childNodes.getLength(); i++) {
            var child = childNodes.item(i);
            if (child.getNodeType() == Element.ELEMENT_NODE) {
                var element = (Element) child;
                if (element.getNodeName().equals(attributePaths[index])) {
                    return attributePaths.length == index + 1 ? element.getTextContent() != null : isSetUrfAttribute(element, attributePaths, index + 1);
                }
            }
        }

        return false;
    }

    default boolean isSetAttribute(String[] attributePaths) {
        return false;
    }
}
