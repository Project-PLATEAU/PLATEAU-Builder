package org.plateau.plateaubuilder.plateaubuilder.fxml.main;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.MeshView;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.citymodel.geometry.GeometryView;
import org.plateau.plateaubuilder.citymodel.geometry.ILODSolidView;
import org.plateau.plateaubuilder.io.mesh.converters.Gltf2LodConverter;
import org.plateau.plateaubuilder.io.mesh.converters.Obj2LodConverter;
import org.plateau.plateaubuilder.io.mesh.exporters.GltfExporter;
import org.plateau.plateaubuilder.io.mesh.exporters.ObjExporter;
import org.plateau.plateaubuilder.plateaubuilder.fxml.io.mesh.ThreeDimensionsExportDialogController;
import org.plateau.plateaubuilder.plateaubuilder.fxml.io.mesh.ThreeDimensionsImportDialogController;
import org.plateau.plateaubuilder.control.transform.AutoGeometryAligner;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.io.mesh.ThreeDimensionsModelEnum;
import org.plateau.plateaubuilder.world.World;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private boolean syncingHierarchy = false;

    private void constructTree() {
        var group = World.getActiveInstance().getCityModelGroup();
        if (group != null) {
            hierarchyTreeTable.rootProperty().set(new HierarchyController.TreeItemImpl(group));
        }
        for (var item : hierarchyTreeTable.getRoot().getChildren()) {
            item.setExpanded(true);
        }
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

        PLATEAUBuilderApp.getFeatureSellection().selectedFeaturesProperty().addListener((SetChangeListener<? super BuildingView>) change -> {
            Platform.runLater(() -> {
                if (syncingHierarchy)
                    return;

                syncingHierarchy = true;
                try {
                    var selectedFeatures = PLATEAUBuilderApp.getFeatureSellection().selectedFeaturesProperty().get();
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
                        .filter((item) -> item.valueProperty().get() instanceof BuildingView)
                        .map((item) -> (BuildingView) item.valueProperty().get()).toArray(BuildingView[]::new);
                var selection = PLATEAUBuilderApp.getFeatureSellection();
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
                    exportGltfMenu.setDisable(!(item instanceof BuildingView));
                    exportObjMenu.setDisable(!(item instanceof BuildingView));
                    importGltfMenu.setDisable(!(item instanceof BuildingView));
                    importObjMenu.setDisable(!(item instanceof BuildingView));
                }
            }
            if (t.getButton() == MouseButton.PRIMARY && t.getClickCount() == 2) {
                TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.valueProperty().get() instanceof BuildingView) {
                    var building = (BuildingView)selectedItem.valueProperty().get();
                    World.getActiveInstance().getCamera().focus(building.getLOD1Solid());
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

    private TreeItem<Node> findTreeItem(BuildingView activeFeature) {
        return findTreeItemRecursive(hierarchyTreeTable.getRoot(), activeFeature);
    }

    private TreeItem<Node> findTreeItemRecursive(TreeItem<Node> currentItem, BuildingView activeFeature) {
        Node currentFeature = currentItem.getValue();
        if (currentFeature instanceof BuildingView && currentFeature.equals(activeFeature)) {
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
        if (!(item instanceof BuildingView))
            return;

        BuildingView building = (BuildingView)item;
        try {
            var controller = ThreeDimensionsExportDialogController.create(building, ThreeDimensionsModelEnum.GLTF);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var solid = controller.getLodSolidView();
            var option = controller.getExportOption();
            new GltfExporter(solid, building.getId(), option).export(fileUrl);
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
        if (!(item instanceof BuildingView))
            return;

        BuildingView building = (BuildingView)item;
        try {
            var controller = ThreeDimensionsExportDialogController.create(building, ThreeDimensionsModelEnum.OBJ);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var solid = controller.getLodSolidView();
            var option = controller.getExportOption();
            new ObjExporter(solid, building.getId(), option).export(fileUrl);
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
        if (!(item instanceof BuildingView))
            return;

        BuildingView building = (BuildingView)item;
        CityModelView cityModelView = (CityModelView)building.getParent();
        try {
            ThreeDimensionsImportDialogController controller = ThreeDimensionsImportDialogController.create(building, ThreeDimensionsModelEnum.GLTF);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var lod = controller.getLod();
            var option = controller.getConvertOption();
            var convertedCityModel = new Gltf2LodConverter(cityModelView, building, lod, option).convert(fileUrl);

            if (!option.isUseGeoReference()) {
                // 位置合わせ
                var id = building.getId();
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
        if (!(item instanceof BuildingView))
            return;

        BuildingView building = (BuildingView)item;
        CityModelView cityModelView = (CityModelView)building.getParent();
        try {
            ThreeDimensionsImportDialogController controller = ThreeDimensionsImportDialogController.create(building, ThreeDimensionsModelEnum.OBJ);
            if (!controller.getDialogResult())
                return;

            var fileUrl = controller.getFileUrl();
            var lod = controller.getLod();
            var option = controller.getConvertOption();

            var convertedCityModel = new Obj2LodConverter(cityModelView, building, lod, option).convert(fileUrl);

            if (!option.isUseGeoReference()) {
                // 位置合わせ
                var id = building.getId();
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
        var selectedViews = PLATEAUBuilderApp.getFeatureSellection().selectedFeaturesProperty().get();
        selectedViews.forEach(view -> {
            view.setVisible(false);
        });
    }

    public void hideUnselectedViews(ActionEvent actionEvent) {
        var allViews = World.getActiveInstance().getCityModelGroup().getAllFeatures();
        var selectedViews = PLATEAUBuilderApp.getFeatureSellection().selectedFeaturesProperty().get();
        allViews.stream()
                .filter(view -> (view instanceof BuildingView) && !selectedViews.contains(view))
                .forEach(view -> view.setVisible(false));
    }

    public void showAllViews() {
        var allViews = World.getActiveInstance().getCityModelGroup().getAllFeatures();
        allViews.forEach(view -> view.setVisible(true));

    }

    private class TreeItemImpl extends TreeItem<Node> {

        public TreeItemImpl(Node node) {
            super(node);
            if (node instanceof Parent) {
                for (Node n : ((Parent) node).getChildrenUnmodifiable()) {
                    if (n instanceof MeshView || n instanceof ILODSolidView) {
                        if (!(n instanceof GeometryView))
                            continue;
                    }
                    getChildren().add(new HierarchyController.TreeItemImpl(n));
                }
            }
        }
    }
}
