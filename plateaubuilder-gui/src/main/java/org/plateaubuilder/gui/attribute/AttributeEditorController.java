package org.plateaubuilder.gui.attribute;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.AttributeValue;
import org.plateaubuilder.core.citymodel.attribute.CommonAttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManagerFactory;
import org.plateaubuilder.core.citymodel.attribute.reader.XSDSchemaDocument;
import org.plateaubuilder.core.citymodel.attribute.wrapper.RootAttributeHandler;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.attribute.AttributeEditor;
import org.plateaubuilder.core.editor.attribute.AttributeTreeBuilder;
import org.plateaubuilder.core.editor.attribute.BuildingSchema;
import org.plateaubuilder.core.editor.commands.AbstractCityGMLUndoableCommand;
import org.plateaubuilder.core.editor.commands.ChangeAttributeValueCommand;
import org.plateaubuilder.gui.utils.AlertController;
import org.plateaubuilder.validation.AttributeValidator;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
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
    private IFeatureView selectedFeature;
    private XSDSchemaDocument uroSchemaDocument = Editor.getUroSchemaDocument();
    private ObjectProperty<IFeatureView> activeFeatureProperty;
    private AttributeTreeBuilder attributeTreeBuilder = new AttributeTreeBuilder();
    private AttributeSchemaManager attributeSchemaManager;

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

            showAddableAttributePanel(selectedTreeItem, getADEComponents());
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
                    // 複数地物選択時の処理を追加
                    if (deleteAttributeItem instanceof CommonAttributeItem) {
                        CommonAttributeItem commonItem = (CommonAttributeItem) deleteAttributeItem;
                        final Set<IFeatureView> originalSelectedFeatures = new HashSet<>(
                                Editor.getFeatureSellection().getSelectedFeatures());
                        Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {
                            private final Map<IFeatureView, String> oldValues = new HashMap<>();
                            private final Map<IFeatureView, String> oldCodeSpaces = new HashMap<>();
                            private final Map<IFeatureView, String> oldUoms = new HashMap<>();
                            private final Map<IFeatureView, ChildList<ADEComponent>> adeComponentsMap = new HashMap<>();
                            private final Map<IFeatureView, AttributeItem> parentAttributeCache = new HashMap<>();
                            private final IFeatureView focusTarget = Editor.getFeatureSellection().getActive();
                            private final String deleteAttributeNameCache = deleteAttributeItem.getName();

                            {
                                // 各地物の現在の状態を保存
                                for (IFeatureView feature : commonItem.getRelatedFeatures()) {
                                    AttributeItem attr = commonItem.getAttributeForFeature(feature);
                                    oldValues.put(feature, attr.getValue());
                                    oldCodeSpaces.put(feature, attr.getCodeSpace());
                                    oldUoms.put(feature, attr.getUom());
                                    adeComponentsMap.put(feature, AttributeTreeBuilder.getADEComponents(feature));
                                    parentAttributeCache.put(feature, attr);
                                }
                            }

                            @Override
                            public void redo() {
                                for (IFeatureView feature : commonItem.getRelatedFeatures()) {
                                    AttributeItem attr = commonItem.getAttributeForFeature(feature);
                                    AttributeEditor.removeAttribute(deleteAttributeNameCache,
                                            parentAttributeCache.get(feature), attr, adeComponentsMap.get(feature));
                                }
                                Platform.runLater(() -> {
                                    Editor.getFeatureSellection().clear();
                                    for (IFeatureView feature : originalSelectedFeatures) {
                                        Editor.getFeatureSellection().addSelection(feature);
                                    }
                                    attributeTreeTable.refresh();
                                });
                            }

                            @Override
                            public void undo() {
                                for (IFeatureView feature : commonItem.getRelatedFeatures()) {
                                    AttributeEditor.addAttribute(parentAttributeCache.get(feature),
                                            deleteAttributeNameCache,
                                            oldValues.get(feature),
                                            oldCodeSpaces.get(feature),
                                            oldUoms.get(feature),
                                            adeComponentsMap.get(feature),
                                            feature);
                                }
                                Platform.runLater(() -> {
                                    Editor.getFeatureSellection().clear();
                                    for (IFeatureView feature : originalSelectedFeatures) {
                                        Editor.getFeatureSellection().addSelection(feature);
                                    }
                                    attributeTreeTable.refresh();
                                });
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
                        // 選択された地物から取得
                        Set<IFeatureView> selectedFeatures = Editor.getFeatureSellection().getSelectedFeatures();
                        IFeatureView feature = selectedFeatures.iterator().next();
                        ChildList<ADEComponent> adeComponents = AttributeTreeBuilder.getADEComponents(feature);

                        Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {
                            private final javafx.scene.Node focusTarget = Editor.getFeatureSellection().getActive()
                                    .getNode();
                            private final String codeSpaceCache = deleteAttributeItem.getCodeSpace();
                            private final String uomCache = deleteAttributeItem.getUom();
                            private final String valueCache = deleteAttributeItem.getValue();
                            private final ChildList<ADEComponent> bldgAttributeTreeCache = adeComponents;
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
                                        uomCache, bldgAttributeTreeCache, feature);
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
            }
        });

        contextMenu.getItems().addAll(addItem, editItem, deleteItem);

        // TreeViewの各行に対する処理
        attributeTreeTable.setRowFactory(treeView -> {
            TreeTableRow<AttributeItem> row = new TreeTableRow<>();
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
                // selectedFeaturesPropertyもバインド
                bind(Editor.getFeatureSellection().selectedFeaturesProperty());
            }

            @Override
            protected TreeItem<AttributeItem> computeValue() {
                Set<IFeatureView> selectedFeatures = Editor.getFeatureSellection().getSelectedFeatures();

                if (selectedFeatures.isEmpty()) {
                    return null;
                }

                TreeItem<AttributeItem> root;
                if (selectedFeatures.size() > 1) {
                    // 複数地物選択時は CommonAttributeItem を使用
                    IFeatureView firstFeature = selectedFeatures.iterator().next();
                    AttributeItem baseRoot = new AttributeItem(new RootAttributeHandler(firstFeature));
                    CommonAttributeItem commonRoot = new CommonAttributeItem(baseRoot, firstFeature);

                    // 他の地物のrootを関連付け
                    for (IFeatureView feature : selectedFeatures) {
                        if (feature != firstFeature) {
                            AttributeItem featureRoot = new AttributeItem(new RootAttributeHandler(feature));
                            commonRoot.addRelatedAttribute(feature, featureRoot);
                        }
                    }

                    root = new TreeItem<>(commonRoot);
                    AttributeTreeBuilder.commonAttributesToTree(selectedFeatures, root);
                } else {
                    // 単一地物選択時は従来通り
                    root = new TreeItem<>(
                            new AttributeItem(new RootAttributeHandler(selectedFeatures.iterator().next())));
                    AttributeTreeBuilder.attributeToTree(selectedFeatures.iterator().next(), root);
                }

                // root.setExpanded(true);
                return root;
            }
        });

        attributeTreeTable.setShowRoot(false);

        keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("name"));
        valueColumn.setCellValueFactory(param -> {
            AttributeItem item = param.getValue().getValue();
            return new SimpleStringProperty(item.getValue());
        });
        valueColumn.setCellFactory(column -> new TextFieldTreeTableCell<AttributeItem, String>(
                new javafx.util.converter.DefaultStringConverter()) {
            private TextField textField;
            private String latestValue;

            @Override
            public void startEdit() {
                super.startEdit();
                textField = (TextField) getGraphic();
                if (textField != null) {
                    latestValue = textField.getText();
                    // 入力内容の変化を常に最新値として保持
                    textField.textProperty().addListener((obs, oldText, newText) -> {
                        latestValue = newText;
                    });
                    textField.setOnAction(e -> commitEdit(latestValue));
                    // フォーカスが失われたときにも即座にコミット
                    textField.focusedProperty().addListener((obs, wasFocused, nowFocused) -> {
                        if (!nowFocused) {
                            commitEdit(latestValue);
                        }
                    });
                }
            }

            @Override
            public void commitEdit(String newValue) {
                AttributeItem item = getTreeTableRow().getItem();
                if (item == null) {
                    return;
                }
                String type = item.getType();

                // バリデーションチェックを実施
                if (AttributeValidator.checkValue(newValue, type)) {
                    String oldValue = item.getValue();
                    if (!oldValue.equals(newValue)) {
                        // 複数地物選択時の処理を追加
                        if (item instanceof CommonAttributeItem) {
                            CommonAttributeItem commonItem = (CommonAttributeItem) item;
                            final Set<IFeatureView> originalSelectedFeatures = new HashSet<>(
                                    Editor.getFeatureSellection().getSelectedFeatures());
                            Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {
                                private final Map<IFeatureView, String> oldValues = new HashMap<>();
                                private final Map<IFeatureView, String> oldCodeSpaces = new HashMap<>();
                                private final Map<IFeatureView, String> oldUoms = new HashMap<>();
                                private final IFeatureView focusTarget = Editor.getFeatureSellection().getActive();

                                {
                                    // 各地物の現在の状態を保存
                                    for (IFeatureView feature : commonItem.getRelatedFeatures()) {
                                        oldValues.put(feature, commonItem.getValueForFeature(feature));
                                        oldCodeSpaces.put(feature, commonItem.getCodeSpaceForFeature(feature));
                                        oldUoms.put(feature, commonItem.getUomForFeature(feature));
                                    }
                                }

                                @Override
                                public void redo() {
                                    commonItem.setValue(newValue);
                                    Platform.runLater(() -> {
                                        // 選択状態を維持
                                        Editor.getFeatureSellection().clear();
                                        for (IFeatureView feature : originalSelectedFeatures) {
                                            Editor.getFeatureSellection().addSelection(feature);
                                        }
                                        attributeTreeTable.refresh();
                                    });
                                }

                                @Override
                                public void undo() {
                                    // 各地物の値を元に戻す
                                    for (IFeatureView feature : commonItem.getRelatedFeatures()) {
                                        AttributeItem attr = commonItem.getAttributeForFeature(feature);
                                        if (attr != null) {
                                            attr.setValue(oldValues.get(feature));
                                            attr.setCodeSpace(oldCodeSpaces.get(feature));
                                            attr.setUom(oldUoms.get(feature));
                                        }
                                    }
                                    Platform.runLater(() -> {
                                        Editor.getFeatureSellection().clear();
                                        for (IFeatureView feature : originalSelectedFeatures) {
                                            Editor.getFeatureSellection().addSelection(feature);
                                        }
                                        attributeTreeTable.refresh();
                                    });
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
                            // 単一地物選択時の既存の処理
                            ChangeAttributeValueCommand command = new ChangeAttributeValueCommand(
                                    item,
                                    oldValue,
                                    newValue,
                                    Editor.getFeatureSellection().getActive().getNode());
                            command.redo();
                            Editor.getUndoManager().addCommand(command);
                        }
                    }
                    updateItem(newValue, false);
                } else {
                    AlertController.showValueAlert(type, null);
                    updateItem(item.getValue(), false);
                }
                attributeTreeTable.refresh();
            }
        });
    }

    public void onClickAddButton() {
        showRootAttributeAdditionPanel();
    }

    private void showRootAttributeAdditionPanel() {
        showAddableAttributePanel(attributeTreeTable.getRoot(), getADEComponents());
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

        // 選択地物の取得を先頭に移動
        Set<IFeatureView> selectedFeatures = Editor.getFeatureSellection().getSelectedFeatures();
        if (selectedFeatures.isEmpty()) {
            return;
        }

        // attributeSchemaManagerの初期化
        attributeSchemaManager = AttributeSchemaManagerFactory
                .getSchemaManager(selectedFeatures.iterator().next().getGML());

        AddableAttributeListPanelController addableAttributeMenuController = null;
        AttributeItem selectedAttributeItem = null;
        String keyAttributeName = null;

        if (selectedTreeItem != null) {
            selectedAttributeItem = selectedTreeItem.getValue();
            keyAttributeName = selectedAttributeItem.getName();
        }

        // 追加済みの子要素の名前を取得
        final ArrayList<String> treeViewChildItemList = extractChildAttributeNames(selectedTreeItem);

        List<AttributeValue> addAttributeList = new ArrayList<>();

        if (selectedFeatures.size() > 1) {
            // 複数選択時の処理
            addAttributeList = getCommonAddableAttributes(selectedFeatures, keyAttributeName, treeViewChildItemList);
        } else {
            ArrayList<ArrayList<String>> addableUroAttributeList = new ArrayList<>();
            ArrayList<String> addableBldgAttributeList = new ArrayList<>();
            // 属性の種類に応じて追加可能な属性リストを取得
            if (keyAttributeName != null && keyAttributeName.startsWith("uro:")) {
                // uro属性の場合は、uro属性のみ取得
                addableUroAttributeList = uroSchemaDocument.getElementList(
                        keyAttributeName, false, treeViewChildItemList, "uro");
            } else if (keyAttributeName.matches("root")) {
                // ルート要素の場合は、uro属性と地物属性の両方を取得
                addableUroAttributeList = uroSchemaDocument.getElementList(
                        keyAttributeName, false, treeViewChildItemList, "uro");
                addableBldgAttributeList = attributeSchemaManager.getAttributeNameList(
                        keyAttributeName, treeViewChildItemList);
            } else {
                // その他の場合は、地物属性のみ取得
                addableBldgAttributeList = attributeSchemaManager.getAttributeNameList(
                        keyAttributeName, treeViewChildItemList);
            }

            // 属性値リストの作成
            for (ArrayList<String> attribute : addableUroAttributeList) {
                addAttributeList.add(new AttributeValue(attribute.get(0), attribute.get(2)));
            }
            for (String attribute : addableBldgAttributeList) {
                addAttributeList.add(new AttributeValue(attribute, ""));
            }
        }
        // 追加可能な属性がない場合はエラー表示
        if (addAttributeList.isEmpty()) {
            AlertController.showAddAlert();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addable-attribute-menu.fxml"));
            Parent root = loader.load();
            addableAttributeMenuController = loader.getController();
            addableAttributeMenuController.showAddableAttributePanel(addAttributeList, root);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final AttributeItem finalSelectedAttributeItem = selectedAttributeItem;

        // 追加用メニュー内の属性がダブルクリックされたときのコールバック
        addableAttributeMenuController.setItemSelectedCallback(selectedItem -> {
            String addAttributeName = selectedItem.getSelectedItem().nameProperty().getValue();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("attribute-input-form.fxml"));
                Parent formRoot = loader.load();
                AttributeInputFormController attributeInputFormController = loader.getController();

                // 追加ボタンが押されたことを検知するコールバック
                attributeInputFormController.setOnAddButtonPressedCallback(() -> {
                    refreshListView();
                });

                // フォームの初期化時に選択地物の情報も渡す
                attributeInputFormController.initialize(finalSelectedAttributeItem,
                        addAttributeName,
                        treeViewChildItemList,
                        bldgAttributeTree,
                        formRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 複数の地物で共通して追加可能な属性リストを取得します
     */
    private List<AttributeValue> getCommonAddableAttributes(Set<IFeatureView> selectedFeatures,
            String keyAttributeName, ArrayList<String> treeViewChildItemList) {

        Map<String, Integer> attributeCount = new HashMap<>();
        Map<String, String> attributeTypeMap = new HashMap<>(); // 属性名とその型の対応を保存
        int featureCount = selectedFeatures.size();

        // 各地物について追加可能な属性をカウント
        for (IFeatureView feature : selectedFeatures) {
            ArrayList<ArrayList<String>> addableUroAttributeList = new ArrayList<>();
            ArrayList<String> addableBldgAttributeList = new ArrayList<>();

            // 属性の種類に応じて追加可能な属性リストを取得
            if (keyAttributeName != null && keyAttributeName.startsWith("uro:")) {
                // uro属性の場合は、uro属性のみ取得
                addableUroAttributeList = uroSchemaDocument.getElementList(
                        keyAttributeName, false, treeViewChildItemList, "uro");
            } else if (keyAttributeName.matches("root")) {
                // ルート要素の場合は、uro属性と地物属性の両方を取得
                addableUroAttributeList = uroSchemaDocument.getElementList(
                        keyAttributeName, false, treeViewChildItemList, "uro");
                addableBldgAttributeList = attributeSchemaManager.getAttributeNameList(
                        keyAttributeName, treeViewChildItemList);
            } else {
                // その他の場合は、地物属性のみ取得
                addableBldgAttributeList = attributeSchemaManager.getAttributeNameList(
                        keyAttributeName, treeViewChildItemList);
            }

            // uro属性のカウントと型情報の保存
            for (ArrayList<String> attr : addableUroAttributeList) {
                String attrName = attr.get(0);
                attributeCount.merge(attrName, 1, Integer::sum);
                if (!attributeTypeMap.containsKey(attrName)) {
                    attributeTypeMap.put(attrName, attr.get(2)); // 型情報を保存
                }
            }

            // 地物属性のカウント
            for (String attrName : addableBldgAttributeList) {
                attributeCount.merge(attrName, 1, Integer::sum);
            }
        }

        // 全ての地物で追加可能な属性のみを抽出
        List<AttributeValue> commonAttributes = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : attributeCount.entrySet()) {
            if (entry.getValue() == featureCount) {
                String attrName = entry.getKey();
                String type = attributeTypeMap.getOrDefault(attrName, "");
                commonAttributes.add(new AttributeValue(attrName, type));
            }
        }

        return commonAttributes;
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

    private ChildList<ADEComponent> getADEComponents() {
        Set<IFeatureView> selectedFeatures = Editor.getFeatureSellection().getSelectedFeatures();
        if (selectedFeatures.isEmpty()) {
            return null;
        }
        return AttributeTreeBuilder.getADEComponents(selectedFeatures.iterator().next());
    }

    private static ChildList<ADEComponent> getADEComponents(IFeatureView selectedFeature) {
        // nullチェックを追加
        if (selectedFeature == null) {
            return null;
        }
        return (ChildList<ADEComponent>) selectedFeature.getADEComponents();
    }

    /**
     * リストビューを更新します
     */
    private void refreshListView() {
        IFeatureView currentSelectedFeature = (IFeatureView) activeFeatureProperty.get();
        activeFeatureProperty.set(null);
        activeFeatureProperty.set(currentSelectedFeature);
    }
}