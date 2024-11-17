package org.plateaubuilder.gui.main;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.geometry.GeometryView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.transform.AutoGeometryAligner;
import org.plateaubuilder.core.io.mesh.FormatEnum;
import org.plateaubuilder.core.io.mesh.ThreeDimensionsModelEnum;
import org.plateaubuilder.core.io.mesh.converters.LODConverterBuilder;
import org.plateaubuilder.core.io.mesh.exporters.LODExporterBuilder;
import org.plateaubuilder.core.world.World;
import org.plateaubuilder.gui.io.mesh.ThreeDimensionsExportDialogController;
import org.plateaubuilder.gui.io.mesh.ThreeDimensionsImportDialogController;
import org.plateaubuilder.gui.search.SearchDialogController;

import javafx.application.Platform;
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
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.MeshView;

public class HierarchyController implements Initializable {
    public TreeTableView<Node> hierarchyTreeTable;
    public TreeTableColumn<Node, String> nodeColumn;
    public TreeTableColumn<Node, String> idColumn;
    public TreeTableColumn<Node, Boolean> visibilityColumn;
    public ContextMenu hierarchyContextMenu;
    public MenuItem exportGltfMenu;
    public MenuItem exportObjMenu;
    public MenuItem importGltfMenu;
    public MenuItem importObjMenu;
    public MenuItem hideSelectedViews;
    public MenuItem hideUnselectedViews;

    @FXML
    Button buttonShowSearchDialog;

    @FXML
    Button buttonClearFilter;

    private boolean syncingHierarchy = false;

    private SearchDialogController searchDialogController;

    private void constructTree() {
        var group = World.getActiveInstance().getCityModelGroup();
        if (group != null) {
            hierarchyTreeTable.rootProperty().set(new HierarchyController.TreeItemImpl(group));
        }
        for (var item : hierarchyTreeTable.getRoot().getChildren()) {
            item.setExpanded(true);
        }
        closeSearchDialog();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        World.getActiveInstance().cityModelGroupProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                return;

            constructTree();

            newValue.addChangeListener(this::constructTree);
        });

        hierarchyTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Editor.getFeatureSellection().selectedFeaturesProperty().addListener((SetChangeListener<? super IFeatureView>) change -> {
            Platform.runLater(() -> {
                if (syncingHierarchy)
                    return;

                syncingHierarchy = true;
                try {
                    var selectedFeatures = Editor.getFeatureSellection().selectedFeaturesProperty().get();
                    var selectionModel = hierarchyTreeTable.getSelectionModel();
                    selectionModel.clearSelection();
                    selectedFeatures.forEach(feature -> {
                        TreeItem<Node> item = findTreeItem(feature);
                        if (item != null) {
                            hierarchyTreeTable.requestFocus();
                            expandTreeViewItem(item);
                            selectionModel.select(item);
                            hierarchyTreeTable.scrollTo(hierarchyTreeTable.getRow(item));
                        }
                    });
                } finally {
                    syncingHierarchy = false;
                }
            });
        });

        hierarchyTreeTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<Node>>) change -> {
            if (syncingHierarchy)
                return;

            syncingHierarchy = true;
            try {
                var selectedItems = hierarchyTreeTable.getSelectionModel().getSelectedItems();
                var selectedFeatures = selectedItems.stream()
                        .filter((item) -> item.valueProperty().get() instanceof IFeatureView).map((item) -> (IFeatureView) item.valueProperty().get())
                        .toArray(IFeatureView[]::new);
                var selection = Editor.getFeatureSellection();
                selection.select(selectedFeatures);
            } finally {
                syncingHierarchy = false;
            }
        });

        hierarchyTreeTable.setOnMouseClicked(t -> {
            if (t.getButton() == MouseButton.SECONDARY) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    var item = selectedItem.valueProperty().get();
                    exportGltfMenu.setDisable(!(item instanceof IFeatureView));
                    exportObjMenu.setDisable(!(item instanceof IFeatureView));
                    importGltfMenu.setDisable(!(item instanceof IFeatureView));
                    importObjMenu.setDisable(!(item instanceof IFeatureView));
                }
            }
            if (t.getButton() == MouseButton.PRIMARY && t.getClickCount() == 2) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.valueProperty().get() instanceof IFeatureView) {
                    var feature = (IFeatureView) selectedItem.valueProperty().get();
                    World.getActiveInstance().getCamera().focus(feature.getLODView(1).getMeshView());
                }
                t.consume();
            }
        });

        hierarchyTreeTable.setOnKeyPressed(t -> {
            if (t.getCode() == KeyCode.SPACE) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    Node node = selectedItem.getValue();
                    node.setVisible(!node.isVisible());
                }
                t.consume();
            }
        });


        nodeColumn.setCellValueFactory(p -> p.getValue().valueProperty().asString());
        idColumn.setCellValueFactory(p -> p.getValue().getValue().idProperty());
        visibilityColumn.setCellValueFactory(p -> p.getValue().getValue().visibleProperty());
        visibilityColumn.setCellFactory(CheckBoxTreeTableCell.forTreeTableColumn(visibilityColumn));
    }

    private TreeItem<Node> findTreeItem(IFeatureView activeFeature) {
        return findTreeItemRecursive(hierarchyTreeTable.getRoot(), activeFeature);
    }

    private TreeItem<Node> findTreeItemRecursive(TreeItem<Node> currentItem, IFeatureView activeFeature) {
        Node currentFeature = currentItem.getValue();
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
        CityModelView cityModelView = (CityModelView) featureView.getParent();
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
        CityModelView cityModelView = (CityModelView) featureView.getParent();
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
