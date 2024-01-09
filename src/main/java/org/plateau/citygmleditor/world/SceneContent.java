package org.plateau.citygmleditor.world;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.beans.property.ObjectProperty;
import javafx.scene.SubScene;

import org.plateau.citygmleditor.citygmleditor.*;

/**
 * Class responsible for Management of Scenes and Contents within the View
 */
public class SceneContent {
    private final SimpleObjectProperty<SubScene> subSceneProperty = new SimpleObjectProperty<>();
    private ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private AutoScalingGroup autoScalingGroup;
    private AntiAliasing antiAliasing;
    private Camera camera;
    private Group root3D;

    public SimpleObjectProperty<SubScene> subSceneProperty() {
        return this.subSceneProperty;
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public SubScene getSubScene() {
        return this.subSceneProperty.get();
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    {
        contentProperty().addListener((ov, oldContent, newContent) -> {
            autoScalingGroup.getChildren().remove(oldContent);
            autoScalingGroup.getChildren().add(newContent);
        });
    }

    public void rebuildSubScene() {
        SubScene oldSubScene = subSceneProperty.get();
        if (oldSubScene != null) {
            oldSubScene.setRoot(new Region());
            oldSubScene.setCamera(null);
            oldSubScene.removeEventHandler(MouseEvent.ANY, camera.getMouseEventHandler());
            oldSubScene.removeEventHandler(KeyEvent.ANY, camera.getKeyEventHandler());
            oldSubScene.removeEventHandler(ScrollEvent.ANY, camera.getScrollEventHandler());
        }

        javafx.scene.SceneAntialiasing aaVal = antiAliasing.getMsaa() ? javafx.scene.SceneAntialiasing.BALANCED
                : javafx.scene.SceneAntialiasing.DISABLED;
        SubScene subScene = new SubScene(root3D, 400, 400, true, aaVal);
        this.subSceneProperty.set(subScene);
        subScene.setFill(Color.GRAY);
        subScene.setCamera(camera.getCamera());
        // SCENE EVENT HANDLING FOR cameraA NAV
        subScene.addEventHandler(MouseEvent.ANY, camera.getMouseEventHandler());
        subScene.addEventHandler(KeyEvent.ANY, camera.getKeyEventHandler());
        // subScene.addEventFilter(KeyEvent.ANY, keyEventHandler);
        subScene.addEventHandler(ZoomEvent.ANY, camera.getZoomEventHandler());
        subScene.addEventHandler(ScrollEvent.ANY, camera.getScrollEventHandler());
    }

    public SceneContent() {
        this.antiAliasing = CityGMLEditorApp.getAntiAliasing();
        this.autoScalingGroup = CityGMLEditorApp.getAutoScalingGroup();
        this.camera = CityGMLEditorApp.getCamera();
        this.root3D = World.getRoot3D();
    }
}
