package org.plateau.citygmleditor.citygmleditor;

import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.MeshView;
import javafx.stage.FileChooser;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.citymodel.BuildingInstallationView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.converters.Gltf2LodConverter;
import org.plateau.citygmleditor.converters.Obj2LodConverter;
import org.plateau.citygmleditor.exporters.GltfExporter;
import org.plateau.citygmleditor.exporters.ObjExporter;
import org.plateau.citygmleditor.world.*;

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
            if (syncingTreeTable)
                return;

            syncingTreeTable = true;

            try {
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
            } finally {
                syncingTreeTable = false;
            }
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
                    exportGltfMenu.setDisable(!(item instanceof ILODSolidView));
                    exportObjMenu.setDisable(!(item instanceof ILODSolidView));
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
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("gLTF", "*.gltf", "*.glb")
        );
        chooser.setTitle("Export gLTF");
        File newFile = chooser.showSaveDialog(hierarchyTreeTable.getScene().getWindow());
        if (newFile == null)
            return;

        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        BuildingView building = (BuildingView)solid.getParent();
        try {
            new GltfExporter().export(newFile.toString(), solid, building.getId());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Export the selected solid to OBJ
     * @param actionEvent the event
     */
    public void exportObj(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("OBJ", "*.obj")
        );
        chooser.setTitle("Export OBJ");
        File newFile = chooser.showSaveDialog(hierarchyTreeTable.getScene().getWindow());
        if (newFile == null)
            return;

        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView solid = (ILODSolidView)item;
        BuildingView building = (BuildingView)solid.getParent();
        try {
            new ObjExporter().export(newFile.toString(), solid, building.getId());
        } catch (Exception ex) {
            Logger.getLogger(HierarchyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void importGltf(ActionEvent actionEvent) {
        var file = FileChooserService.showOpenDialog("*.gltf", SessionManager.GLTF_FILE_PATH_PROPERTY);

        if (file == null)
            return;

        var content = (Group)World.getActiveInstance().getCityModelGroup();
        var cityModelNode = content.getChildren().get(0);
        if (cityModelNode == null)
            return;

        var cityModelView = (CityModelView)cityModelNode;
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView lodSolidView = (ILODSolidView)item;
        try {
            var convertedCityModel = new Gltf2LodConverter(cityModelView, lodSolidView).convert(file.toString());
            var node = new Group();
            node.setId(content.getId());
            node.getChildren().add(convertedCityModel);

            //CityGMLEditorApp.getSceneContent().setContent(node);
            World.getActiveInstance().setCityModelGroup(node);

            // 位置合わせ
            var id = lodSolidView.getParent().getId();
            AutoGeometryAligner.GeometryAlign(convertedCityModel, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void importObj(ActionEvent actionEvent) {
        var file = FileChooserService.showOpenDialog("*.obj", SessionManager.OBJ_FILE_PATH_PROPERTY);

        if (file == null)
            return;

        var content = (Group)World.getActiveInstance().getCityModelGroup();
        var cityModelNode = content.getChildren().get(0);
        if (cityModelNode == null)
            return;

        var cityModelView = (CityModelView)cityModelNode;
        TreeItem<Node> selectedItem = hierarchyTreeTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null)
            return;

        var item = selectedItem.valueProperty().get();
        if (!(item instanceof ILODSolidView))
            return;

        ILODSolidView lodSolidView = (ILODSolidView)item;
        try {
            var convertedCityModel = new Obj2LodConverter(cityModelView, lodSolidView).convert(file.toString());
            var node = new Group();
            node.setId(content.getId());
            node.getChildren().add(convertedCityModel);

            World.getActiveInstance().setCityModelGroup(node);

            // 位置合わせ
            var id = lodSolidView.getParent().getId();
            AutoGeometryAligner.GeometryAlign(convertedCityModel, id);

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
                    if (n instanceof MeshView) {
                        if (!(n instanceof LOD1SolidView) && !(n instanceof BuildingInstallationView))
                            continue;
                    }
                    getChildren().add(new HierarchyController.TreeItemImpl(n));
                }
            }
        }
    }
}
