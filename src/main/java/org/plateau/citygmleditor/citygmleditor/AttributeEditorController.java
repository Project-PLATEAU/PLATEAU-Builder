package org.plateau.citygmleditor.citygmleditor;

import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.plateau.citygmleditor.citymodel.UroAttributeInfo;
import org.plateau.citygmleditor.citymodel.AttributeItem;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.text.Document;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.common.child.ChildList;
import java.util.List;
import org.citygml4j.model.citygml.ade.ADEComponent;

import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.control.ListCell;

public class AttributeEditorController implements Initializable {
    public TreeTableView<AttributeItem> attributeTreeTable;
    public TreeTableColumn<AttributeItem, String> keyColumn;
    public TreeTableColumn<AttributeItem, String> valueColumn;
    private AbstractBuilding selectedBuilding;
    private org.w3c.dom.Document uroAttributeDocument;
    @FXML
    private TitledPane titledPane;
    @FXML
    private Label featureID;
    @FXML
    private Label featureType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var activeFeatureProperty = CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty();
        // コンテキストメニュー
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("追加");
        MenuItem deleteItem = new MenuItem("削除");
        // 削除ボタン押下時の挙動
        deleteItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null) {
                // ツリービューから対象の行を削除
                selectedItem.getParent().getChildren().remove(selectedItem);

                // モデルの情報から対象の属性を削除
                String deleteAttributeKeyName = selectedItem.getValue().keyProperty().get();
                removeAttribute(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        deleteAttributeKeyName);
            }
        });
        // 追加ボタン押下時の挙動
        addItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null) {
                String selectedAttributeKeyName = selectedItem.getValue().keyProperty().get();
                showListView(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        selectedAttributeKeyName); // 例えば、選択された行の名前をshowListViewメソッドに渡す
            }
        });
        contextMenu.getItems().addAll(addItem, deleteItem);

        // ツリービューにコンテキストメニューを設定
        attributeTreeTable.setRowFactory(treeView -> {
            TreeTableRow<AttributeItem> row = new TreeTableRow<>();
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu));
            return row;
        });

        // ツリービュー全体にコンテキストメニューを設定
        // attributeTreeTable.setContextMenu(contextMenu);
        attributeTreeTable.rootProperty().bind(new ObjectBinding<>() {
            {
                bind(activeFeatureProperty);
            }

            @Override
            protected TreeItem<AttributeItem> computeValue() {
                var feature = activeFeatureProperty.get();
                if (feature == null)
                    return null;

                selectedBuilding = feature.getGMLObject();
                featureID.setText("地物ID：" + selectedBuilding.getId());
                featureType.setText("地物型：建築物（Buildings）");
                // System.out.println(feature.getClass().getName());
                var root = new TreeItem<>(new AttributeItem("", ""));
                {
                    var attribute = new AttributeItem(
                            "measuredHeight",
                            Double.toString(selectedBuilding.getMeasuredHeight().getValue()));
                    attribute.valueProperty().addListener((observable, oldValue, newValue) -> {
                        selectedBuilding.getMeasuredHeight().setValue(Double.parseDouble(newValue));
                    });
                    root.getChildren().add(new TreeItem<>(attribute));
                }

                {
                    addADEPropertyToTree(selectedBuilding, root);
                }

                root.setExpanded(true);

                return root;
            }
        });
        // attributeTreeTable.setOnMouseClicked(event -> {
        // // マウスがクリックされた座標を取得
        // double clickX = event.getX();
        // double clickY = event.getY();
        // TreeItem<AttributeItem> selectedItem =
        // attributeTreeTable.getSelectionModel().getSelectedItem();

        // // 選択されているアイテムがnullの場合、または空白の領域がクリックされた場合
        // if (selectedItem == null || !selectedItem.isLeaf()) {
        // // 選択をクリア
        // attributeTreeTable.getSelectionModel().clearSelection();

        // System.out.println("TreeViewの空白部分がクリックされました");
        // }
        // });

        attributeTreeTable.setShowRoot(false);

        keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("key"));
        valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));

        // attributeTreeTable.setOnKeyPressed(t -> {
        // if (t.getCode() == KeyCode.SPACE) {
        // TreeItem<Node> selectedItem =
        // attributeTreeTable.getSelectionModel().getSelectedItem();
        // if (selectedItem != null) {
        // Node node = selectedItem.getValue();
        // node.setVisible(!node.isVisible());
        // }
        // t.consume();
        // }
        // });

        valueColumn.setCellFactory(
                TextFieldTreeTableCell.forTreeTableColumn());
        valueColumn.setOnEditCommit(event -> {
            TreeItem<AttributeItem> editedItem = event.getRowValue();
            AttributeItem attribute = editedItem.getValue();
            attribute.valueProperty().set(event.getNewValue());
        });
    }

    private static void addADEPropertyToTree(AbstractBuilding selectedBuilding, TreeItem<AttributeItem> root) {
        for (var adeComponent : selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding()) {
            var adeElement = (ADEGenericElement) adeComponent;
            addXMLElementToTree(adeElement.getContent(), null, root);
        }
    }

    private static void addXMLElementToTree(Node node, Node parentNode, TreeItem<AttributeItem> root) {
        // 子が末尾の要素であるかチェック
        var firstChild = node.getFirstChild();
        String nodeTagName = ((Element) node).getTagName().toLowerCase();
        String parentNodeTagName = "";

        if (parentNode != null)
            parentNodeTagName = ((Element) parentNode).getTagName().toLowerCase();

        if (node.getChildNodes().getLength() == 1 && firstChild instanceof CharacterData) {
            if (parentNode != null && nodeTagName.equals(parentNodeTagName))
                return;
            // 子の内容を属性値として登録して再帰処理を終了
            var attribute = new AttributeItem(node.getNodeName(), firstChild.getNodeValue());
            attribute.valueProperty().addListener((observable, oldValue, newValue) -> {
                firstChild.setNodeValue(newValue);
            });
            var item = new TreeItem<>(attribute);
            root.getChildren().add(item);
            return;
        }

        var item = new TreeItem<>(
                new AttributeItem(node.getNodeName(), "", false));
        item.setExpanded(true);

        // XMLの子要素を再帰的に追加
        var children = node.getChildNodes();

        for (int i = 0; i < children.getLength(); ++i) {
            var xmlElement = children.item(i);

            // ここでの文字列要素はタブ・改行なので飛ばす
            if (xmlElement instanceof CharacterData)
                continue;

            // 親ノードのタグ名と同じ（小文字大文字は無視）であれば、そのノードは無視
            if (parentNode != null && nodeTagName.equals(parentNodeTagName)) {
                addXMLElementToTree(xmlElement, node, root);
            } else {
                addXMLElementToTree(xmlElement, node, item);
            }
        }
        if (parentNode != null && nodeTagName.equals(parentNodeTagName))
            return;
        root.getChildren().add(item);
    }

    private void removeAttribute(ChildList<ADEComponent> childList, String deleteAttributeKeyName) {
        for (int i = 0; i < childList.size(); i++) {
            var adeComponent = childList.get(i);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            String nodeTagName = ((Element) node).getTagName();
            // 第一階層の要素が削除対象である場合の処理
            if (nodeTagName.equals(deleteAttributeKeyName)) {
                childList.remove(i);
                return;
            }
            // 再帰的に削除対象の要素を探し、削除
            traverseAndRemoveAttribute(node, deleteAttributeKeyName);
        }
    }

    private void traverseAndRemoveAttribute(Node node, String deleteAttributeKeyName) {
        var childlenNode = node.getChildNodes();
        var firstChild = node.getFirstChild();
        if (childlenNode.getLength() == 1 && firstChild instanceof CharacterData)
            return;
        for (int i = 0; i < childlenNode.getLength(); ++i) {
            var xmlElement = childlenNode.item(i);
            // ここでの文字列要素はタブ・改行なので飛ばす
            if (xmlElement instanceof CharacterData)
                continue;
            Node xmlNode = ((Node) xmlElement);
            String nodeTagName = ((Element) xmlNode).getTagName();
            if (nodeTagName.equals(deleteAttributeKeyName))
                node.removeChild(xmlNode);
            traverseAndRemoveAttribute(xmlNode, deleteAttributeKeyName);
        }
    }

    private void addAttribute(ChildList<ADEComponent> childList, String parentAttributeName,
            String addAttributeName) {
        for (int i = 0; i < childList.size(); i++) {
            var adeComponent = childList.get(i);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            Element element = (Element) node;
            String nodeTagName = element.getTagName();
            // 第一階層の属性が削除対象である場合の処理
            if (nodeTagName.equals(parentAttributeName)) {

                NodeList childNodeList = node.getChildNodes();
                Node childNode = childNodeList.item(0);
                Element childElement = (Element) childNode;
                String childElementName = childElement.getTagName().toLowerCase();
                nodeTagName = nodeTagName.toLowerCase();
                org.w3c.dom.Document doc = element.getOwnerDocument();
                Element newElement = doc.createElement(addAttributeName);
                newElement.setTextContent("NULL");
                if (nodeTagName.matches(childElementName)) {
                    System.out.println("addElementTagName_match:" + childElement.getTagName());
                    childNode.appendChild(newElement);
                } else {
                    System.out.println("addElementTagName_notMatch:" + element.getTagName());
                    node.appendChild(newElement);
                }
            }
        }
    }

    private void showListView(ChildList<ADEComponent> childList, String selectedAttributeKeyName) {
        ArrayList<String> attributeList = getUroList(selectedAttributeKeyName);

        Stage pStage = new Stage();
        // ListView
        ListView<String> listView = new ListView<>();
        for (var attribute : attributeList) {
            listView.getItems().add(attribute);
        }

        // listView.setMaxWidth(100);
        // listView.setMaxHeight(150);

        // 選択モデルを取得し、リスナーを追加する
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("選択されたアイテム: " + newValue);
                addAttribute(childList, selectedAttributeKeyName, "uro:" + newValue);
            }
        });
        // 配置
        VBox vbRoot = new VBox();
        vbRoot.setAlignment(Pos.CENTER);
        vbRoot.setSpacing(10);
        vbRoot.getChildren().addAll(listView);

        pStage.setTitle("要素の追加");
        pStage.setWidth(300);
        pStage.setHeight(200);
        pStage.setScene(new Scene(vbRoot));
        pStage.show();
    }

    private ArrayList<String> getUroList(String targetName) {
        targetName = targetName.substring(4);
        System.out.println("targetNmae:" + targetName);
        // Uro要素の取得
        uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
        Node rootNode = uroAttributeDocument.getDocumentElement();
        Element targetElement = (Element) rootNode;
        NodeList elementNodeList = rootNode.getChildNodes();
        ArrayList<String> attributeList = new ArrayList<String>();
        for (int i = 0; i < elementNodeList.getLength(); i++) {
            Node node = elementNodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (targetName.equals(element.getAttribute("name"))) {
                    targetElement = (Element) node;
                    System.out.println("detected:" + element.getAttribute("name"));
                }
            }
        }

        NodeList targetNodeList = targetElement.getElementsByTagName("xs:element");
        for (int j = 0; j < targetNodeList.getLength(); j++) {
            Node node = targetNodeList.item(j);
            Element element = (Element) node;
            // System.out.println("targetNodeList.getAttribute+" +
            // element.getAttribute("name"));
            attributeList.add(element.getAttribute("name"));
        }
        return attributeList;
    }
}
