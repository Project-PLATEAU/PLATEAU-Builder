package org.plateau.citygmleditor.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.TreeItem;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;
import org.plateau.citygmleditor.world.World;

import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;

import java.util.List;

public class FeatureSelection {
    private final ObjectProperty<BuildingView> active = new SimpleObjectProperty<>();
    private final ObjectProperty<AbstractCityObject> activeCityObject = new SimpleObjectProperty<>();
    private final OutLine outLine = new OutLine();

    private final ObjectProperty<List<PolygonSection>> activeSection = new SimpleObjectProperty<>();

    private final ObjectProperty<Node> selectElement = new SimpleObjectProperty<>();

    private BooleanProperty enabled = new SimpleBooleanProperty();
    {
        enabled.set(true);
        enabled.addListener((observableValue, oldValue, newValue) -> {
            if (!newValue)
                clear();
        });
    }

    public FeatureSelection() {
        var node = (Group) World.getRoot3D();
        node.getChildren().add(outLine);

        // モード切替時に選択をリセット
        var viewMode = CityGMLEditorApp.getCityModelViewMode();
        viewMode.isSurfaceViewModeProperty().addListener((observable) -> refreshOutLine());
        viewMode.lodProperty().addListener((observable) -> refreshOutLine());
    }

    public BuildingView getActive() {
        return active.get();
    }

    public ObjectProperty<BuildingView> getActiveFeatureProperty() {
        return active;
    }

    public ObjectProperty<List<PolygonSection>> getSurfacePolygonSectionProperty() {
        return activeSection;
    }

    public void registerClickEvent(SubScene scene) {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!enabled.get())
                return;

            if (!event.isPrimaryButtonDown())
                return;

            if (event.getClickCount() == 2) {
                if (active.get() != null) {
                    World.getActiveInstance().getCamera().focus(active.get().getLOD1Solid());
                }
                event.consume();

                return;
            }

            PickResult pickResult = event.getPickResult();
            var newSelectedMesh = pickResult.getIntersectedNode();

            if (newSelectedMesh != null
                    && World.getActiveInstance().getGizmo().isNodeInGizmo(newSelectedMesh)) {
                newSelectedMesh = World.getActiveInstance().getGizmo().getAttachNode();
            }

            var feature = getBuilding(newSelectedMesh);
            var element = getLodSolidView(newSelectedMesh);

            select(feature);

            selectElement.set((Node) element);
        });
    }

    public void select(BuildingView feature) {
        if (!enabled.get())
            return;

        clear();

        if (feature == null)
            return;

        var viewMode = CityGMLEditorApp.getCityModelViewMode();

        var solid = feature.getSolid(viewMode.getLOD());
        if (solid == null)
            return;

        active.set(feature);
        activeCityObject.set(feature.getGMLObject());

        refreshOutLine();
    }

    public void clear() {
        active.set(null);
        activeSection.set(null);
        activeCityObject.set(null);
        outLine.setMesh(null);
        selectElement.set(null);
    }

    public void refreshOutLine() {
        outLine.setMesh(null);

        if (active.get() == null)
            return;
        
        var viewMode = CityGMLEditorApp.getCityModelViewMode();

        var solid = active.get().getSolid(viewMode.getLOD());
        if (solid == null)
            return;

        outLine.setMesh(solid.getTotalMesh());

        selectElement.set((Node) solid);
    }

    public MeshView getOutLine() {
        return outLine;
    }

    private BuildingView getBuilding(Node node) {
        while (node != null && !(node instanceof BuildingView)) {
            node = node.getParent();
        }
        return (BuildingView) node;
    }
    
    private ILODSolidView getLodSolidView(Node node) {
        while (node != null && !(node instanceof ILODSolidView)) {
            node = node.getParent();
        }
        return (ILODSolidView) node;
    }
    
    public void setSelectElement(Node node) {
        selectElement.set(node);
    }

    public Node getSelectElement() {
        return selectElement.get();
    }
    
    public ObjectProperty<Node> getSelectElementProperty() {
        return selectElement;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public AbstractCityObject getActiveCityObject() {
        return activeCityObject.get();
    }

    public ObjectProperty<AbstractCityObject> activeCityObjectProperty() {
        return activeCityObject;
    }
}
