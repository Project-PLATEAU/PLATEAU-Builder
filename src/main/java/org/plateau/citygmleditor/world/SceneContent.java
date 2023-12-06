package org.plateau.citygmleditor.world;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.input.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.scene.SubScene;

import org.plateau.citygmleditor.citygmleditor.*;

public class SceneContent {
    private final SimpleObjectProperty<SubScene> subScene = new SimpleObjectProperty<>();
    private ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private AutoScalingGroup autoScalingGroup;
    private AntiAliasing antiAliasing;
    private Camera camera;
    private Group root3D;

    public SimpleObjectProperty<SubScene> subSceneProperty() {
        return this.subScene;
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public SubScene getSubScene() {
        return this.subScene.get();
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
        SubScene oldSubScene = subScene.get();
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
        this.subScene.set(subScene);
        subScene.setFill(Color.ALICEBLUE);
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
