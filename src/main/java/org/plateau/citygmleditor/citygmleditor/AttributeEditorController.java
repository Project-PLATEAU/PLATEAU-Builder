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
import org.plateau.citygmleditor.validation.AttributeValidator;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Flow.Subscriber;

import javax.swing.text.Document;
import javax.swing.text.ElementIterator;

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
import java.util.Objects;
import java.util.Optional;

import org.citygml4j.model.citygml.ade.ADEComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TextInputDialog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AttributeEditorController implements Initializable {
    public TreeTableView<AttributeItem> attributeTreeTable;
    public TreeTableColumn<AttributeItem, String> keyColumn;
    public TreeTableColumn<AttributeItem, String> valueColumn;
    private AbstractBuilding selectedBuilding;
    private org.w3c.dom.Document uroAttributeDocument;
    private ObjectProperty<BuildingView> activeFeatureProperty;
    private AttributeAddingMenuController attributeAddingMenuController = new AttributeAddingMenuController();
    @FXML
    private TitledPane titledPane;
    @FXML
    private Label featureID;
    @FXML
    private Label featureType;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> attributeListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        activeFeatureProperty = CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty();

        // コンテキストメニュー
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("追加");
        MenuItem deleteItem = new MenuItem("削除");

        // 削除ボタン押下時の挙動
        deleteItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null) {
                // 選択状態のアイテム名を取得
                String selectedAttributeKeyName = selectedItem.getValue().keyProperty().get();
                // 親のTreeItemを取得し、アイテム名を取得
                TreeItem<AttributeItem> parentItem = selectedItem.getParent();
                String parentAttributeKeyName = parentItem.getValue().keyProperty().get();
                // 削除処理
                removeAttribute(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        selectedAttributeKeyName, parentAttributeKeyName);
            }
        });

        // 追加ボタン押下時の挙動
        addItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                // 未選択状態時
                try {
                    // 追加可能な属性一覧のメニューを出し、要素を追加
                    attributeAddingMenuController.showListView(
                            (ChildList<ADEComponent>) selectedBuilding
                                    .getGenericApplicationPropertyOfAbstractBuilding(),
                            null, selectedItem, attributeTreeTable);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (selectedItem != null && selectedItem.getParent() != null) {
                // アイテム選択時
                String selectedAttributeKeyName = selectedItem.getValue().keyProperty().get();
                try {
                    // 追加可能な属性一覧のメニューを出し、要素を追加
                    attributeAddingMenuController.showListView(
                            (ChildList<ADEComponent>) selectedBuilding
                                    .getGenericApplicationPropertyOfAbstractBuilding(),
                            selectedAttributeKeyName, selectedItem, attributeTreeTable);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        contextMenu.getItems().addAll(addItem, deleteItem);

        // TreeViewの各行に対する処理
        attributeTreeTable.setRowFactory(treeView -> {
            TreeTableRow<AttributeItem> row = new TreeTableRow<>() {
                // 削除不可能アイテムの背景色をグレーにする処理
                @Override
                protected void updateItem(AttributeItem item, boolean empty) {
                    super.updateItem(item, empty);
                    // 項目が空、または存在しない場合、背景色なし。
                    if (empty || item == null) {
                        setStyle("");
                    } else {
                        // 項目が存在する場合のみ、チェックを行う。
                        TreeItem<AttributeItem> parentItem = getTreeItem().getParent();
                        String parentKey = parentItem != null ? parentItem.getValue().keyProperty().get() : null;
                        if (!isDeletable(item.keyProperty().get(), parentKey)) {
                            setStyle("-fx-background-color: grey;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };
            // 空白部分をクリックした際にアイテムの選択状態をクリアにする処理
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && row.isEmpty()) {
                    // クリックされたセルの行からアイテムを取得
                    AttributeItem item = row.getItem();
                    if (item == null) {
                        // アイテムが存在しない場合、選択状態をクリア
                        attributeTreeTable.getSelectionModel().clearSelection();
                    }
                }
            });

            return row;
        });

        // ツリービュー全体にコンテキストメニューを設定
        attributeTreeTable.setContextMenu(contextMenu);

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

        attributeTreeTable.setShowRoot(false);

        keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("key"));
        valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));

        valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

        valueColumn.setOnEditCommit(event -> {
            TreeItem<AttributeItem> editedItem = event.getRowValue();
            AttributeItem attributeItem = editedItem.getValue();
            AttributeItem parentAttributeItem = editedItem.getParent().getValue();
            String newValue = event.getNewValue();
            String attributeItemName = attributeItem.keyProperty().get();
            String parentAttributeItemName = parentAttributeItem.keyProperty().get();

            // 編集したアイテムが持つType情報を取得
            String type = getType(attributeItemName, parentAttributeItemName);
            // バリデーションチェック
            if (type == null) {
                attributeItem.valueProperty().set(event.getNewValue());
                System.out.println("event.getNewValue():" + event.getNewValue());
            } else if (AttributeValidator.checkValue(newValue, type)) {
                attributeItem.valueProperty().set(event.getNewValue());
            } else {
                // アラートを作成
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("変更エラー");
                alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
                alert.getDialogPane().getStyleClass().add("alert");
                alert.setHeaderText(null);
                alert.setContentText("変更後の値が要素の条件を満たしていません。\n" + type + "に従ってください");
                // アラートを表示
                alert.showAndWait();
                attributeItem.valueProperty().set(event.getOldValue());
                attributeTreeTable.refresh();
            }
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

    /**
     * removeAttribute
     * 対象の要素を削除する
     *
     * @param childList                    確認対象の要素
     * @param deleteAttributeKeyName       削除対象の要素の名前
     * @param deleteAttributeParentKeyName 削除対象の親要素の名前（ツリービュー上）
     */
    private void removeAttribute(ChildList<ADEComponent> childList, String deleteAttributeKeyName,
            String deleteAttributeParentKeyName) {
        if (deleteAttributeParentKeyName != null) {
            // 削除可能な対象かを確認
            if (!isDeletable(deleteAttributeKeyName, deleteAttributeParentKeyName)) {
                // アラートを作成
                Alert alert = new Alert(AlertType.WARNING);
                alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
                alert.getDialogPane().getStyleClass().add("alert");
                alert.setTitle("削除エラー");
                alert.setHeaderText(null);
                alert.setContentText("削除できない要素です。");

                // アラートを表示
                alert.showAndWait();
                return;
            }
        }
        for (int i = 0; i < childList.size(); i++) {
            var adeComponent = childList.get(i);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            String nodeTagName = ((Element) node).getTagName();
            // 第一階層の要素が削除対象である場合、削除
            if (nodeTagName.equals(deleteAttributeKeyName)) {
                childList.remove(i);
                return;
            }
            // 再帰的に削除対象の要素を探し、削除
            traverseAndRemoveAttribute(node, deleteAttributeKeyName);
        }
        refreshListView();
    }

    /**
     * traverseAndRemoveAttribute
     * 親メソッドから与えられたノードの子要素を探索し、削除する
     *
     * @param node                   確認対象のノード群
     * @param deleteAttributeKeyName 削除対象の要素の名前
     */
    private void traverseAndRemoveAttribute(Node node, String deleteAttributeKeyName) {
        var childNodes = node.getChildNodes();
        var firstChild = node.getFirstChild();
        if (childNodes.getLength() == 1 && firstChild instanceof CharacterData)
            return;
        for (int i = 0; i < childNodes.getLength(); ++i) {
            var childNode = childNodes.item(i);

            // ここでの文字列要素はタブ・改行なので飛ばす
            if (childNode instanceof CharacterData)
                continue;
            // Node xmlNode = ((Node) childNode);
            String tagName = ((Element) childNode).getTagName();
            if (tagName.equals(deleteAttributeKeyName)) {
                node.removeChild(childNode);
            }
            traverseAndRemoveAttribute(childNode, deleteAttributeKeyName);
        }
    }

    /**
     * isDeletable
     * 対象の要素が削除可能かどうかを判別する
     *
     * @param deleteAttributeKeyName       削除対象の要素の名前
     * @param deleteAttributeParentKeyName 削除対象の親要素の名前（ツリービュー上）
     */
    private Boolean isDeletable(String deleteAttributeKeyName,
            String deleteAttributeParentKeyName) {

        uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
        Node rootNode = uroAttributeDocument.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();
        Element baseElement = null;

        // 削除対象の基準となる親要素を取得
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (deleteAttributeParentKeyName.equals("uro:" + element.getAttribute("name"))) {
                    baseElement = element;
                }
            }
        }
        if (baseElement != null) {
            // 基準となる要素の子要素を取得
            NodeList targetNodeList = baseElement.getElementsByTagName("xs:element");
            for (int j = 0; j < targetNodeList.getLength(); j++) {
                Node node = targetNodeList.item(j);
                Element element = (Element) node;
                if (deleteAttributeKeyName.matches("uro:" + element.getAttribute("name"))) {
                    if (element.getAttribute("minOccurs") == "") {
                        return false;
                    } else if (Integer.parseInt(element.getAttribute("minOccurs")) > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * getType
     * 対象の要素のtype属性の内容を取得する
     *
     * @param attributeKeyName       確認対象の要素
     * @param parentAttributeKeyName 確認対象の親要素の名前（ツリービュー上）
     */
    private String getType(String attributeKeyName,
            String parentAttributeKeyName) {
        uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
        Node rootNode = uroAttributeDocument.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();
        Element baseElement = null;

        // 削除対象の基準となる親要素を取得
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (parentAttributeKeyName.equals("uro:" + element.getAttribute("name"))) {
                    baseElement = element;
                }
            }
        }

        if (baseElement != null) {
            // 基準となる要素の子要素を取得
            NodeList targetNodeList = baseElement.getElementsByTagName("xs:element");
            for (int j = 0; j < targetNodeList.getLength(); j++) {
                Node node = targetNodeList.item(j);
                Element element = (Element) node;
                if (attributeKeyName.matches("uro:" + element.getAttribute("name"))) {
                    return element.getAttribute("type");
                }
            }
        }
        return null;
    }

    /**
     * refreshListView
     * リストビューの更新を行う
     */
    private void refreshListView() {
        BuildingView currentSelectedBuilding = activeFeatureProperty.get();
        activeFeatureProperty.set(null); // 一旦 null に設定
        activeFeatureProperty.set(currentSelectedBuilding); // そして元の値に戻す
    }
}
