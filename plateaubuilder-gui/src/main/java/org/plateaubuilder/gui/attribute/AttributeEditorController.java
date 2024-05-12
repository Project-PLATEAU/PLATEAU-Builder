package org.plateaubuilder.gui.attribute;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.AttributeItem;
import org.plateaubuilder.core.citymodel.AttributeValue;
import org.plateaubuilder.core.citymodel.BuildingView;
import org.plateaubuilder.core.citymodel.MeasuredHeightHandler;
import org.plateaubuilder.core.citymodel.MeasuredHeightManager;
import org.plateaubuilder.core.citymodel.NodeAttributeHandler;
import org.plateaubuilder.core.citymodel.RootAttributeHandler;
import org.plateaubuilder.core.citymodel.XSDSchemaDocument;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.attribute.AttributeEditor;
import org.plateaubuilder.core.editor.attribute.BuildingSchema;
import org.plateaubuilder.core.editor.commands.AbstractCityGMLUndoableCommand;
import org.plateaubuilder.gui.utils.AlertController;
import org.plateaubuilder.validation.AttributeValidator;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

public class AttributeEditorController implements Initializable {
    public TreeTableView<AttributeItem> attributeTreeTable;
    public TreeTableColumn<AttributeItem, String> keyColumn;
    public TreeTableColumn<AttributeItem, String> valueColumn;
    private AbstractBuilding selectedBuilding;
    private XSDSchemaDocument uroSchemaDocument = Editor.getUroSchemaDocument();
    private ObjectProperty<BuildingView> activeFeatureProperty;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activeFeatureProperty = Editor.getFeatureSellection().getActiveFeatureProperty();

        // コンテキストメニュー
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addItem = new MenuItem("追加");
        MenuItem editItem = new MenuItem("編集");
        MenuItem deleteItem = new MenuItem("削除");

        // 追加ボタン押下時の挙動
        addItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedTreeItem;
            if (attributeTreeTable.getSelectionModel().getSelectedItem() != null) {
                selectedTreeItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            } else {
                selectedTreeItem = attributeTreeTable.getRoot();
            }
            showAddableAttributePanel(selectedTreeItem,
                    (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding());
        });

        // 編集ボタン押下時の挙動
        editItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedTreeItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            TreeItem<AttributeItem> parentTreeItem = attributeTreeTable.getSelectionModel().getSelectedItem()
                    .getParent();
            if (selectedTreeItem != null && parentTreeItem != null) {
                showEditAttributePanel(selectedTreeItem.getValue(), parentTreeItem.getValue());
            }
        });

        // 削除ボタン押下時の挙動
        deleteItem.setOnAction(event -> {
            TreeItem<AttributeItem> selectedTreeItem = attributeTreeTable.getSelectionModel().getSelectedItem();
            if (selectedTreeItem != null) {
                AttributeItem deleteAttributeItem = selectedTreeItem.getValue();
                AttributeItem parentAttributeItem = selectedTreeItem.getParent().getValue();

                if (!uroSchemaDocument.isDeletable(deleteAttributeItem.getName(), parentAttributeItem.getName(),
                        "uro")) {
                    AlertController.showDeleteAlert();
                } else {
                    Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {
                        private final BuildingView focusTarget = Editor.getFeatureSellection().getActive();
                        private final String codeSpaceCache = deleteAttributeItem.getCodeSpace();
                        private final String uomCache = deleteAttributeItem.getUom();
                        private final String valueCache = deleteAttributeItem.getValue();
                        private final ChildList<ADEComponent> bldgAttributeTreeCache = (ChildList<ADEComponent>) selectedBuilding
                                .getGenericApplicationPropertyOfAbstractBuilding();
                        private final AttributeItem parentAttributeItemCache = parentAttributeItem;
                        private final String deleteAttributeNameCache = deleteAttributeItem.getName();
                        private AttributeItem targetAttributeItem = deleteAttributeItem;

                        @Override
                        public void redo() {
                            AttributeEditor.removeAttribute(deleteAttributeNameCache,
                                    parentAttributeItemCache, targetAttributeItem, bldgAttributeTreeCache);
                        }

                        @Override
                        public void undo() {
                            targetAttributeItem = AttributeEditor.addAttribute(parentAttributeItemCache,
                                    deleteAttributeNameCache, valueCache,
                                    codeSpaceCache,
                                    uomCache, bldgAttributeTreeCache);
                        }

                        @Override
                        public javafx.scene.Node getUndoFocusTarget() {
                            return focusTarget;
                        }

                        @Override
                        public javafx.scene.Node getRedoFocusTarget() {
                            return focusTarget;
                        }
                    });
                }
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
                        String parentKey = parentItem != null ? parentItem.getValue().getName() : null;
                        if (!uroSchemaDocument.isDeletable(item.getName(), parentKey, "uro")) {
                            setStyle("-fx-background-color: grey;");
                        } else {
                            setStyle("");
                        }
                    }

                    // ツールチップを設定
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
                selectedFeature = activeFeatureProperty.get();

                if (selectedFeature == null)
                    return null;
                selectedBuilding = feature.getGML();
                var root = new TreeItem<>(
                        new AttributeItem(new RootAttributeHandler(selectedBuilding)));
                {
                    if (selectedBuilding.isSetMeasuredHeight()) {
                        MeasuredHeightManager.addMeasureHeightToTreeView(selectedBuilding, root);
                    }
                }
                {
                    addADEPropertyToTree(selectedFeature, root);
                }
                root.setExpanded(true);
                return root;
            }
        });

        attributeTreeTable.setShowRoot(false);

        keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        valueColumn.setOnEditCommit(event -> {
            TreeItem<AttributeItem> editedItem = event.getRowValue();
            AttributeItem attributeItem = editedItem.getValue();
            AttributeItem parentAttributeItem = editedItem.getParent().getValue();
            String newValue = event.getNewValue();
            String type = uroSchemaDocument.getType(attributeItem.getName(), parentAttributeItem.getName(), "uro");

            // バリデーションチェック
            if (AttributeValidator.checkValue(newValue, type)) {
                Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {
                    private final IFeatureView focusTarget = Editor.getFeatureSellection().getActive();
                    private final AttributeItem attributeItemCache = attributeItem;
                    private final String newValue = event.getNewValue();
                    private final String oldValue = event.getOldValue();

                    @Override
                    public void redo() {
                        attributeItemCache.setValue(newValue);
                    }

                    @Override
                    public void undo() {
                        attributeItemCache.setValue(oldValue);
                    }

                    @Override
                    public javafx.scene.Node getUndoFocusTarget() {
                        return focusTarget.getNode();
                    }

                    @Override
                    public javafx.scene.Node getRedoFocusTarget() {
                        return focusTarget.getNode();
                    }
                });
            } else {
                AlertController.showChangeAlert(type);
                attributeItem.setValue(event.getOldValue());
                attributeTreeTable.refresh();
            }
        });

    }

    private void addADEPropertyToTree(AbstractBuilding selectedBuilding, TreeItem<AttributeItem> root) {
        for (var adeComponent : selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding()) {
            var adeElement = (ADEGenericElement) adeComponent;
            addXMLElementToTree(adeElement.getContent(), null, root);
        }
    }

    private void addXMLElementToTree(Node node, Node parentNode, TreeItem<AttributeItem> root) {
        // 子が末尾の要素であるかチェック
        String nodeTagName = ((Element) node).getTagName().toLowerCase();
        String parentNodeTagName = "";
        String uom = ((Element) node).getAttribute("uom");
        String codeSpace = ((Element) node).getAttribute("codeSpace");
        String type = null;

        if (parentNode != null) {
            parentNodeTagName = ((Element) parentNode).getTagName().toLowerCase();
            type = uroSchemaDocument.getType(node.getNodeName(), parentNode.getNodeName(), "uro");
        } else {
            type = uroSchemaDocument.getType(node.getNodeName(), null, "uro");
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

    private static ADEComponent getRootADEComponent(ChildList<ADEComponent> adeComponents, Node node) {
        var parentNodes = new ArrayList<Node>();
        while (node.getParentNode() != null) {
            parentNodes.add(node);
            node = node.getParentNode();
        }
        for (var adeComponent : adeComponents) {
            var adeElement = (ADEGenericElement) adeComponent;
            Node content = adeElement.getContent();
            if (parentNodes.contains(content))
                return adeComponent;
        }
        return null;
    }

    public void onClickAddButton() {
        showRootAttributeAdditionPanel();
    }

    private void showRootAttributeAdditionPanel() {
        showAddableAttributePanel(attributeTreeTable.getRoot(),
                (ChildList<ADEComponent>) selectedBuilding.getGenericApplicationPropertyOfAbstractBuilding());
    }

    /**
     * 属性情報を編集するための画面を提示します。
     *
     * @param selectedAttributeItem 選択中のAttributeItem
     * @param parentAttributeItem   選択中のAttributeItemの親AttributeItem
     */
    private void showEditAttributePanel(AttributeItem selectedAttributeItem, AttributeItem parentAttributeItem) {
        // 対象が編集不可である場合はエラー表示
        if (!Editor.getUroSchemaDocument().isEditable(selectedAttributeItem, "uro")) {
            AlertController.showEditAlert();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    AttributeEditorController.class.getResource("attribute-input-form.fxml"));
            Parent formRoot = loader.load();
            AttributeInputFormController attributeInputFormController = loader.getController();
            var type = new BuildingSchema().getType(selectedAttributeItem.getName());
            attributeInputFormController.initialize(selectedAttributeItem, formRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 属性の追加を行うための画面を表示します
     *
     * @param selectedTreeItem  選択中のツリーアイテム
     * @param bldgAttributeTree 選択中の地物のツリー情報
     */
    private void showAddableAttributePanel(TreeItem<AttributeItem> selectedTreeItem,
            ChildList<ADEComponent> bldgAttributeTree) {
        AddableAttributeListPanelController addableAttributeMenuController = null;
        AttributeItem selectedAttributeItem = null;
        String keyAttributeName = null;

        if (selectedTreeItem != null) {
            selectedAttributeItem = selectedTreeItem.getValue();
            keyAttributeName = selectedAttributeItem.getName();
        }

        // 追加済みの子要素の名前を取得
        final ArrayList<String> treeViewChildItemList = extractChildAttributeNames(selectedTreeItem);
        ArrayList<ArrayList<String>> addableUroAttributeList = uroSchemaDocument.getElementList(keyAttributeName, false,
                treeViewChildItemList, "uro");
        addableUroAttributeList.addAll(getBuildingElementList());

        // 追加可能な属性がない場合はエラー表示
        if (addableUroAttributeList.size() == 0) {
            AlertController.showAddAlert();
            return;
        }
        List<AttributeValue> addAttributeList = new ArrayList<>();
        for (ArrayList<String> attribute : addableUroAttributeList) {
            addAttributeList.add(new AttributeValue(attribute.get(0), attribute.get(2)));
        }

        // 追加する属性の選択画面を表示
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addable-attribute-menu.fxml"));
            Parent root = loader.load();
            addableAttributeMenuController = loader.getController();
            addableAttributeMenuController.showAddableAttributePanel(addAttributeList, root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final AttributeItem finalSelectedAttributeItem = selectedAttributeItem;

        // 追加用メニュー内の属性がダブルクリックされたときに呼び出されるコールバック
        addableAttributeMenuController.setItemSelectedCallback(selectedItem -> {
            String addAttributeName = selectedItem.getSelectedItem().nameProperty().getValue();
            try {
                // 追加する属性の情報を入力するフォームを表示
                FXMLLoader loader = new FXMLLoader(getClass().getResource("attribute-input-form.fxml"));
                Parent formRoot = loader.load();
                AttributeInputFormController addAttributeFormController = loader.getController();

                // 追加ボタンが押されたことを検知するコールバックを設定
                addAttributeFormController.setOnAddButtonPressedCallback(() -> {
                    refreshListView();
                });
                // 情報入力用フォームのコントローラーを初期化
                addAttributeFormController.initialize(finalSelectedAttributeItem,
                        addAttributeName,
                        treeViewChildItemList,
                        bldgAttributeTree, formRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private ArrayList<String> extractChildAttributeNames(TreeItem<AttributeItem> selectedTreeItem) {
        ArrayList<String> childNameList = new ArrayList<>();
        if (selectedTreeItem != null) {
            for (TreeItem<AttributeItem> child : selectedTreeItem.getChildren()) {
                childNameList.add(child.getValue().getName());
            }
        }
        return childNameList;
    }

    private ArrayList<ArrayList<String>> getBuildingElementList() {
        ArrayList<ArrayList<String>> attributeList = new ArrayList<ArrayList<String>>();
        List<String> treeViewRootItemList = new ArrayList<String>();

        // 追加済みのルート要素の名前を取得
        for (TreeItem<AttributeItem> item : attributeTreeTable.getRoot().getChildren()) {
            treeViewRootItemList.add(item.getValue().getName());
        }

        var schema = new BuildingSchema();
        for (var element : schema.getAllElements()) {
            boolean exists = treeViewRootItemList.contains(element);
            if (!exists) {
                var item = new ArrayList<String>();
                item.add(element);
                item.add(schema.getType(element));
                item.add("");
                attributeList.add(item);
            }
        }
        return attributeList;
    }

    /**
     * リストビューを更新します
     */
    private void refreshListView() {
        BuildingView currentSelectedBuilding = activeFeatureProperty.get();
        activeFeatureProperty.set(null);
        activeFeatureProperty.set(currentSelectedBuilding);
    }
}
