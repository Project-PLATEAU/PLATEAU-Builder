package org.plateaubuilder.core.world;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.plateaubuilder.core.editor.Editor;

/**
 * Class responsible for Management of Scenes and Contents within the View
 */
public class SceneContent {
    private final SimpleObjectProperty<SubScene> subSceneProperty = new SimpleObjectProperty<>();

    public SimpleObjectProperty<SubScene> subSceneProperty() {
        return this.subSceneProperty;
    }

    public SubScene getSubScene() {
        return this.subSceneProperty.get();
    }

    public void rebuildSubScene() {
        SubScene oldSubScene = subSceneProperty.get();
        var camera = World.getActiveInstance().getCamera();

        if (oldSubScene != null) {
            oldSubScene.setRoot(new Region());
            oldSubScene.setCamera(null);
            oldSubScene.removeEventHandler(MouseEvent.ANY, camera.getMouseEventHandler());
            oldSubScene.removeEventHandler(ScrollEvent.ANY, camera.getScrollEventHandler());
        }

        var antiAliasing = Editor.getAntiAliasing();
        javafx.scene.SceneAntialiasing aaVal = antiAliasing.getMsaa() ? javafx.scene.SceneAntialiasing.BALANCED
                : javafx.scene.SceneAntialiasing.DISABLED;
        SubScene subScene = new SubScene(World.getRoot3D(), 400, 400, true, aaVal);
        this.subSceneProperty.set(subScene);
        subScene.setFill(Color.valueOf("#353535"));
        subScene.setCamera(camera.getCamera());

        subScene.addEventHandler(MouseEvent.ANY, camera.getMouseEventHandler());
        subScene.addEventHandler(ScrollEvent.ANY, camera.getScrollEventHandler());
    }
}
