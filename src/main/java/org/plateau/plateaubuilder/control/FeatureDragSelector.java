package org.plateau.plateaubuilder.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.world.World;

import java.util.ArrayList;

public class FeatureDragSelector {
    DoubleProperty startXProperty = new SimpleDoubleProperty();
    DoubleProperty startYProperty = new SimpleDoubleProperty();
    DoubleProperty endXProperty = new SimpleDoubleProperty();
    DoubleProperty endYProperty = new SimpleDoubleProperty();
    Rectangle rect = new Rectangle();
    {
        rect.xProperty().bind(
                Bindings.min(startXProperty, endXProperty)
        );
        rect.yProperty().bind(
                Bindings.min(startYProperty, endYProperty)
        );
        rect.widthProperty().bind(
                Bindings.max(startXProperty, endXProperty).subtract(Bindings.min(startXProperty, endXProperty))
        );
        rect.heightProperty().bind(
                Bindings.max(startYProperty, endYProperty).subtract(Bindings.min(startYProperty, endYProperty))
        );
    }

    private final ListProperty<BuildingView> selectedViews = new SimpleListProperty<>();
    private final BooleanProperty dragging = new SimpleBooleanProperty(false);

    AnchorPane overlay;

    public ReadOnlyListProperty<BuildingView> selectedViewsProperty() {
        return selectedViews;
    }

    public FeatureDragSelector(SubScene subScene) {
        overlay = new AnchorPane();
        ((Pane)subScene.getParent()).getChildren().add(overlay);
        rect.setFill(null);
        rect.setStroke(Color.GREEN);
        overlay.getChildren().add(rect);
        overlay.setMouseTransparent(true);
        rect.setMouseTransparent(true);
        rect.setVisible(false);

        subScene.addEventHandler(MouseEvent.ANY, event -> {
            if (!event.isShiftDown())
                return;

            if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
                startXProperty.set(event.getX());
                startYProperty.set(event.getY());
            } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                if (!draggingProperty().get())
                    draggingProperty().set(true);
                endXProperty.set(event.getX());
                endYProperty.set(event.getY());
                rect.setVisible(true);

                var bounds = new BoundingBox(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

                var features = new ArrayList<BuildingView>();
                var group = World.getActiveInstance().getCityModelGroup();

                if (group != null) {
                    for (var feature : World.getActiveInstance().getCityModelGroup().getAllFeatures()) {
                        if (!(feature instanceof BuildingView))
                            continue;

                        var featureBounds = getBounds((BuildingView) feature);
                        if (featureBounds.intersects(bounds))
                            features.add((BuildingView) feature);
                    }

                    selectedViews.set(FXCollections.observableArrayList(features));
                }

                event.consume();
            } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
                draggingProperty().set(false);
                rect.setVisible(false);
                event.consume();
            }
        });
    }

    private Bounds getBounds(BuildingView view) {
        var sceneBounds = view.localToScene(view.getBoundsInLocal(), true);
        return overlay.sceneToLocal(sceneBounds);
    }

    public boolean isDragging() {
        return dragging.get();
    }

    public BooleanProperty draggingProperty() {
        return dragging;
    }
}
