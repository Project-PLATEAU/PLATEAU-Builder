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

public class AttributeEditorController implements Initializable {
    public TreeTableView<AttributeItem> attributeTreeTable;
    public TreeTableColumn<AttributeItem, String> keyColumn;
    public TreeTableColumn<AttributeItem, String> valueColumn;
    private AbstractBuilding selectedBuilding;
    private org.w3c.dom.Document uroAttributeDocument;
    private ObjectProperty<BuildingView> activeFeatureProperty;
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
                showListView(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        null);
            } else if (selectedItem != null && selectedItem.getParent() != null) {
                // アイテム選択時
                String selectedAttributeKeyName = selectedItem.getValue().keyProperty().get();
                // 追加可能な属性一覧のメニューを出し、要素を追加
                showListView(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        selectedAttributeKeyName);
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
     * addAttribute
     * 要素の追加を行う
     *
     * @param childList           選択中の地物の要素リスト
     * @param parentAttributeName 選択中のリストビューのアイテム名
     * @param addAttributeName    追加する要素の名前
     * @param type                追加する要素が持つタイプ
     * @param attributeList       パースしたuro要素の情報一覧
     */
    private void addAttribute(ChildList<ADEComponent> childList, String parentAttributeName,
            String addAttributeName, String type, ArrayList<ArrayList<String>> attributeList) {
        String namespaceURI = uroAttributeDocument.getDocumentElement().getAttribute("xmlns:uro");

        if (parentAttributeName == null) {
            // ルート要素を追加する際の処理
            var adeComponent = childList.get(0);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            Element element = (Element) node;
            org.w3c.dom.Document doc = node.getOwnerDocument();

            if (addAttributeName != null) {
                Element newElement = doc.createElementNS(namespaceURI, addAttributeName);
                newElement.setTextContent("NULL");
                ADEGenericElement newAdelement = new ADEGenericElement(newElement);
                childList.add(childList.size(), (ADEComponent) newAdelement);

                // 型要素があるかどうかを確認し、あれば追加
                for (int i = 0; i < attributeList.size(); i++) {
                    if (!attributeList.get(i).isEmpty() && attributeList.get(i).get(2) != null) {
                        if (("uro:" + attributeList.get(i).get(2).toLowerCase())
                                .matches(addAttributeName.toLowerCase())) {
                            Node parentNode = newAdelement.getContent();
                            Element newChildElement = doc.createElementNS(namespaceURI,
                                    "uro:" + attributeList.get(i).get(2));
                            parentNode.appendChild(newChildElement);
                        }
                    }
                }
            }
        } else {
            // 第二階層以下を追加する際の処理
            for (int i = 0; i < childList.size(); i++) {
                var adeComponent = childList.get(i);
                var adeElement = (ADEGenericElement) adeComponent;
                Node node = adeElement.getContent();
                Element element = (Element) node;
                String nodeTagName = element.getTagName();

                // 親要素を見つけたら新要素を追加
                if (nodeTagName.equals(parentAttributeName)) {
                    NodeList childNodeList = node.getChildNodes();
                    Node childNode = childNodeList.item(0);
                    nodeTagName = nodeTagName.toLowerCase();
                    org.w3c.dom.Document doc = element.getOwnerDocument();
                    Element newElement = doc.createElementNS(namespaceURI, addAttributeName);
                    newElement.setTextContent("NULL");

                    if (type.matches("gml:CodeType")) {
                        inputCodeSpace(newElement);
                    } else if (type.matches("gml:MeasureType") | type.matches("gml:LengthType")
                            | type.matches("gml::MeasureOrNullListType")) {
                        inputUom(newElement);
                    }

                    if (childNode != null) {
                        if (nodeTagName.matches(((Element) childNode).getTagName().toLowerCase())) {
                            childNode.appendChild(newElement);
                        } else {
                            node.appendChild(newElement);
                        }
                    } else {
                        node.appendChild(newElement);
                    }
                }
            }
        }
        // 要素をソート
        sortElement(childList, parentAttributeName, attributeList);
        refreshListView();
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

    /**
     * showListView
     * 追加メニューの一覧に乗せるUro要素の一覧を表示し、追加を行う
     *
     * @param childList                選択中の地物の要素リスト
     * @param selectedAttributeKeyName 選択中のリストビューのアイテム名
     * @return メニューに表示させる要素リスト
     */
    private ArrayList<ArrayList<String>> showListView(ChildList<ADEComponent> childList,
            String selectedAttributeKeyName) {
        Stage pStage = new Stage();
        ArrayList<ArrayList<String>> attributeList = getUroList(selectedAttributeKeyName);
        if (attributeList.size() == 0) {
            // アラートを作成
            Alert alert = new Alert(AlertType.WARNING);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("alert");
            alert.setTitle("追加エラー");
            alert.setHeaderText(null);
            alert.setContentText("追加できる要素がありません。");

            // アラートを表示
            alert.showAndWait();
            return null;
        }
        // ListView
        attributeListView = new ListView<>();

        for (ArrayList<String> attribute : attributeList) {
            attributeListView.getItems().add(attribute.get(0));
        }

        // メニュー内の要素をダブルクリックで要素を追加
        attributeListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedItem = attributeListView.getSelectionModel().getSelectedItem();
                int selectedIndex = attributeListView.getSelectionModel().getSelectedIndex();
                // 要素を追加
                addAttribute(childList, selectedAttributeKeyName, "uro:" + selectedItem,
                        attributeList.get(selectedIndex).get(1), attributeList);
                pStage.close();
            }
        });

        // 検索欄
        searchField = new TextField();
        searchField.setPromptText("Search");

        // 検索用リスナーを追加
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            attributeListView.getItems().clear();
            if (newValue == null || newValue.isEmpty()) {
                for (var attribute : attributeList) {
                    attributeListView.getItems().add(attribute.get(0));
                }
            } else {
                for (var attribute : attributeList) {
                    if (attribute.get(0).toLowerCase().startsWith(newValue.toLowerCase())) {
                        attributeListView.getItems().add(attribute.get(0));
                    }
                }
            }
        });

        // 配置
        VBox vbRoot = new VBox();
        vbRoot.setAlignment(Pos.CENTER);
        vbRoot.setSpacing(20);
        vbRoot.getChildren().addAll(searchField);
        vbRoot.getChildren().addAll(attributeListView);

        pStage.setTitle("要素の追加");
        pStage.setWidth(500);
        pStage.setHeight(300);
        pStage.setScene(new Scene(vbRoot));
        pStage.show();
        return attributeList;
    }

    /**
     * getUroList
     * 追加メニューの一覧に乗せるUro要素の一覧を返す
     *
     * @param targetName 地物情報のリスト
     * @return メニューに表示させる要素リスト
     */
    private ArrayList<ArrayList<String>> getUroList(String targetName) {
        ArrayList<ArrayList<String>> attributeList = new ArrayList<ArrayList<String>>();
        ArrayList<String> treeViewRootItemList = new ArrayList<String>();
        ArrayList<String> treeViewChildItemList = new ArrayList<String>();
        TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();

        // 追加済みのルート要素の名前を取得
        for (TreeItem<AttributeItem> item : attributeTreeTable.getRoot().getChildren()) {
            treeViewRootItemList.add(item.getValue().keyProperty().get());
        }

        // 追加済みの子要素の名前を取得
        if (selectedItem != null) {
            // 子アイテムのリストを取得
            var children = selectedItem.getChildren();
            // 子アイテムを処理する
            for (TreeItem<AttributeItem> child : children) {
                treeViewChildItemList.add(child.getValue().keyProperty().get());
            }
        }
        if (targetName == null) {
            // Root要素の追加
            // Uro要素の取得
            uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
            Node rootNode = uroAttributeDocument.getDocumentElement();
            Element targetElement = (Element) rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    targetElement = (Element) node;
                    // すでに追加済みのアイテムは除く
                    if (!treeViewRootItemList.contains("uro:" + targetElement.getAttribute("name"))) {
                        ArrayList<String> attributeSet = new ArrayList<String>();
                        attributeSet.add(targetElement.getAttribute("name"));
                        attributeSet.add(targetElement.getAttribute("type"));

                        Node childNode = node.getChildNodes().item(0);
                        Element childElement = (Element) childNode;
                        if (childElement != null) {
                            attributeSet.add(childElement.getAttribute("name"));
                        } else {
                            attributeSet.add(null);
                        }
                        attributeList.add(attributeSet);
                    }
                }
            }
        } else {
            // 第二階層以下の要素の追加
            targetName = targetName.substring(4);

            // 追加対象の基準となる親要素を取得
            uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
            Node rootNode = uroAttributeDocument.getDocumentElement();
            Element targetElement = (Element) rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (targetName.equals(element.getAttribute("name"))) {
                        targetElement = (Element) node;
                    }
                }
            }
            if (!targetElement.getTagName().equals("uro")) {
                // 基準となる要素の子要素を取得
                NodeList targetNodeList = targetElement.getElementsByTagName("xs:element");

                for (int j = 0; j < targetNodeList.getLength(); j++) {
                    Node node = targetNodeList.item(j);
                    Element element = (Element) node;
                    int count = 0;
                    for (String itemName : treeViewChildItemList) {
                        if (itemName.equals("uro:" + element.getAttribute("name"))) {
                            count++;
                        }
                    }
                    if (!targetName.toLowerCase().matches(element.getAttribute("name").toLowerCase())) {
                        String maxOccurs = element.getAttribute("maxOccurs");
                        int max;
                        if (maxOccurs.equals("unbounded")) {
                            max = Integer.MAX_VALUE;
                        } else if (maxOccurs == "") {
                            max = 1;
                        } else {
                            max = Integer.parseInt(maxOccurs);
                        }
                        if (count < max) {
                            ArrayList<String> attributeSet = new ArrayList<>();
                            attributeSet.add(element.getAttribute("name"));
                            attributeSet.add(element.getAttribute("type"));
                            attributeSet.add(element.getAttribute("minOccurs"));
                            attributeList.add(attributeSet);
                        }
                    }
                }
            }
        }
        return attributeList;
    }

    /**
     * CodeSpace
     * CodeSpace属性を入力させ、属性として格納する
     */
    private void inputCodeSpace(Element element) {
        String datasetPath = CityGMLEditorApp.getDatasetPath();
        String codeListPath = datasetPath + "\\codelists";
        Stage pStage = new Stage();
        ListView<String> listView = new ListView<>();
        File folder = new File(codeListPath);

        // フォルダ内のファイル名をリストビューに追加
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            listView.getItems().add(file.getName());
        }

        // リストビューのアイテムがダブルクリックされた場合の処理
        listView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedFile = listView.getSelectionModel().getSelectedItem();
                element.setAttribute("codeSpace", "../../codelists/" + selectedFile);
                // リストビューを閉じる
                pStage.close();
            }
        });

        // 配置
        VBox vbRoot = new VBox();
        vbRoot.setAlignment(Pos.CENTER);
        vbRoot.setSpacing(20);
        vbRoot.getChildren().addAll(listView);

        pStage.setTitle("codeSpaceの選択");
        pStage.setWidth(500);
        pStage.setHeight(300);
        pStage.setScene(new Scene(vbRoot));
        pStage.show();
    }

    /**
     * inputUom
     * Uom属性を入力させ、属性として格納する
     */
    private void inputUom(Element element) {
        // テキスト入力ダイアログの作成
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("uom属性入力フォーム");
        dialog.setHeaderText("原則：長さの単位は m,面積の単位は m2,時間の単位は hour");
        dialog.setContentText("uom:");

        // ダイアログを表示し、結果を取得
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(uomValue -> {
            element.setAttribute("uom", uomValue);
        });
    }

    /**
     * sortElement
     * 要素をソートしてモデル情報に反映する
     *
     * @param childList           選択中の地物のNodeList
     * @param parentAttributeName ソート対象要素の親の名前
     * @param attributeList       パースしたuro要素の情報一覧（ソートの基準となる）
     */
    private void sortElement(ChildList<ADEComponent> childList, String parentAttributeName,
            ArrayList<ArrayList<String>> attributeList) {
        NodeList targetNodeList = null;
        ArrayList<String> nameOrder = new ArrayList<>();
        Element parentElement = null;

        // 名前のリストの作成
        for (ArrayList<String> attribute : attributeList) {
            if (!attribute.isEmpty()) {
                // 各リストの最初の要素をnameOrderに追加
                nameOrder.add("uro:" + attribute.get(0));
            }
        }

        // ソート対象のNodeListの取得
        for (int i = 0; i < childList.size(); i++) {
            var adeComponent = childList.get(i);
            var adeElement = (ADEGenericElement) adeComponent;
            Element element = (Element) adeElement.getContent();

            // 親要素を見つけたら新要素を追加
            if (element.getTagName().equals(parentAttributeName)) {
                parentElement = element;
                targetNodeList = element.getChildNodes();
                Node childNode = targetNodeList.item(0);
                Element childElement = (Element) childNode;
                if (childElement.getTagName().toLowerCase().equals(element.getTagName().toLowerCase())) {
                    targetNodeList = childNode.getChildNodes();
                    parentElement = childElement;
                }
            }
        }

        // NodeListをArrayListに変換
        ArrayList<Node> sortedNodes = new ArrayList<>();
        if (targetNodeList != null) {
            for (int i = 0; i < targetNodeList.getLength(); i++) {
                sortedNodes.add(targetNodeList.item(i));
            }
            // ソート
            Collections.sort(sortedNodes, new Comparator<Node>() {
                @Override
                public int compare(Node node1, Node node2) {
                    int index1 = nameOrder.indexOf(node1.getNodeName());
                    int index2 = nameOrder.indexOf(node2.getNodeName());
                    // nameOrderに含まれていない要素はリストの最後に配置
                    index1 = index1 == -1 ? Integer.MAX_VALUE : index1;
                    index2 = index2 == -1 ? Integer.MAX_VALUE : index2;
                    return Integer.compare(index1, index2);
                }
            });

            clearNodeChildren((Node) parentElement);
            setNewNodeChildren((Node) parentElement, sortedNodes);
        }
    }

    /**
     * clearNodeChildren
     * 子ノードをクリアする
     */
    private void clearNodeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    /**
     * setNewNodeChildren
     * 新しいNodeListをNodeに格納する
     *
     * @param node       親ノード
     * @param childNodes 追加したいノード
     */
    private void setNewNodeChildren(Node node, ArrayList<Node> childNodes) {
        for (int i = 0; i < childNodes.size(); i++) {
            node.appendChild(childNodes.get(i));
        }
    }

    /**
     * printNode
     * （デバッグ用）ノードリストを可視化する
     * 子ノードがあればprintNode()を呼び出す
     *
     * @param nodeList 表示対象のノードリスト
     */
    private void printNodeList(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            printNode(node, 0);
        }
    }

    /**
     * printNode
     * （デバッグ用）ノードを可視化する
     *
     * @param node   表示対象のノード
     * @param indent 子を表すためのインデントの数
     */
    private void printNode(Node node, int indent) {
        // インデント文字列の生成
        String indentString = new String(new char[indent]).replace("\0", "    ");

        // ノードの基本情報を表示
        System.out.println(indentString + "Node Name: " + node.getNodeName() + ", Type: " + node.getNodeType());
        // 子ノードがある場合は再帰的に表示
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                printNode(children.item(i), indent + 1);
            }
        }
    }
}
