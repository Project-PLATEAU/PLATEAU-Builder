package org.plateau.plateaubuilder.control.surfacetype;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import org.plateau.plateaubuilder.control.Outline;
import org.plateau.plateaubuilder.world.World;

import java.util.HashMap;
import java.util.Map;

public class SurfaceTypeEditor {
    private final ObjectProperty<SurfaceTypeEditMode> mode = new SimpleObjectProperty<>();
    private final ObjectProperty<BuildingSurfaceTypeView> targetView = new SimpleObjectProperty<>();
    private final Outline outLine = new Outline();
    private final Map<SurfaceTypeEditMode, PerModeSurfaceEditor> perModeSurfaceEditorMap = new HashMap<>();
    {
        perModeSurfaceEditorMap.put(SurfaceTypeEditMode.POLYGON_TYPE_EDIT, new PolygonTypeEditor());
        perModeSurfaceEditorMap.put(SurfaceTypeEditMode.COMPONENT_TYPE_EDIT, new ComponentTypeEditor());
        perModeSurfaceEditorMap.put(SurfaceTypeEditMode.COMPONENT_POLYGON_EDIT, new ComponentPolygonEditor());
        perModeSurfaceEditorMap.put(SurfaceTypeEditMode.COMPONENT_OPENING_EDIT, new ComponentOpeningEditor());
    }

    private final BooleanProperty enabled = new SimpleBooleanProperty();
    {
        enabled.addListener((observableValue, oldValue, newValue) -> {
            if (!newValue)
                clear();
        });
    }

    public SurfaceTypeEditMode getMode() {
        return mode.get();
    }

    public ObjectProperty<SurfaceTypeEditMode> modeProperty() {
        return mode;
    }
    {
        modeProperty().addListener((observable, oldValue, newValue) -> {
            outLine.clear();

            if (oldValue != null)
                perModeSurfaceEditorMap.get(oldValue).reset();
        });
    }

    public ObjectProperty<BuildingSurfaceTypeView> targetViewProperty() {
        return targetView;
    }

    public SurfaceTypeEditor() {
        var node = (Group) World.getRoot3D();
        node.getChildren().add(outLine);
    }

    public void registerClickEvent(SubScene subScene) {
        subScene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!enabled.get())
                return;

            if (!event.isPrimaryButtonDown())
                return;

            PerModeSurfaceEditor perModeSurfaceEditor = perModeSurfaceEditorMap.get(getMode());
            if (perModeSurfaceEditor != null) {
                perModeSurfaceEditor.onClick(event);
                perModeSurfaceEditor.refreshOutLine(outLine);
            }
        });
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        return enabled;
    }

    public void clear() {
        targetView.set(null);
        outLine.setMesh(null);
    }

    public PolygonTypeEditor getPolygonTypeEditor() {
        return (PolygonTypeEditor) perModeSurfaceEditorMap.get(SurfaceTypeEditMode.POLYGON_TYPE_EDIT);
    }

    public ComponentTypeEditor getComponentTypeEditor() {
        return (ComponentTypeEditor) perModeSurfaceEditorMap.get(SurfaceTypeEditMode.COMPONENT_TYPE_EDIT);
    }

    public ComponentPolygonEditor getComponentPolygonEditor() {
        return (ComponentPolygonEditor) perModeSurfaceEditorMap.get(SurfaceTypeEditMode.COMPONENT_POLYGON_EDIT);
    }

    public ComponentOpeningEditor getComponentOpeningEditor() {
        return (ComponentOpeningEditor) perModeSurfaceEditorMap.get(SurfaceTypeEditMode.COMPONENT_OPENING_EDIT);
    }
}
