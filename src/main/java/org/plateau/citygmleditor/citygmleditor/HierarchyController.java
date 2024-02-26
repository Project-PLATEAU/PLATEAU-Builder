package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.geometry.GeometryView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.converters.Gltf2LodConverter;
import org.plateau.citygmleditor.converters.Obj2LodConverter;
import org.plateau.citygmleditor.exporters.GltfExporter;
import org.plateau.citygmleditor.exporters.ObjExporter;
import org.plateau.citygmleditor.world.World;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
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

    private boolean syncingTreeTable = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hierarchyTreeTable.rootProperty().bind(new ObjectBinding<TreeItem<Node>>() {
            {
                bind(World.getActiveInstance().cityModelGroupProperty());
            }

            @Override
            protected TreeItem<Node> computeValue() {
                Node content3D = World.getActiveInstance().getCityModelGroup();
                if (content3D != null) {
                    return new HierarchyController.TreeItemImpl(content3D);
                } else {
                    return null;
                }
            }
        });

        CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty().addListener(observable -> {
            Platform.runLater(() -> {
                BuildingView activeFeature = CityGMLEditorApp.getFeatureSellection().getActive();
                if (activeFeature == null) {
                    if (hierarchyTreeTable.getSelectionModel().getSelectedItem() != null)
                        hierarchyTreeTable.getSelectionModel().clearSelection();
                    return;
                }

                TreeItem<Node> activeItem = findTreeItem(activeFeature);
                if (activeItem != null && activeItem != hierarchyTreeTable.getFocusModel().getFocusedItem()) {
                    expandTreeViewItem(activeItem);
                    hierarchyTreeTable.getSelectionModel().select(activeItem);
                    hierarchyTreeTable.scrollTo(hierarchyTreeTable.getRow(activeItem));
                }
            });
        });

        hierarchyTreeTable.getFocusModel().focusedCellProperty().addListener((obs, oldVal, newVal) -> {
            TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.valueProperty().get() instanceof BuildingView) {
                CityGMLEditorApp.getFeatureSellection().select((BuildingView) selectedItem.valueProperty().get());
            }
            if (selectedItem != null && selectedItem.valueProperty().get() instanceof ILODSolidView) {
                CityGMLEditorApp.getFeatureSellection().setSelectElement((Node)selectedItem.valueProperty().get());
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
