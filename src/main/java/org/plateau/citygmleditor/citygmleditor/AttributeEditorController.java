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
import org.plateau.citygmleditor.citymodel.CodeSpaceAttributeInfo;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import javafx.scene.control.Tooltip;
import org.plateau.citygmleditor.citymodel.CodeSpaceValue;
import org.plateau.citygmleditor.citymodel.AttributeValue;

public class AttributeEditorController implements Initializable {
    public TreeTableView<AttributeItem> attributeTreeTable;
    public TreeTableColumn<AttributeItem, String> keyColumn;
    public TreeTableColumn<AttributeItem, String> valueColumn;
    private AbstractBuilding selectedBuilding;
    private org.w3c.dom.Document uroAttributeDocument;
    private ObjectProperty<BuildingView> activeFeatureProperty;
    @FXML
    private TitledPane titledPane;
    // @FXML
    // private Label featureID;
    // @FXML
    // private Label featureType;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeFeatureProperty = CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty();

        // コンテキストメニュー
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("追加");
        MenuItem editItem = new MenuItem("編集");
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
                // 親のインデックスを取得
                int parentIndex = attributeTreeTable.getRoot().getChildren().indexOf(parentItem);
                // 選択されたアイテムのインデックスを親の子リストから取得
                int selectedIndex = parentItem.getChildren().indexOf(selectedItem);
                // 削除処理
                removeAttribute(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        selectedAttributeKeyName, parentAttributeKeyName, parentIndex, selectedIndex);
            }
        });

        // 追加ボタン押下時の挙動
        addItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                // 未選択状態時
                showListView(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        null, -2, -2);
            } else if (selectedItem != null && selectedItem.getParent() != null) {
                // アイテム選択時
                TreeItem<AttributeItem> parentItem = selectedItem.getParent();
                String parentAttributeKeyName = parentItem.getValue().keyProperty().get();
                // 親のインデックスを取得
                Integer parentIndex = attributeTreeTable.getRoot().getChildren().indexOf(parentItem);
                String selectedAttributeKeyName = selectedItem.getValue().keyProperty().get();
                // 選択されたアイテムのインデックスを親の子リストから取得
                int selectedIndex = parentItem.getChildren().indexOf(selectedItem);
                // 追加可能な属性一覧のメニューを出し、要素を追加
                showListView(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        selectedAttributeKeyName, parentIndex, selectedIndex);
            }
        });

        // 編集ボタン押下時の挙動
        editItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getParent() != null) {
                // 選択状態のアイテム名を取得
                String selectedAttributeKeyName = selectedItem.getValue().keyProperty().get();
                // 親のTreeItemを取得し、アイテム名を取得
                TreeItem<AttributeItem> parentItem = selectedItem.getParent();
                String parentAttributeKeyName = parentItem.getValue().keyProperty().get();
                // 親のインデックスを取得
                int parentIndex = attributeTreeTable.getRoot().getChildren().indexOf(parentItem);
                // 選択されたアイテムのインデックスを親の子リストから取得
                int selectedIndex = parentItem.getChildren().indexOf(selectedItem);
                editAttribute(
                        (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding(),
                        selectedAttributeKeyName, parentAttributeKeyName, parentIndex, selectedIndex);
            }
        });
        contextMenu.getItems().addAll(addItem, editItem, deleteItem);

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

                    if (item != null) {
                        // ツールチップ用のテキストを構築
                        StringBuilder tooltipText = new StringBuilder();
                        if (item.uomProperty().getValue() != null
                                && !item.uomProperty().getValue().isEmpty()) {
                            tooltipText.append("uom: ").append(item.uomProperty().getValue());
                        }
                        if (item.codeSpaceProperty().getValue() != null
                                && !item.codeSpaceProperty().getValue().isEmpty()) {
                            if (tooltipText == null) {
                                tooltipText.append("\n");
                            }
                            tooltipText.append("codeSpace: ").append(item.codeSpaceProperty().getValue());
                        }
                        // ツールチップを設定
                        if (tooltipText.length() > 0) {
                            Tooltip tooltip = new Tooltip(tooltipText.toString());
                            setTooltip(tooltip);
                        } else {
                            setTooltip(null);
                        }
                    } else {
                        setTooltip(null);
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
                // featureID.setText("地物ID：" + selectedBuilding.getId());
                // featureType.setText("地物型：建築物（Buildings）");
                var root = new TreeItem<>(new AttributeItem("", "", "", ""));
                {
                    var attribute = new AttributeItem(
                            "measuredHeight",
                            Double.toString(selectedBuilding.getMeasuredHeight().getValue()), "", "");
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
            if (AttributeValidator.checkValue(newValue, type)) {
                attributeItem.valueProperty().set(event.getNewValue());
            } else {
                // アラートを作成
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("変更エラー");
                alert.setHeaderText(null);
                alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
                alert.getDialogPane().getStyleClass().add("alert");
                alert.setContentText("変更後の値が要素の条件を満たしていません。\n" + type + "に従ってください");
                // アラートを表示
                alert.showAndWait();
                attributeItem.valueProperty().set(event.getOldValue());
                attributeTreeTable.refresh();
            }


    private void showRootAttributeAdditionPanel() {
        Consumer<AddAttributeListViewController> onAdd = (AddAttributeListViewController attributeAdditionPanel) -> {
            // addAttribute(
            // attributeAdditionPanel.getSelectedTagName(),
            // null,
            // attributeAdditionPanel.getAttributeList().get(attributeAdditionPanel.getSelectedIndex()).get(1),
            // attributeAdditionPanel.getAttributeList());
        };

        // 未選択もしくはルート選択状態時
        AddAttributeListViewController.createModal(attributeTreeTable.getRoot(), true, onAdd);
    }

    private void showAttributeAdditionPanel() {
        TreeItem<AttributeItem> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();

        Consumer<AddAttributeListViewController> onAdd = (AddAttributeListViewController attributeAdditionPanel) -> {
            // addAttribute(
            // attributeAdditionPanel.getSelectedTagName(),
            // selectedItem.getValue().keyProperty().get(),
            // attributeAdditionPanel.getAttributeList().get(attributeAdditionPanel.getSelectedIndex()).get(1),
            // attributeAdditionPanel.getAttributeList());
        };


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
        String uom = ((Element) node).getAttribute("uom");
        String codeSpace = ((Element) node).getAttribute("codeSpace");
        if (parentNode != null)
            parentNodeTagName = ((Element) parentNode).getTagName().toLowerCase();

        if (node.getChildNodes().getLength() == 1 && firstChild instanceof CharacterData) {
            if (parentNode != null && nodeTagName.equals(parentNodeTagName))
                return;
            // 子の内容を属性値として登録して再帰処理を終了
            AttributeItem attribute;
            // if (uom == "") {
            attribute = new AttributeItem(node.getNodeName(), firstChild.getNodeValue(), uom, codeSpace);
            // } else {
            // attribute = new AttributeItem(node.getNodeName() + " uom(" + uom + ")",
            // firstChild.getNodeValue(), uom,
            // codeSpace);
            // }
            attribute.valueProperty().addListener((observable, oldValue, newValue) -> {
                firstChild.setNodeValue(newValue);
            });
            var item = new TreeItem<>(attribute);
            root.getChildren().add(item);
            return;
        }

        var item = new TreeItem<>(
                new AttributeItem(node.getNodeName(), "", uom, codeSpace, false));
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
     * @param childList                    確認対象
     * @param deleteAttributeKeyName       削除対象の要素の名前
     * @param deleteAttributeParentKeyName 削除対象の親要素の名前（ツリービュー上）
     * @param parentIndex                  親要素のindex(ツリービュー上)
     * @param index                        要素のindex(ツリービュー上)
     */
    private void removeAttribute(ChildList<ADEComponent> childList, String deleteAttributeKeyName,
            String deleteAttributeParentKeyName, int parentIndex, int index) {
        if (deleteAttributeParentKeyName != null) {
            // 削除可能な対象かを確認
            if (!isDeletable(deleteAttributeKeyName, deleteAttributeParentKeyName)) {
                // アラートを作成
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("削除エラー");
                alert.setHeaderText(null);
                alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
                alert.getDialogPane().getStyleClass().add("alert");
                alert.setContentText("削除できない要素です。");

                // アラートを表示
                alert.showAndWait();
                return;
            }
        }
        for (int i = 0; i < childList.size(); i++) {
            if (parentIndex == -1) {
                childList.remove(index - 1);
                refreshListView();
                return;
            } else {
                var adeComponent = childList.get(parentIndex - 1);
                var adeElement = (ADEGenericElement) adeComponent;
                Node content = adeElement.getContent();
                Node parentNode = content.getChildNodes().item(0);
                Node removeNode = parentNode.getChildNodes().item(index);
                parentNode.removeChild(removeNode);
                refreshListView();
                return;
            }
        }
        refreshListView();
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

        // 確認対象の基準となる親要素を取得
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

    /**
     * showListView
     * 追加メニューの一覧に乗せるUro要素の一覧を表示し、追加を行う
     *
     * @param childList                選択中の地物の要素リスト
     * @param selectedAttributeKeyName 選択中のリストビューのアイテム名
     * @return メニューに表示させる要素リスト
     */
    private ArrayList<ArrayList<String>> showListView(ChildList<ADEComponent> childList,
            String selectedAttributeKeyName, int parentIndex, int selectedIndex) {
        AddingAttributeMenuController addingAttributeMenuController = null;
        Parent root = null;
        Stage pStage = new Stage();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/add-attribute-list-view.fxml"));
            root = loader.load();
            addingAttributeMenuController = loader.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<ArrayList<String>> addAttributeList = getUroList(selectedAttributeKeyName, false);
        if (addAttributeList.size() == 0) {
            // アラートを作成
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("追加エラー");
            alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("alert");
            alert.setHeaderText(null);
            alert.setContentText("追加できる要素がありません。");

            // アラートを表示
            alert.showAndWait();
            return null;
        }

        List<AttributeValue> attributeLists = new ArrayList<>();
        for (ArrayList<String> attribute : addAttributeList) {
            attributeLists.add(new AttributeValue(attribute.get(0), attribute.get(2)));
        }
        addingAttributeMenuController.setList(attributeLists);

        // // メニュー内の要素をダブルクリックで要素を追加
        addingAttributeMenuController.setItemSelectedCallback(selectedItem -> {
            String selectedItemName = selectedItem.getSelectedItem().nameProperty().getValue();
            int selectedItemIndex = selectedItem.getSelectedIndex();

            ArrayList<ArrayList<String>> requiredChildAttributeList = getUroList("uro:" + selectedItemName, true);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/add-attribute-form.fxml"));
                Parent formRoot = loader.load();
                InputAttributeFormController inputAttributeFormController = loader.getController();
                inputAttributeFormController.initialize(childList, selectedAttributeKeyName, "uro:" + selectedItemName,
                        addAttributeList.get(selectedItemIndex).get(1), addAttributeList, uroAttributeDocument,
                        requiredChildAttributeList, parentIndex, selectedIndex);

                Stage stage = new Stage();
                stage.setTitle("属性の追加");
                stage.setScene(new Scene(formRoot));
                // ボタンが押されたことを検知するコールバックを設定
                inputAttributeFormController.setOnAddButtonPressedCallback(() -> {
                    stage.close();
                    refreshListView();
                });
                // ウィンドウを表示
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

            pStage.close();
        });

        pStage.setScene(new Scene(root));
        pStage.show();
        return addAttributeList;
    }

    private void editAttribute(ChildList<ADEComponent> childList, String selectedItemName, String parentItemName,
            int parentIndex,
            int selectedIndex) {
        if (parentIndex == -1) {
            // アラートを作成
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("編集エラー");
            alert.setHeaderText(null);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("alert");
            alert.setContentText("編集可能な対象ではありません。");
            // アラートを表示
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/add-attribute-form.fxml"));
            Parent formRoot = loader.load();
            InputAttributeFormController inputAttributeFormController = loader.getController();
            inputAttributeFormController.initialize(childList, "uro:" + selectedItemName,
                    getType(selectedItemName, parentItemName), parentIndex, selectedIndex);
            // 新しいウィンドウ（ステージ）の設定
            Stage stage = new Stage();
            stage.setTitle("属性の編集");
            stage.setScene(new Scene(formRoot));
            // ボタンが押されたことを検知するコールバックを設定
            inputAttributeFormController.setOnAddButtonPressedCallback(() -> {
                stage.close();
                refreshListView();
                // 必要に応じて他の処理を実行
            });
            // ウィンドウを表示
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getUroList
     * 追加メニューの一覧に乗せるUro要素の一覧を返す
     *
     * @param targetName 地物情報のリスト
     * @param required   追加が必須となっている要素のみを抽出するかどうかを表すflag
     * @return メニューに表示させる要素リスト
     */
    private ArrayList<ArrayList<String>> getUroList(String targetName, boolean required) {
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
                    ArrayList<String> attributeSet = new ArrayList<String>();
                    attributeSet.add(targetElement.getAttribute("name"));
                    attributeSet.add(targetElement.getAttribute("type"));
                    attributeSet.add(targetElement.getAttribute("annotation"));
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
                            if (required) {
                                if (element.getAttribute("minOccurs") == ""
                                        || Integer.parseInt(element.getAttribute("minOccurs")) > 0) {
                                    attributeSet.add(element.getAttribute("name"));
                                    attributeSet.add(element.getAttribute("type"));
                                    attributeSet.add(element.getAttribute("annotation"));
                                    attributeList.add(attributeSet);
                                }
                            } else {
                                attributeSet.add(element.getAttribute("name"));
                                attributeSet.add(element.getAttribute("type"));
                                attributeSet.add(element.getAttribute("annotation"));
                                attributeList.add(attributeSet);
                            }
                        }
                    }
                }
            }
        }
        return attributeList;
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
     * @param node     親ノード
     * @param newNodes 追加したいノード
     */
    private void setNewNodeChildren(Node node, ArrayList<Node> newNodes) {
        for (int i = 0; i < newNodes.size(); i++) {
            node.appendChild(newNodes.get(i));
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
        System.out.println(
                indentString + "Node Name: " + ((Element) node).getAttribute("name") + ", Type: "
                        + ((Element) node).getAttribute("type")
                        + ", annotation: " + ((Element) node).getAttribute("annotation"));
        // 子ノードがある場合は再帰的に表示
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                printNode(children.item(i), indent + 1);
            }
        }
    }
}