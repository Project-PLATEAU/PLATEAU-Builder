package org.plateaubuilder.core.editor.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.CommonAttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManagerFactory;
import org.plateaubuilder.core.citymodel.attribute.manager.BuildingSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NodeAttributeHandler;
import org.plateaubuilder.core.citymodel.attribute.wrapper.RootAttributeHandler;
import org.plateaubuilder.core.editor.Editor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AttributeEditor {
    /**
     * 子要素を追加します
     * 
     * @param baseAttributeItem 追加したい属性の親AttributeItem
     * @param value             属性値入力フォーム上の値
     * @param codeSpace         codeSpace入力フォーム上の値
     * @param uom               uom入力フォーム上の値
     */
    public static AttributeItem addAttribute(AttributeItem baseAttributeItem, String addAttributeName, String value,
            String codeSpace, String uom, ChildList<ADEComponent> addedAttributeTree) {
        // CommonAttributeItemの場合、全関連地物に対して属性を追加
        if (baseAttributeItem instanceof CommonAttributeItem) {
            AttributeItem firstAdded = null;
            Set<IFeatureView> features = baseAttributeItem.getName().equals("root")
                    ? Editor.getFeatureSellection().getSelectedFeatures()
                    : ((CommonAttributeItem) baseAttributeItem).getRelatedFeatures();
            CommonAttributeItem commonAttributeItem = null;
            for (IFeatureView feature : features) {
                AttributeItem actualParent = baseAttributeItem.getName().equals("root")
                        ? new AttributeItem(new RootAttributeHandler(feature))
                        : ((CommonAttributeItem) baseAttributeItem).getAttributeForFeature(feature);

                AttributeItem addedItem = addAttributeForFeature(actualParent, addAttributeName, value,
                        codeSpace, uom, AttributeTreeBuilder.getADEComponents(feature));

                if (firstAdded == null) {
                    firstAdded = addedItem;
                    commonAttributeItem = new CommonAttributeItem(addedItem, feature);
                } else {
                    commonAttributeItem.addRelatedAttribute(feature, addedItem);
                }
            }
            return commonAttributeItem;
        }
        return addAttributeForFeature(baseAttributeItem, addAttributeName, value, codeSpace, uom, addedAttributeTree);
    }

    private static AttributeItem addAttributeForFeature(AttributeItem baseAttributeItem, String addAttributeName,
            String value, String codeSpace, String uom, ChildList<ADEComponent> addedAttributeTree) {
        AttributeItem addedAttributeItem = null;
        AttributeSchemaManager attributeSchemaManager;
        if (addAttributeName.split(":")[0].matches("uro")) {
            ArrayList<ArrayList<String>> childAttributeList = Editor.getUroSchemaDocument().getElementList(
                    baseAttributeItem.getName(), false, null, "uro");
            String addAttributeType;

            addAttributeType = Editor.getUroSchemaDocument().getType(addAttributeName, baseAttributeItem.getName(),
                    "uro");

            if (baseAttributeItem.getName().matches("root")) {
                var adeComponent = addedAttributeTree.get(0);
                Element newElement = createNewElement(
                        ((ADEGenericElement) adeComponent).getContent().getOwnerDocument(),
                        value, uom, codeSpace, addAttributeName);

                ADEGenericElement newAdeElement = new ADEGenericElement(newElement);

                addedAttributeTree.add(addedAttributeTree.size(), (ADEComponent) newAdeElement);
                addedAttributeItem = new AttributeItem(new NodeAttributeHandler((Node) newElement, addAttributeType));

            } else {
                Element newElement = createNewElement(((Node) baseAttributeItem.getContent()).getOwnerDocument(), value,
                        uom, codeSpace, addAttributeName);
                ((Node) baseAttributeItem.getContent()).appendChild(newElement);
                addedAttributeItem = new AttributeItem(new NodeAttributeHandler((Node) newElement, addAttributeType));
                sortElement(baseAttributeItem, childAttributeList);
            }
        } else {
            attributeSchemaManager = AttributeSchemaManagerFactory
                    .getSchemaManager(Editor.getFeatureSellection().getActiveFeatureProperty().get().getGML());
            addedAttributeItem = attributeSchemaManager.addAttribute(
                    (AbstractCityObject) baseAttributeItem.getContent(), addAttributeName,
                    value);
        }
        if (!(uom == null)) {
            addedAttributeItem.setUom(uom);
        }
        if (!(codeSpace == null)) {
            addedAttributeItem.setCodeSpace(codeSpace);
        }
        return addedAttributeItem;
    }

    /**
     * 属性の各種値を編集します
     *
     * @param targetNode 編集対象のAttributeItem
     * @param codeSpace  更新するcodeSpaceの値
     * @param uom        更新するuomの値
     * @param value      更新する属性の値
     */
    public static void editAttribute(AttributeItem targetAttribute, String codeSpace,
            String uom, String value) {
        if (codeSpace != null && !codeSpace.isEmpty()) {
            targetAttribute.setCodeSpace(codeSpace);
        }
        if (uom != null && !uom.isEmpty()) {
            targetAttribute.setUom(uom);
        }
        targetAttribute.setValue(value);
    }

    /**
     * 属性を削除します
     *
     * @param targetNode 編集対象のAttributeItem
     * @param codeSpace  更新するcodeSpaceの値
     * @param uom        更新するuomの値
     * @param value      更新する属性の値
     */
    public static void removeAttribute(String removeAttributeName, AttributeItem parentAttributeItem,
            AttributeItem removeAttributeItem,
            ChildList<ADEComponent> addedAttributeTree) {
        // CommonAttributeItemの場合、関連する全ての属性を削除
        if (removeAttributeItem instanceof CommonAttributeItem) {
            CommonAttributeItem commonAttr = (CommonAttributeItem) removeAttributeItem;
            var baseAttributeItem = parentAttributeItem;
            // 全ての関連地物の属性を削除
            for (Map.Entry<IFeatureView, AttributeItem> entry : commonAttr.getFeatureAttributeEntries()) {
                IFeatureView feature = entry.getKey();
                AttributeItem attributeItem = entry.getValue();

                // 各地物の属性ツリーから削除
                if (removeAttributeName.split(":")[0].matches("uro")) {
                    if (!parentAttributeItem.getName().matches("root")
                            && parentAttributeItem instanceof CommonAttributeItem) {
                        baseAttributeItem = ((CommonAttributeItem) parentAttributeItem)
                                .getAttributeForFeature(feature);
                    }
                    removeUroAttribute(removeAttributeName, baseAttributeItem, attributeItem,
                            AttributeTreeBuilder.getADEComponents(feature));
                } else {
                    attributeItem.remove();
                }
            }
        } else {
            // 単一属性の場合は既存の処理を使用
            if (removeAttributeName.split(":")[0].matches("uro")) {
                removeUroAttribute(removeAttributeName, parentAttributeItem, removeAttributeItem,
                        addedAttributeTree);
            } else {
                removeAttributeItem.remove();
            }
        }
    }

    // URO属性の削除処理を分離
    private static void removeUroAttribute(String removeAttributeName, AttributeItem parentAttributeItem,
            AttributeItem removeAttributeItem, ChildList<ADEComponent> addedAttributeTree) {
        if (parentAttributeItem.getName() == "root") {
            ADEComponent targetAttributeComponent = null;
            Node removeAttributeNode = (Node) removeAttributeItem.getContent();

            for (var adeComponent : addedAttributeTree) {
                Node targetNode = ((ADEGenericElement) adeComponent).getContent();
                if (targetNode == removeAttributeNode)
                    targetAttributeComponent = adeComponent;
            }
            if (targetAttributeComponent != null)
                addedAttributeTree.remove(targetAttributeComponent);
        } else {
            Node parentNode = (Node) parentAttributeItem.getContent();
            Node targetNode = (Node) removeAttributeItem.getContent();
            if (hasTypeElement(parentNode.getNodeName(), parentNode.getFirstChild().getNodeName())) {
                parentNode.getFirstChild().removeChild(targetNode);
            } else {
                parentNode.removeChild(targetNode);
            }
        }
    }

    private static boolean hasTypeElement(String childAttributeName, String baseAttributeName) {
        if ((baseAttributeName.toLowerCase()).matches(childAttributeName.toLowerCase()))
            return true;
        else
            return false;
    }

    public static Element createNewElement(org.w3c.dom.Document document, String value, String uom, String codeSpace,
            String attributeName) {
        String namespaceURI = Editor.getUroSchemaDocument().getXSDDocument().getDocumentElement()
                .getAttribute("xmlns:uro");
        Element newElement = document.createElementNS(namespaceURI, attributeName);
        if (value != null && !value.isEmpty()) {
            newElement.setTextContent(value);
        }
        if (codeSpace != null && !codeSpace.isEmpty()) {
            newElement.setAttribute("codeSpace", Editor.getDatasetPath() + "\\codelists" + "\\" + codeSpace);
        }
        if (uom != null && !uom.isEmpty()) {
            newElement.setAttribute("uom", uom);
        }
        return newElement;
    }

    /**
     * sortElement
     * 要素をソートして地物情報に反映します
     */
    private static void sortElement(AttributeItem baseAttributeItem, ArrayList<ArrayList<String>> childAttributeList) {
        NodeList targetNodeList = null;
        ArrayList<String> sortOrder = new ArrayList<>();
        Element parentElement = null;

        // ソートの基準となるデータを作成
        for (ArrayList<String> attribute : childAttributeList) {
            if (!attribute.isEmpty()) {
                // 各リストの最初の要素をsortOrderに追加
                sortOrder.add(attribute.get(0));
            }
        }
        // ソート対象のNodeListの取得
        parentElement = ((Element) ((Node) baseAttributeItem.getContent()));
        targetNodeList = parentElement.getChildNodes();
        Node childNode = targetNodeList.item(0);
        Element childElement = (Element) childNode;

        ArrayList<Node> sortedNodes = new ArrayList<>();
        if (targetNodeList != null) {
            for (int i = 0; i < targetNodeList.getLength(); i++) {
                Node node = targetNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    sortedNodes.add(node);
                }
            }
            // ソート
            Collections.sort(sortedNodes, new Comparator<Node>() {
                @Override
                public int compare(Node node1, Node node2) {
                    int index1 = sortOrder.indexOf(node1.getNodeName());
                    int index2 = sortOrder.indexOf(node2.getNodeName());
                    // sortOrderに含まれていない要素はリストの最後に配置
                    index1 = index1 == -1 ? Integer.MAX_VALUE : index1;
                    index2 = index2 == -1 ? Integer.MAX_VALUE : index2;
                    return Integer.compare(index1, index2);
                }
            });
            // 旧子ノード群を削除し、ソート後のノード群を設定
            clearChildNode((Node) baseAttributeItem.getContent());
            setChildNode((Node) baseAttributeItem.getContent(), sortedNodes);
        }
    }

    // uomの値を属性から取得
    public static String getUom(AttributeItem targetAttribute) {
        String uomValue = "";
        uomValue = targetAttribute.getUom();
        return uomValue;
    }

    // codeSpaceの値を属性から取得
    public static String getCodeSpace(AttributeItem targetAttribute) {
        String codeSpace = "";
        codeSpace = targetAttribute.getCodeSpace();
        return codeSpace;
    }

    // 属性値を属性から取得
    public static String getValue(AttributeItem targetAttribute) {
        String value = targetAttribute.getValue();
        return value;
    }

    /**
     * 子ノードをクリアします
     */
    private static void clearChildNode(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    /**
     * 新しいNodeListをNodeに格納します
     *
     * @param node       親ノード
     * @param childNodes 追加したいノード
     */
    private static void setChildNode(Node node, ArrayList<Node> childNodes) {
        for (int i = 0; i < childNodes.size(); i++) {
            node.appendChild(childNodes.get(i));
        }
    }

}
