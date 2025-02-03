package org.plateaubuilder.gui.main;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.geometry.GeometryView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.transform.AutoGeometryAligner;
import org.plateaubuilder.core.io.csv.exporters.CSVExporter;
import org.plateaubuilder.core.io.mesh.FormatEnum;
import org.plateaubuilder.core.io.mesh.ThreeDimensionsModelEnum;
import org.plateaubuilder.core.io.mesh.converters.LODConverterBuilder;
import org.plateaubuilder.core.io.mesh.exporters.LODExporterBuilder;
import org.plateaubuilder.core.world.World;
import org.plateaubuilder.gui.io.csv.CsvExportDialogController;
import org.plateaubuilder.gui.io.mesh.ThreeDimensionsExportDialogController;
import org.plateaubuilder.gui.io.mesh.ThreeDimensionsImportDialogController;
import org.plateaubuilder.gui.search.SearchDialogController;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.shape.MeshView;

public class HierarchyController implements Initializable {
    @FXML
    private TreeTableView<Node> hierarchyTreeTable;
    @FXML
    private TreeTableColumn<Node, String> nodeColumn;
    @FXML
    private TreeTableColumn<Node, Node> idColumn;
    @FXML
    private TreeTableColumn<Node, Boolean> visibilityColumn;
    @FXML
    private ContextMenu hierarchyContextMenu;
    @FXML
    private MenuItem exportCsvMenu;
    @FXML
    private MenuItem exportGltfMenu;
    @FXML
    private MenuItem exportObjMenu;
    @FXML
    private MenuItem importGltfMenu;
    @FXML
    private MenuItem importObjMenu;
    @FXML
    private MenuItem hideSelectedViews;
    @FXML
    private MenuItem hideUnselectedViews;

    @FXML
    private Button buttonShowSearchDialog;
    @FXML
    private Button buttonClearFilter;

    private boolean syncingHierarchy = false;
    private SearchDialogController searchDialogController;

    /**
     * 現在のCityModelGroupをもとにツリーを再構築します。
     * ルートの子要素はデフォルトで展開されます。
     */
    private void constructTree() {
        var group = World.getActiveInstance().getCityModelGroup();
        if (group != null) {
            hierarchyTreeTable.setRoot(new TreeItemImpl(group));
            // ルート直下の要素は全て展開
            for (var item : hierarchyTreeTable.getRoot().getChildren()) {
                item.setExpanded(true);
            }
        }
        // 検索ダイアログが開いている場合は閉じる
        closeSearchDialog();
    }

    /**
     * @brief 初期化処理。GUI要素の設定とイベントリスナを登録します。
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hierarchyTreeTable.setFocusTraversable(true);

        // CityModelGroupの変更を監視
        World.getActiveInstance().cityModelGroupProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            constructTree();
            // CityModelの変更があったら再構築
            newValue.addChangeListener(this::constructTree);
        });

        // 複数選択を許可
        hierarchyTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 3Dビューでの選択変更を監視し、ツリー上の選択状態に反映
        Editor.getFeatureSellection().selectedFeaturesProperty().addListener(
                (SetChangeListener<? super IFeatureView>) change -> {
                    Platform.runLater(() -> {
                        if (syncingHierarchy) {
                            return;
                        }
                        syncingHierarchy = true;

                        try {
                            var selectedFeatures = Editor.getFeatureSellection().selectedFeaturesProperty().get();
                            var selectionModel = hierarchyTreeTable.getSelectionModel();
                            selectionModel.clearSelection();
                            // 選択中の地物に対応するツリーアイテムを探して選択
                            selectedFeatures.forEach(feature -> {
                                TreeItem<Node> item = findTreeItem(feature);
                                if (item != null) {
                                    hierarchyTreeTable.requestFocus();
                                    expandTreeViewItem(item);
                                    selectionModel.select(item);

                                    // 画面外の場合はスクロール
                                    if (!isRowVisible(item))
                                        hierarchyTreeTable.scrollTo(hierarchyTreeTable.getRow(item));
                                }
                            });
                        } finally {
                            syncingHierarchy = false;
                        }
                    });
                });

        // ツリー上の選択アイテム変更を監視
        hierarchyTreeTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<Node>>) change -> {
            if (syncingHierarchy) {
                return;
            }
            syncingHierarchy = true;
            try {
                var selectedItems = hierarchyTreeTable.getSelectionModel().getSelectedItems();
                var selectedFeatures = selectedItems.stream()
                        .filter(item -> item.getValue() instanceof IFeatureView)
                        .map(item -> (IFeatureView) item.getValue())
                        .toArray(IFeatureView[]::new);
                Editor.getFeatureSellection().select(selectedFeatures);
            } finally {
                syncingHierarchy = false;
            }
        });

        // マウスクリック時の処理
        hierarchyTreeTable.setOnMouseClicked(event -> {
            // 右クリック時、コンテキストメニューの有効/無効を切り替え
            if (event.getButton() == MouseButton.SECONDARY) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    var item = selectedItem.getValue();
                    var isSingleSelection = hierarchyTreeTable.getSelectionModel().getSelectedItems().size() == 1;
                    exportCsvMenu.setDisable(!(item instanceof IFeatureView));
                    exportGltfMenu.setDisable(!(item instanceof IFeatureView) || !isSingleSelection);
                    exportObjMenu.setDisable(!(item instanceof IFeatureView) || !isSingleSelection);
                    importGltfMenu.setDisable(!(item instanceof IFeatureView) || !isSingleSelection);
                    importObjMenu.setDisable(!(item instanceof IFeatureView) || !isSingleSelection);
                }
            }
            // 左ダブルクリック時、カメラをその地物にフォーカス
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getValue() instanceof IFeatureView) {
                    var feature = (IFeatureView) selectedItem.getValue();
                    // カメラをLOD1のMeshViewに向ける
                    World.getActiveInstance().getCamera().focus(feature.getLODView(1).getMeshView());
                }
                event.consume();
            }
        });

        // スペースキーで表示/非表示を切り替え
        hierarchyTreeTable.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Node node = selectedItem.getValue();
                    // 表示状態を反転
                    node.setVisible(!node.isVisible());
                }
                event.consume();
            }
        });

        // カラム設定
        nodeColumn.setCellValueFactory(p -> p.getValue().valueProperty().asString());
        idColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue()));
        idColumn.setCellFactory(col -> new TreeTableCell<Node, Node>() {
            private final HBox container = new HBox(3); // 画像同士の間隔を3pxに

            @Override
            protected void updateItem(Node item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || !(item instanceof IFeatureView)) {
                    // セルが空の場合はグラフィックを表示しない
                    setGraphic(null);
                } else {
                    // いったん前の子ノードをクリア
                    container.getChildren().clear();

                    var isGayout = false;
                    var currentViewLod = Editor.getCityModelViewMode().lodProperty().get();
                    var featureView = (IFeatureView) item;
                    for (var i = 1; i <= 3; i++) {
                        var hasLod = featureView.getLODView(i) != null;
                        ImageView imageView = new ImageView(new Image(getClass()
                                .getResource(String.format("/org/plateaubuilder/gui/images/img_lod0%d_%s.png", i, hasLod ? "on" : "off")).toExternalForm()));
                        imageView.setFitWidth(24);
                        imageView.setFitHeight(24);
                        imageView.setPreserveRatio(true);
                        container.getChildren().add(imageView);
                        if (currentViewLod == i) {
                            isGayout = !hasLod;
                        }
                    }

                    setGraphic(container);
                    setText(featureView.getId());
                    setStyle(isGayout ? "-fx-text-fill: gray;" : "");
                }
            }
        });
        visibilityColumn.setCellValueFactory(p -> p.getValue().getValue().visibleProperty());
        visibilityColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(visibilityColumn));

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            hierarchyTreeTable.refresh();
        });
    }

    /**
     * 指定したIFeatureViewを保持するTreeItemを探します。
     * @param activeFeature 検索対象のIFeatureView
     * @return TreeItem<Node> 該当アイテム、未発見の場合null
     */
    private TreeItem<Node> findTreeItem(IFeatureView activeFeature) {
        return findTreeItemRecursive(hierarchyTreeTable.getRoot(), activeFeature);
    }

    /**
     * findTreeItem用の再帰ヘルパー関数。
     * @param currentItem 現在探索中のTreeItem
     * @param activeFeature 検索対象のIFeatureView
     * @return 見つかったTreeItem、またはnull
     */
    private TreeItem<Node> findTreeItemRecursive(TreeItem<Node> currentItem, IFeatureView activeFeature) {
        Node currentFeature = currentItem.getValue();
        // IFeatureView同士で比較
        if (currentFeature instanceof IFeatureView && currentFeature.equals(activeFeature)) {
            return currentItem;
        }
        for (TreeItem<Node> child : currentItem.getChildren()) {
            TreeItem<Node> found = findTreeItemRecursive(child, activeFeature);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * Export the selected solid to gLTF
     * @param actionEvent the event
     */
    public void exportGltf(ActionEvent actionEvent) {
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof IFeatureView))
            return;

        IFeatureView featureView = (IFeatureView) item;
        try {
            var controller = ThreeDimensionsExportDialogController.create(featureView, ThreeDimensionsModelEnum.GLTF);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var lodView = controller.getLodView();
            var option = controller.getExportOption();
            var exporter = new LODExporterBuilder().lodView(lodView).featureId(featureView.getId()).exportOption(option).format(FormatEnum.gLTF).build();
            exporter.export(fileUrl);
            java.awt.Desktop.getDesktop().open(new File(fileUrl).getParentFile());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Export the selected solid to Csv
     * @param actionEvent the event
     */
    public void exportCsv(ActionEvent actionEvent) {
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof IFeatureView))
            return;

        try {
            CsvExportDialogController controller = CsvExportDialogController.create(false);
            if (!controller.getDialogResult())
                return;
            String fileUrl = controller.getFileUrl();
            CSVExporter exporter = new CSVExporter(false);
            exporter.export(fileUrl);
            java.awt.Desktop.getDesktop().open(new File(fileUrl).getParentFile());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Export the selected solid to OBJ
     * @param actionEvent the event
     */
    public void exportObj(ActionEvent actionEvent) {
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof IFeatureView))
            return;

        IFeatureView featureView = (IFeatureView) item;
        try {
            var controller = ThreeDimensionsExportDialogController.create(featureView, ThreeDimensionsModelEnum.OBJ);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var lodView = controller.getLodView();
            var option = controller.getExportOption();
            var exporter = new LODExporterBuilder().lodView(lodView).featureId(featureView.getId()).exportOption(option).format(FormatEnum.OBJ).build();
            exporter.export(fileUrl);
            java.awt.Desktop.getDesktop().open(new File(fileUrl).getParentFile());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void importGltf(ActionEvent actionEvent) {
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof IFeatureView))
            return;

        IFeatureView featureView = (IFeatureView) item;
        CityModelView cityModelView = featureView.getCityModelView();
        try {
            ThreeDimensionsImportDialogController controller = ThreeDimensionsImportDialogController.create(featureView,
                    ThreeDimensionsModelEnum.GLTF);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var lod = controller.getLod();
            var option = controller.getConvertOption();
            var converter = new LODConverterBuilder().cityModelView(cityModelView).featureView(featureView).lod(lod).convertOption(option)
                    .format(FormatEnum.gLTF).build();
            var convertedCityModel = converter.convert(fileUrl);

            if (!option.isUseGeoReference()) {
                // 位置合わせ
                var id = featureView.getId();
                if (lod > 0)
                    AutoGeometryAligner.GeometryAlign(convertedCityModel, id, lod);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importObj(ActionEvent actionEvent) {
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof IFeatureView))
            return;

        IFeatureView featureView = (IFeatureView) item;
        CityModelView cityModelView = (CityModelView) featureView.getCityModelView();
        try {
            ThreeDimensionsImportDialogController controller = ThreeDimensionsImportDialogController.create(featureView,
                    ThreeDimensionsModelEnum.OBJ);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var lod = controller.getLod();
            var option = controller.getConvertOption();
            var converter = new LODConverterBuilder().cityModelView(cityModelView).featureView(featureView).lod(lod).convertOption(option)
                    .format(FormatEnum.OBJ).build();
            var convertedCityModel = converter.convert(fileUrl);

            if (!option.isUseGeoReference()) {
                // 位置合わせ
                var id = featureView.getId();
                if (lod > 0)
                    AutoGeometryAligner.GeometryAlign(convertedCityModel, id, lod);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void expandTreeViewItem(TreeItem<?> item) {
        if (item != null && item.getParent() != null) {
            item.getParent().setExpanded(true);
            expandTreeViewItem(item.getParent());
        }
    }

    public void hideSelectedViews(ActionEvent actionEvent) {
        var selectedViews = Editor.getFeatureSellection().selectedFeaturesProperty().get();
        selectedViews.forEach(view -> {
            view.setVisible(false);
        });
    }

    public void hideUnselectedViews(ActionEvent actionEvent) {
        var allViews = World.getActiveInstance().getCityModelGroup().getAllFeatures();
        var selectedViews = Editor.getFeatureSellection().selectedFeaturesProperty().get();
        allViews.stream()
                .filter(view -> (view instanceof IFeatureView) && !selectedViews.contains(view))
                .forEach(view -> view.setVisible(false));
    }

    public void showAllViews() {
        var allViews = World.getActiveInstance().getCityModelGroup().getAllFeatures();
        allViews.forEach(view -> view.setVisible(true));

    }

    public void showSearchDialog() {
        var group = World.getActiveInstance().getCityModelGroup();
        if (group == null) {
            return;
        }
        var allViews = group.getAllFeatures();
        if (allViews == null || allViews.isEmpty()) {
            return;
        }

        var featureList = new ArrayList<IFeatureView>();
        allViews.forEach(view -> {
            if (view instanceof IFeatureView) {
                featureList.add((IFeatureView) view);
            }
        });
        var typeList = featureList.stream().map(f -> f.getFeatureType()).distinct().sorted().collect(Collectors.toList());

        if (searchDialogController == null) {
            var controller = SearchDialogController.createSearchDialog(typeList, featureList);
            controller.setOnCloseAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent e) {
                    try {
                        if (!controller.getDialogResult()) {
                            return;
                        }
                        var filter = controller.getFilter();
                        if (filter == null) {
                            return;
                        }

                        featureList.forEach(view -> {
                            var filtered = !filter.evaluate(view);
                            view.setFiltered(filtered);
                            view.setVisible(true);
                            if (filtered) {
                                view.setVisible(false);
                            }
                        });
                        hierarchyTreeTable.rootProperty().set(new HierarchyController.FilterTreeItemImpl(group));
                        buttonClearFilter.setDisable(false);
                    } finally {
                        searchDialogController = null;
                    }
                }
            });
            searchDialogController = controller;
        }

        searchDialogController.show();
    }

    public void clearFilter() {
        var group = World.getActiveInstance().getCityModelGroup();
        if (group != null) {
            hierarchyTreeTable.rootProperty().set(new HierarchyController.TreeItemImpl(group));
        }
        for (var item : hierarchyTreeTable.getRoot().getChildren()) {
            item.setExpanded(true);
        }

        buttonClearFilter.setDisable(true);

        var allViews = group.getAllFeatures();
        if (allViews == null || allViews.isEmpty()) {
            return;
        }
        allViews.forEach(view -> {
            if (view instanceof IFeatureView) {
                ((IFeatureView) view).setVisible(true);
            }
        });
    }

    public void closeSearchDialog() {
        if (searchDialogController != null) {
            searchDialogController.close();
        }
    }

    /**
     * 指定した行番号が現在の可視領域に含まれているかを返す。
     * 非公開API(VirtualFlow)を利用するため、将来のJavaFXで動作が変わる可能性がある点に注意。
     */
    private boolean isRowVisible(TreeItem<Node> item) {
        int rowIndex = hierarchyTreeTable.getRow(item);
        if (rowIndex < 0) return false; // アイテムが見つからない場合

        if (!(hierarchyTreeTable.getSkin() instanceof TreeTableViewSkin)) {
            return false;
        }
        TreeTableViewSkin<?> skin = (TreeTableViewSkin<?>) hierarchyTreeTable.getSkin();

        // Skin内部の子要素(VirtualFlow)を取得
        var flows = skin.getChildren().stream().filter(node -> node instanceof VirtualFlow).findFirst();
        if (flows.isEmpty()) {
            return false;
        }
        var flow = (VirtualFlow<?>)flows.get();

        // 現在の先頭セルと末尾セルを取得
        var firstCell = flow.getFirstVisibleCell();
        var lastCell = flow.getLastVisibleCell();
        if (firstCell == null || lastCell == null) {
            return false;
        }
        int firstIndex = firstCell.getIndex();
        int lastIndex = lastCell.getIndex();
        // rowIndex が可視範囲に入っていれば true
        return (rowIndex >= firstIndex && rowIndex <= lastIndex);
    }

    private class TreeItemImpl extends TreeItem<Node> {

        public TreeItemImpl(Node node) {
            super(node);
            if (node instanceof Parent) {
                for (Node n : ((Parent) node).getChildrenUnmodifiable()) {
                    if (n instanceof MeshView || n instanceof ILODView) {
                        if (!(n instanceof GeometryView))
                            continue;
                    }
                    getChildren().add(new HierarchyController.TreeItemImpl(n));
                }
            }
        }
    }

    private class FilterTreeItemImpl extends TreeItem<Node> {

        public FilterTreeItemImpl(Node node) {
            super(node);
            if (node instanceof Parent) {
                for (Node n : ((Parent) node).getChildrenUnmodifiable()) {
                    if (!(n instanceof CityModelView)) {
                        continue;
                    }
                    for (Node g : ((Parent) n).getChildrenUnmodifiable()) {
                        if (!(g instanceof IFeatureView)) {
                            continue;
                        }
                        if (((IFeatureView) g).isFiltered()) {
                            continue;
                        }
                        getChildren().add(new HierarchyController.FilterTreeItemImpl(g));
                    }
                }
            }
        }
    }
}
