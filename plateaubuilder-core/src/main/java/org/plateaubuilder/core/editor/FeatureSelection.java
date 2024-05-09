package org.plateaubuilder.core.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.editor.surfacetype.PolygonSection;
import org.plateaubuilder.core.world.World;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.transform.Scale;

public class FeatureSelection {
    private FeatureDragSelector dragSelector;

    private final ObjectProperty<IFeatureView> active = new SimpleObjectProperty<>();
    private final ObjectProperty<AbstractCityObject> activeCityObject = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> selectElement = new SimpleObjectProperty<>();
    private final SetProperty<IFeatureView> selectedFeatures = new SimpleSetProperty<>();
    {
        selectedFeatures.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                active.set(null);
                activeCityObject.set(null);
            } else {
                var feature = newValue.stream().findFirst().get();
                active.set(feature);
                activeCityObject.set(feature.getGML());
                var viewMode = Editor.getCityModelViewMode();
                var lodView = feature.getLODView(viewMode.getLOD());
                if (lodView != null) {
                    selectElement.set((Node) lodView);
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
        var viewMode = Editor.getCityModelViewMode();
        viewMode.isSurfaceViewModeProperty().addListener((observable) -> refreshOutLine());
        viewMode.lodProperty().addListener((observable) -> {
            if (active.get() != null) {
                var lodView = active.get().getLODView(viewMode.getLOD());
                if (lodView != null && lodView != selectElement.get())
                    selectElement.set((Node) lodView);
            }
            refreshOutLine();
        });
    }

    public IFeatureView getActive() {
        return active.get();
    }

    public ObjectProperty<IFeatureView> getActiveFeatureProperty() {
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
                    World.getActiveInstance().getCamera().focus(active.get().getLODView(1).getMeshView());
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

            var feature = getFeatureView(newSelectedMesh);
            var element = getLodView(newSelectedMesh);

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

    public void addSelection(IFeatureView feature) {
        if (feature == null)
            throw new IllegalArgumentException();

        selectedFeatures.add(feature);
    }

    public void unselect(IFeatureView feature) {
        if (!selectedFeatures.contains(feature))
            return;

        selectedFeatures.remove(feature);
    }

    public void select(IFeatureView... features) {
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

        var viewMode = Editor.getCityModelViewMode();

        for (var featureView : selectedFeatures) {
            var lodView = featureView.getLODView(viewMode.getLOD());
            if (lodView == null)
                continue;

            outlines.add(createOutline(lodView));
        }
        outlineGroup.getChildren().addAll(outlines);
    }

    private Outline createOutline(ILODView solidView) {
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

    private IFeatureView getFeatureView(Node node) {
        while (node != null && !(node instanceof IFeatureView)) {
            node = node.getParent();
        }
        return (IFeatureView) node;
    }
    
    private ILODView getLodView(Node node) {
        while (node != null && !(node instanceof ILODView)) {
            node = node.getParent();
        }
        return (ILODView) node;
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

    public ReadOnlySetProperty<IFeatureView> selectedFeaturesProperty() {
        return selectedFeatures;
    }

    private Set<IFeatureView> preDragSelectedViews;

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
