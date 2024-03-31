package org.plateau.plateaubuilder.control;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.transform.Scale;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateau.plateaubuilder.control.surfacetype.PolygonSection;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.citymodel.geometry.ILODSolidView;
import org.plateau.plateaubuilder.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureSelection {
    private FeatureDragSelector dragSelector;

    private final ObjectProperty<BuildingView> active = new SimpleObjectProperty<>();
    private final ObjectProperty<AbstractCityObject> activeCityObject = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> selectElement = new SimpleObjectProperty<>();
    private final SetProperty<BuildingView> selectedFeatures = new SimpleSetProperty<>();
    {
        selectedFeatures.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                active.set(null);
                activeCityObject.set(null);
            } else {
                var feature = newValue.stream().findFirst().get();
                active.set(feature);
                activeCityObject.set(feature.getGML());
                var viewMode = PLATEAUBuilderApp.getCityModelViewMode();
                var solid = feature.getSolid(viewMode.getLOD());
                if (solid != null) {
                    selectElement.set((Node) solid);
                }
            }

            refreshOutLine();
        });
    }

    private final List<Outline> outlines = new ArrayList<>();
    private final Group outlineGroup = new Group();
    private final BooleanProperty outlineVisibleProperty = new SimpleBooleanProperty(true);
    {
        outlineVisibleProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                refreshOutLine();
            } else {
                clearOutlines();
            }
        });
    }

    private final ObjectProperty<List<PolygonSection>> activeSection = new SimpleObjectProperty<>();


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
        node.getChildren().add(outlineGroup);

        // モード切替時に選択をリセット
        var viewMode = PLATEAUBuilderApp.getCityModelViewMode();
        viewMode.isSurfaceViewModeProperty().addListener((observable) -> refreshOutLine());
        viewMode.lodProperty().addListener((observable) -> {
            if (active.get() != null) {
                var solid = active.get().getSolid(viewMode.getLOD());
                if (solid != null && solid != selectElement.get())
                    selectElement.set((Node) solid);
            }
            refreshOutLine();
        });
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

            if (event.isShiftDown()) {
                if (!selectedFeatures.isEmpty() && feature != null) {
                    addSelection(feature);
                }
            } else if (event.isControlDown()) {
                if (!selectedFeatures.isEmpty() && feature != null) {
                    if (selectedFeatures.contains(feature))
                        unselect(feature);
                    else
                        addSelection(feature);
                }
            } else {
                select(feature);

                selectElement.set((Node) element);
            }
        });
    }

    public void addSelection(BuildingView feature) {
        if (feature == null)
            throw new IllegalArgumentException();

        selectedFeatures.add(feature);
    }

    public void unselect(BuildingView feature) {
        if (!selectedFeatures.contains(feature))
            return;

        selectedFeatures.remove(feature);
    }

    public void select(BuildingView... features) {
        clear();

        if (features.length == 0 || features[0] == null)
            return;

        selectedFeatures.set(FXCollections.observableSet(features));
    }

    public void clear() {
        active.set(null);
        activeSection.set(null);
        selectedFeatures.clear();
        activeCityObject.set(null);
        clearOutlines();
        selectElement.set(null);
    }

    public void refreshOutLine() {
        clearOutlines();

        if (!outlineVisibleProperty.get())
            return;

        var viewMode = PLATEAUBuilderApp.getCityModelViewMode();

        for (var featureView : selectedFeatures) {
            var solid = featureView.getSolid(viewMode.getLOD());
            if (solid == null)
                continue;

            outlines.add(createOutline(solid));
        }
        outlineGroup.getChildren().addAll(outlines);
    }

    private Outline createOutline(ILODSolidView solidView) {
        if (solidView == null)
            throw new IllegalArgumentException();

        var outline = new Outline();
        outline.setMesh(solidView.getTotalMesh());

        // Transformの反映
        outline.getTransforms().clear();
        // 建物の座標変換情報から建物の座標変換を作成
        var manipulator = solidView.getTransformManipulator();
        var pivot = manipulator.getOrigin();
        outline.getTransforms().add(manipulator.getTransformCache());
        // スケールを適用
        outline.getTransforms().add(new Scale(manipulator.getScale().getX(), manipulator.getScale().getY(), manipulator.getScale().getZ(), pivot.getX(), pivot.getY(), pivot.getZ()));

        return outline;
    }

    private void clearOutlines() {
        outlineGroup.getChildren().clear();
        outlines.clear();
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

    public boolean isOutlineVisible() {
        return outlineVisibleProperty.get();
    }

    public BooleanProperty outlineVisibleProperty() {
        return outlineVisibleProperty;
    }

    public ReadOnlySetProperty<BuildingView> selectedFeaturesProperty() {
        return selectedFeatures;
    }

    private Set<BuildingView> preDragSelectedViews;

    public void initialize(SubScene subScene) {
        registerClickEvent(subScene);
        dragSelector = new FeatureDragSelector(subScene);

        dragSelector.draggingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue || !newValue)
                return;

            if (selectedFeatures.get() != null)
                preDragSelectedViews = selectedFeatures.get();
            else
                preDragSelectedViews = new HashSet<>();
        });

        dragSelector.selectedViewsProperty().addListener((observable, oldValue, newValue) -> {
            if (!dragSelector.isDragging())
                return;

            var selectedViews = new HashSet<>(preDragSelectedViews);
            selectedViews.addAll(newValue);

            selectedFeatures.set(FXCollections.observableSet(selectedViews));
        });
    }
}
