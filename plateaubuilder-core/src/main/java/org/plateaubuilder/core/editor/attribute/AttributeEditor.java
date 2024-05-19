package org.plateaubuilder.core.editor.attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.MeasuredHeightHandler;
import org.plateaubuilder.core.citymodel.attribute.MeasuredHeightManager;
import org.plateaubuilder.core.citymodel.attribute.NodeAttributeHandler;
import org.plateaubuilder.core.editor.Editor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
            String codeSpace, String uom,
            ChildList<ADEComponent> bldgAttributeTree) {
        AttributeItem addedAttributeItem = null;
        ArrayList<ArrayList<String>> childAttributeList = Editor.getUroSchemaDocument().getElementList(
                baseAttributeItem.getName(),
                false,
                null, "uro");
        String addAttributeType;

        if (addAttributeName.matches(MeasuredHeightManager.getName())) {
            addAttributeType = MeasuredHeightManager.getType();
        } else {
            addAttributeType = Editor.getUroSchemaDocument().getType(addAttributeName, baseAttributeItem.getName(),
                    "uro");
        }

        // 追加する属性に合わせて、追加処理を実施
        if (addAttributeName.matches((MeasuredHeightManager.getName()))) {
            AbstractBuilding building = (AbstractBuilding) baseAttributeItem.getContent();
            MeasuredHeightManager.setMeasuredHeight(building, value, uom);
            addedAttributeItem = new AttributeItem(new MeasuredHeightHandler(building));
        } else if (baseAttributeItem.getName().matches("root")) {
            var adeComponent = bldgAttributeTree.get(0);
            Element newElement = createNewElement(((ADEGenericElement) adeComponent).getContent().getOwnerDocument(),
                    value, uom, codeSpace, addAttributeName);
            ADEGenericElement newAdeElement = new ADEGenericElement(newElement);
            bldgAttributeTree.add(bldgAttributeTree.size(), (ADEComponent) newAdeElement);

            // 型要素があるかどうかを確認し、あれば追加
            for (int i = 0; i < childAttributeList.size(); i++) {
                if (!childAttributeList.get(i).isEmpty() && childAttributeList.get(i).get(3) != null) {
                    if (hasTypeElement("uro:" + childAttributeList.get(i).get(3), addAttributeName)) {
                        Element newChildElement = createNewElement(
                                ((ADEGenericElement) adeComponent).getContent().getOwnerDocument(), "", "", "",
                                "uro:" + childAttributeList.get(i).get(3));
                        newAdeElement.getContent().appendChild(newChildElement);
                    }
                }
            }
            addedAttributeItem = new AttributeItem(new NodeAttributeHandler((Node) newElement, addAttributeType));
        } else {
            Element newElement = createNewElement(((Node) baseAttributeItem.getContent()).getOwnerDocument(), value,
                    uom,
                    codeSpace,
                    addAttributeName);
            if (((Node) baseAttributeItem.getContent()).getFirstChild() != null) {
                if (hasTypeElement(baseAttributeItem.getName(),
                        ((Element) ((Node) baseAttributeItem.getContent()).getFirstChild()).getTagName())) {
                    ((Node) baseAttributeItem.getContent()).getFirstChild().appendChild(newElement);
                } else {
                    ((Node) baseAttributeItem.getContent()).appendChild(newElement);
                }
            } else {
                ((Node) baseAttributeItem.getContent()).appendChild(newElement);
            }
            addedAttributeItem = new AttributeItem(new NodeAttributeHandler((Node) newElement, addAttributeType));
            sortElement(baseAttributeItem, childAttributeList);
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
            ChildList<ADEComponent> bldgAttributeTree) {
        if (removeAttributeName.matches(MeasuredHeightManager.getName())) {
            MeasuredHeightManager.removeMeasuredHeight((AbstractBuilding) parentAttributeItem.getContent());
        } else if (parentAttributeItem.getName() == "root") {
            ADEComponent targetAttributeComponent = null;
            Node removeAttributeNode = (Node) removeAttributeItem.getContent();

            for (var adeComponent : bldgAttributeTree) {
                Node targetNode = ((ADEGenericElement) adeComponent).getContent();
                if (targetNode == removeAttributeNode)
                    targetAttributeComponent = adeComponent;
            }
            if (targetAttributeComponent != null)
                bldgAttributeTree.remove(targetAttributeComponent);
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

    private static Element createNewElement(org.w3c.dom.Document document, String value, String uom, String codeSpace,
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
        if (childElement.getTagName().toLowerCase().equals(parentElement.getTagName().toLowerCase())) {
            targetNodeList = childNode.getChildNodes();
            parentElement = childElement;
        }

        ArrayList<Node> sortedNodes = new ArrayList<>();
        if (targetNodeList != null) {
            for (int i = 0; i < targetNodeList.getLength(); i++) {
                sortedNodes.add(targetNodeList.item(i));
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
