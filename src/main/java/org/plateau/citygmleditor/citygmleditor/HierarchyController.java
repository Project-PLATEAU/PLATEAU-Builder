package org.plateau.citygmleditor.citygmleditor;

import java.util.List;
import java.util.ArrayList;
import javafx.beans.binding.ObjectBinding;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;
import org.plateau.citygmleditor.citymodel.BuildingInstallationView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.converters.Obj2LodConverter;
import org.plateau.citygmleditor.exporters.GltfExporter;
import org.plateau.citygmleditor.exporters.ObjExporter;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.*;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HierarchyController implements Initializable {
    private final SceneContent sceneContent = CityGMLEditorApp.getSceneContent();
    public TreeTableView<Node> hierarchyTreeTable;
    public TreeTableColumn<Node, String> nodeColumn;
    public TreeTableColumn<Node, String> idColumn;
    public TreeTableColumn<Node, Boolean> visibilityColumn;
    public ContextMenu hierarchyContextMenu;
    public MenuItem exportGltfMenu;
    public MenuItem exportObjMenu;
    public MenuItem importObjMenu;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        hierarchyTreeTable.rootProperty().bind(new ObjectBinding<TreeItem<Node>>() {
            {
                bind(sceneContent.contentProperty());
            }

            @Override
            protected TreeItem<Node> computeValue() {
                Node content3D = sceneContent.getContent();
                if (content3D != null) {
                    return new HierarchyController.TreeItemImpl(content3D);
                } else {
                    return null;
                }
            }
        });

        CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty().addListener(observable -> {
            BuildingView activeFeature = CityGMLEditorApp.getFeatureSellection().getActive();
            if (activeFeature == null)
                return;

            TreeItem<Node> activeItem = findTreeItem(activeFeature);
            if (activeItem != null && activeItem != hierarchyTreeTable.getFocusModel().getFocusedItem()) {
                hierarchyTreeTable.getSelectionModel().select(activeItem);
                hierarchyTreeTable.scrollTo(hierarchyTreeTable.getRow(activeItem));
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
                // TODO: フォーカス
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

    public void importObj(ActionEvent actionEvent) {
        var file = FileChooserService.showOpenDialog("*.obj", SessionManager.OBJ_FILE_PATH_PROPERTY);

        if (file == null)
            return;

        var content = (Group)sceneContent.getContent();
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

            CityGMLEditorApp.getSceneContent().setContent(node);

            // 位置合わせ
            var id = lodSolidView.getParent().getId();
            postProcess(convertedCityModel, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void postProcess(CityModelView convertedCityModel, String id) {
        LOD1SolidView lod1SolidView = null;
        LOD2SolidView lod2SolidView = null;
        for (var building : convertedCityModel.getChildrenUnmodifiable()) {
            if (!building.getId().equals(id))
                continue;
            var buildingView = (org.plateau.citygmleditor.citymodel.BuildingView) building;
            lod1SolidView = buildingView.getLOD1Solid();
            lod2SolidView = buildingView.getLOD2Solid();
        }
        if ((lod1SolidView == null) || (lod2SolidView == null)) {
            return;
        }
        var lod1vertices = lod1SolidView.getVertexBuffer().getVertices();
        var lod1geometry = createJTSGeometry(lod1vertices);
        var lod2vertices = lod2SolidView.getVertexBuffer().getVertices();
        var lod2geometry = createJTSGeometry(lod2vertices);
        if ((lod1geometry == null) || (lod2geometry == null))
            return;
        var lod2polygon = createJavaFXPolygon((org.locationtech.jts.geom.Polygon) lod2geometry);
        var geoReference = World.getActiveInstance().getGeoReference();
        var lod1point = geoReference.project(new GeoCoordinate(lod1geometry.getCentroid().getX(), lod1geometry.getCentroid().getY(), 0));
        var lod2point = geoReference.project(new GeoCoordinate(lod2geometry.getCentroid().getX(), lod2geometry.getCentroid().getY(), 0));
        var translate = new javafx.scene.transform.Translate(lod2point.x - lod1point.x, lod2point.y - lod1point.y);
        List<Geometry> lod2Geometries = new ArrayList<Geometry>();
        // 10度づつの回転オフセット
        for (int i = 0; i < 360/10; i++) {
            var rotate = new javafx.scene.transform.Rotate(i*10, lod2point.x, lod2point.y, 0, Rotate.Z_AXIS);
            lod2polygon.getTransforms().clear();
            lod2polygon.getTransforms().addAll(rotate, translate);
            lod2Geometries.add(createJTSPolygon(lod2polygon));
            // World.getRoot3D().getChildren().add(createJavaFXPolygon(createJTSPolygon(lod2polygon)));
        }
        var index1 = findNear(lod1geometry, lod2Geometries);
        var baseangle = index1 * 10;
        var angles = new ArrayList<Integer>();
        angles.add(baseangle);
        // ±1度づつの回転オフセット
        lod2Geometries.clear();
        for (int i = 0; i < 2; i++) {
            for (int j = 1; j < 10; j++) {
                var angle = (baseangle + (j * (i == 0 ? 1 : -1)) + 360) % 360;
                angles.add(angle);
                var rotate = new javafx.scene.transform.Rotate(angle, lod2point.x, lod2point.y, 0, Rotate.Z_AXIS);
                lod2polygon.getTransforms().clear();
                lod2polygon.getTransforms().addAll(rotate, translate);
                lod2Geometries.add(createJTSPolygon(lod2polygon));
                // World.getRoot3D().getChildren().add(createJavaFXPolygon(createJTSPolygon(lod2polygon)));
            }
        }
        var index2 = findNear(lod1geometry, lod2Geometries);
        var fixangle = angles.get(index2);
        // var fixGeometry = lod2Geometries.get(index2);
        // var fixpolygon = createJavaFXPolygon((org.locationtech.jts.geom.Polygon) fixGeometry);
        // World.getRoot3D().getChildren().addAll(fixpolygon);
        // 反映
        var manipulator = lod2SolidView.getTransformManipulator();
        Transform worldToLocalTransform = manipulator.getTransformCache();
        // 逆変換行列を取得
        try {
            worldToLocalTransform = worldToLocalTransform.createInverse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        var offset = worldToLocalTransform.transform(lod1point.x - lod2point.x, lod1point.y - lod2point.y, lod1SolidView.getTransformManipulator().getOrigin().getZ() - manipulator.getOrigin().getZ());
        var pivot = worldToLocalTransform.transform(lod2point.x, lod2point.y, 0);
        var axis = worldToLocalTransform.transform(Rotate.Z_AXIS);
        var fixTranslate = new javafx.scene.transform.Translate(offset.getX(), offset.getY(), offset.getZ());
        var fixRotate = new javafx.scene.transform.Rotate(-fixangle, pivot.getX(), pivot.getY(), pivot.getZ(), axis);
        manipulator.addTransformCache(fixTranslate);
        manipulator.addTransformCache(fixRotate);
        manipulator.setLocation(new Point3D(manipulator.getLocation().getX() + offset.getX(), manipulator.getLocation().getY() + offset.getY(), manipulator.getLocation().getZ() + offset.getZ()));
        manipulator.setRotation(new Point3D(0, 0, fixangle));
        lod2SolidView.getTransforms().clear();
        lod2SolidView.getTransforms().add(manipulator.getTransformCache());
        lod2SolidView.refrectGML();
    }
    
    private int findNear(Geometry lod1geometry, List<Geometry> lod2Geometries) {
        var overlapArea = 0.0d;
        int index = 0;
        for (int i = 0; i < lod2Geometries.size(); i++) {
            var intersection = lod1geometry.intersection(lod2Geometries.get(i));
            var overlap = intersection.getArea();
            if (overlap > overlapArea) {
                overlapArea = overlap;
                index = i;
            }
        }
        return index;
    }

    private Geometry createJTSGeometry(List<Vec3f> vertices) {
        var coordinates = new ArrayList<Coordinate>();
        var geoReference = World.getActiveInstance().getGeoReference();
        for (var vertex : vertices) {
            vertex.z = 0.0f;
            var coord = geoReference.unproject(vertex);
            coordinates.add(new Coordinate(coord.lat, coord.lon));
        }
        return new ConvexHull(coordinates.toArray(new Coordinate[0]), new GeometryFactory()).getConvexHull();
    }

    private javafx.scene.shape.Polygon createJavaFXPolygon(
            org.locationtech.jts.geom.Polygon jtsPolygon) {
        var geoReference = World.getActiveInstance().getGeoReference();
        javafx.scene.shape.Polygon javafxPolygon = new javafx.scene.shape.Polygon();

        // JTS Polygon の座標を取得して JavaFX Polygon に設定
        for (Coordinate coordinate : jtsPolygon.getCoordinates()) {
            var local = geoReference
                    .project(new GeoCoordinate(coordinate.x, coordinate.y, coordinate.z));
            javafxPolygon.getPoints().addAll((double) local.x, (double) local.y);
        }

        return javafxPolygon;
    }
    
    public static org.locationtech.jts.geom.Polygon createJTSPolygon(Polygon javafxPolygon) {
        var geoReference = World.getActiveInstance().getGeoReference();
        List<Coordinate> coordinates = new ArrayList<>();
        var transform = javafxPolygon.getLocalToParentTransform();
        for (int i = 0; i < javafxPolygon.getPoints().size(); i += 2) {
            double x = javafxPolygon.getPoints().get(i);
            double y = javafxPolygon.getPoints().get(i + 1);
            Point3D point = Point3D.ZERO;
            try {
                point = transform.inverseTransform(new Point3D(x, y, 0));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            var coordinate = geoReference.unproject(new Vec3f((float)point.getX(), (float) point.getY(), (float)point.getZ()));
            coordinates.add(new Coordinate(coordinate.lat, coordinate.lon));
        }

        // Close the ring if needed
        if (coordinates.size() > 2
                && !coordinates.get(0).equals2D(coordinates.get(coordinates.size() - 1))) {
            coordinates.add(coordinates.get(0));
        }

        GeometryFactory geometryFactory = new GeometryFactory();
        org.locationtech.jts.geom.LinearRing linearRing =
                geometryFactory.createLinearRing(coordinates.toArray(new Coordinate[0]));
        return geometryFactory.createPolygon(linearRing, null);
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
            node.setOnMouseClicked(t -> {
                TreeItem<Node> parent = getParent();
                while (parent != null) {
                    parent.setExpanded(true);
                    parent = parent.getParent();
                }
                hierarchyTreeTable.getSelectionModel().select(HierarchyController.TreeItemImpl.this);
                hierarchyTreeTable.scrollTo(hierarchyTreeTable.getSelectionModel().getSelectedIndex());
                t.consume();
            });
        }
    }
}
