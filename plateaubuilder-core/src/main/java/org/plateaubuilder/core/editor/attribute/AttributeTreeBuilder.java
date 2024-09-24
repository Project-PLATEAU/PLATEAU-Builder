package org.plateaubuilder.core.editor.attribute;

import java.util.ArrayList;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.BuildingSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.reader.XSDSchemaDocument;
import org.plateaubuilder.core.citymodel.attribute.wrapper.NodeAttributeHandler;
import org.plateaubuilder.core.editor.Editor;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javafx.scene.control.TreeItem;

public class AttributeTreeBuilder {
    private XSDSchemaDocument uroSchemaDocument = Editor.getUroSchemaDocument();

    public static void bldgAddAttributeTree(IFeatureView selectedFeature,
            TreeItem<AttributeItem> root) {
        if (selectedFeature != null)
            BuildingSchemaManager.addAttributeToTreeView(selectedFeature, root);
    }

    public static void addADEPropertyToTree(IFeatureView selectedFeature, TreeItem<AttributeItem> root) {
        for (var adeComponent : getADEComponents(selectedFeature)) {
            var adeElement = (ADEGenericElement) adeComponent;
            addXMLElementToTree(adeElement.getContent(), null, root);
        }
    }

    public static void addXMLElementToTree(Node node, Node parentNode, TreeItem<AttributeItem> root) {
        // 子が末尾の要素であるかチェック
        String nodeTagName = ((Element) node).getTagName().toLowerCase();
        String parentNodeTagName = "";
        String uom = ((Element) node).getAttribute("uom");
        String codeSpace = ((Element) node).getAttribute("codeSpace");
        String type = null;

        if (parentNode != null) {
            parentNodeTagName = ((Element) parentNode).getTagName().toLowerCase();
            type = Editor.getUroSchemaDocument().getType(node.getNodeName(), parentNode.getNodeName(), "uro");
        } else {
            type = Editor.getUroSchemaDocument().getType(node.getNodeName(), null, "uro");
        }

        if (node.getChildNodes().getLength() == 1 && node.getFirstChild() instanceof CharacterData) {
            if (parentNode != null && nodeTagName.equals(parentNodeTagName))
                return;
            // 子の内容を属性値として登録して再帰処理を終了
            AttributeItem attributeItem = new AttributeItem(new NodeAttributeHandler(node, type));

            var item = new TreeItem<>(attributeItem);
            root.getChildren().add(item);
            return;
        }

        var item = new TreeItem<>(
                new AttributeItem(new NodeAttributeHandler(node, type)));
        item.setExpanded(true);

        // XMLの子要素を再帰的に追加
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            var childNode = children.item(i);

            // ここでの文字列要素はタブ・改行なので飛ばす
            if (childNode instanceof CharacterData)
                continue;

            // 親ノードのタグ名と同じであれば、そのノードは無視
            if (parentNode != null && nodeTagName.equals(parentNodeTagName)) {
                addXMLElementToTree(childNode, node, root);
            } else {
                addXMLElementToTree(childNode, node, item);
            }
        }
        if (parentNode != null && nodeTagName.equals(parentNodeTagName))
            return;
        root.getChildren().add(item);
    }

    public static ChildList<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
        var gml = selectedFeature.getGML();
        if (gml instanceof AbstractBuilding) {
            return (ChildList<ADEComponent>) ((AbstractBuilding) gml).getGenericApplicationPropertyOfAbstractBuilding();
        } else if (gml instanceof Road) {
            return (ChildList<ADEComponent>) ((Road) gml).getGenericApplicationPropertyOfRoad();
        }
        return (ChildList<ADEComponent>) new ArrayList<ADEComponent>();
    }
}
