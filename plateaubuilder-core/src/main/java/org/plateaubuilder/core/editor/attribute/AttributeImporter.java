package org.plateaubuilder.core.editor.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeDataCollection;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManagerFactory;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.world.World;
import org.w3c.dom.Element;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class AttributeImporter {
    public void importAttributeDataCollection(AttributeDataCollection attributeDataCollection) {
        Set<String> gmlIDList = attributeDataCollection.getKey();
        AttributeSchemaManager attributeSchemaManager;
        for (String gmlID : gmlIDList) {
            IFeatureView featureView = getFeatureView(gmlID);
            attributeSchemaManager = AttributeSchemaManagerFactory
                    .getSchemaManager(featureView.getGML());
            importAttributes(featureView, attributeSchemaManager, attributeDataCollection.getData(gmlID));
        }
    }

    private IFeatureView getFeatureView(String gmlID) {
        List<IFeatureView> features = getFeatureViews();
        for (var feature : features) {
            if (feature.getId().matches(gmlID)) {
                return feature;
            }
        }
        return null;
    }

    private List<IFeatureView> getFeatureViews() {
        List<Node> cityModelViews = World.getActiveInstance().getCityModelGroup().getChildren();
        List<IFeatureView> features = new ArrayList<>();
        for (Node cityModelView : cityModelViews) {
            CityModelView city = (CityModelView) cityModelView;
            features.addAll(city.getFeatureViews());
        }
        return features;
    }

    private void importAttributes(IFeatureView feature, AttributeSchemaManager attributeSchemaManager,
            TreeItem<AttributeItem> attributeTree) {
        AbstractCityObject object = feature.getGML();
        ChildList<ADEComponent> adeTree = (ChildList<ADEComponent>) attributeSchemaManager
                .initializeAttributes(feature);
        // printAttributeTree(attributeTree, 0);
        for (TreeItem<AttributeItem> childTree : attributeTree.getChildren()) {
            String attributeName = childTree.getValue().getName();
            if (attributeName.split(":")[0].matches("uro")) {
                importUroAttribute(childTree, adeTree, null);
            } else {
                importCityGmlAttribute(object, attributeSchemaManager, childTree);
            }
        }
    }

    private void importCityGmlAttribute(AbstractCityObject object, AttributeSchemaManager attributeSchemaManager,
            TreeItem<AttributeItem> attributeTree) {
        if (attributeTree == null || attributeTree.getValue() == null) {
            return; // ツリーがnullの場合は終了
        }

        // 現在のノードのAttributeItemを取得
        AttributeItem attributeItem = attributeTree.getValue();
        attributeSchemaManager.addAttribute(object, attributeItem.getName(), attributeItem.getValue());

        // 再帰的に子ノードを探索
        for (TreeItem<AttributeItem> child : attributeTree.getChildren()) {
            importCityGmlAttribute(object, attributeSchemaManager, child);
        }
    }

    private void importUroAttribute(TreeItem<AttributeItem> attributeTree, ChildList<ADEComponent> adeTree,
            Element parentElement) {
        AttributeItem attributeItem = attributeTree.getValue();

        // Nullチェック
        if (attributeItem == null) {
            return;
        }

        // 新しいADE要素を作成
        String attributeName = attributeItem.getName();
        String value = attributeItem.getValue();
        String uom = attributeItem.getUom();
        String codeSpace = attributeItem.getCodeSpace();

        if (attributeName.split(":")[0].equals("uro")) {
            Element newElement = AttributeEditor.createNewElement(
                    Editor.getUroSchemaDocument().getXSDDocument().getDocumentElement().getOwnerDocument(),
                    value, uom, codeSpace, attributeName);
            ADEGenericElement adeElement = new ADEGenericElement(newElement);
            if (parentElement != null) {
                parentElement.appendChild(newElement); // 親Elementに子要素を追加
            } else {
                adeTree.add(adeTree.size(), adeElement); // 親がなければadeTreeに追加
            }
            parentElement = newElement;
        }

        // 子要素があれば再帰的に処理
        for (TreeItem<AttributeItem> childTree : attributeTree.getChildren()) {
            importUroAttribute(childTree, adeTree, parentElement); // 再帰呼び出し
        }
    }

    private void printAttributeTree(TreeItem<AttributeItem> attributeTree, int depth) {
        // インデントを階層に応じて追加
        String indent = "  ".repeat(depth);

        // 現在のノードの情報を表示
        AttributeItem attributeItem = attributeTree.getValue();
        if (attributeItem != null) {
            System.out.println(indent + "Attribute Name: " + attributeItem.getName());
            System.out.println(indent + "  Value: " + attributeItem.getValue());
            System.out.println(indent + "  UOM: " + attributeItem.getUom());
            System.out.println(indent + "  CodeSpace: " + attributeItem.getCodeSpace());
        }

        // 子ノードがあれば再帰的に処理
        for (TreeItem<AttributeItem> child : attributeTree.getChildren()) {
            printAttributeTree(child, depth + 1); // 階層を深くして再帰呼び出し
        }
    }
}
