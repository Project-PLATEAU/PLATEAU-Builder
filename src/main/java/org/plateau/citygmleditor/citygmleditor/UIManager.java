package org.plateau.citygmleditor.citygmleditor;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
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

/**
 * Class responsible for managing the status and display of buttons on the UI
 */
public class UIManager {
    private CameraController cameraController;
    private final SimpleObjectProperty<SubScene> subScene = new SimpleObjectProperty<>();
    private final Group root3D = new Group();
    private ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private AutoScalingGroup autoScalingGroup = new AutoScalingGroup(2);
    private Box xAxis, yAxis, zAxis;
    private Sphere xSphere, ySphere, zSphere;
    private AmbientLight ambientLight = new AmbientLight(Color.DARKGREY);
    private PointLight light1 = new PointLight(Color.WHITE);
    private PointLight light2 = new PointLight(Color.ANTIQUEWHITE);
    private PointLight light3 = new PointLight(Color.ALICEBLUE);

    private SimpleBooleanProperty ambientLightEnabled = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                root3D.getChildren().add(ambientLight);
            } else {
                root3D.getChildren().remove(ambientLight);
            }
        }
    };

    private SimpleBooleanProperty light1Enabled = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                root3D.getChildren().add(light1);
            } else {
                root3D.getChildren().remove(light1);
            }
        }
    };

    private SimpleBooleanProperty light2Enabled = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                root3D.getChildren().add(light2);
            } else {
                root3D.getChildren().remove(light2);
            }
        }
    };

    private SimpleBooleanProperty light3Enabled = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                root3D.getChildren().add(light3);
            } else {
                root3D.getChildren().remove(light3);
            }
        }
    };

    private SimpleBooleanProperty showAxis = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                if (xAxis == null) {
                    createAxes();
                }
                autoScalingGroup.getChildren().addAll(xAxis, yAxis, zAxis);
                autoScalingGroup.getChildren().addAll(xSphere, ySphere, zSphere);
            } else if (xAxis != null) {
                autoScalingGroup.getChildren().removeAll(xAxis, yAxis, zAxis);
                autoScalingGroup.getChildren().removeAll(xSphere, ySphere, zSphere);
            }
        }
    };

    private SimpleBooleanProperty yUp = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
            if (get()) {
                cameraController.setYUpRotate(180);
            } else {
                cameraController.setYUpRotate(0);
            }
        }
    };

    private SimpleBooleanProperty rotateMode = new SimpleBooleanProperty(true) {
        @Override
        protected void invalidated() {
        }
    };

    private SimpleBooleanProperty moveMode = new SimpleBooleanProperty(false) {
        @Override
        protected void invalidated() {
        }
    };

    private SimpleBooleanProperty msaa = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            rebuildSubScene();
        }
    };

    public boolean getAmbientLightEnabled() {
        return ambientLightEnabled.get();
    }

    public SimpleBooleanProperty ambientLightEnabledProperty() {
        return ambientLightEnabled;
    }

    public void setAmbientLightEnabled(boolean ambientLightEnabled) {
        this.ambientLightEnabled.set(ambientLightEnabled);
    }

    public boolean getLight1Enabled() {
        return light1Enabled.get();
    }

    public SimpleBooleanProperty light1EnabledProperty() {
        return light1Enabled;
    }

    public void setLight1Enabled(boolean light1Enabled) {
        this.light1Enabled.set(light1Enabled);
    }

    public boolean getLight2Enabled() {
        return light2Enabled.get();
    }

    public SimpleBooleanProperty light2EnabledProperty() {
        return light2Enabled;
    }

    public void setLight2Enabled(boolean light2Enabled) {
        this.light2Enabled.set(light2Enabled);
    }

    public boolean getLight3Enabled() {
        return light3Enabled.get();
    }

    public SimpleBooleanProperty light3EnabledProperty() {
        return light3Enabled;
    }

    public void setLight3Enabled(boolean light3Enabled) {
        this.light3Enabled.set(light3Enabled);
    }

    public AmbientLight getAmbientLight() {
        return ambientLight;
    }

    public PointLight getLight1() {
        return light1;
    }

    public PointLight getLight2() {
        return light2;
    }

    public PointLight getLight3() {
        return light3;
    }

    public SimpleBooleanProperty yUpProperty() {
        return yUp;
    }

    public boolean getYUp() {
        return yUp.get();
    }

    public void setYUp(boolean yUp) {
        this.yUp.set(yUp);
    }

    public SimpleBooleanProperty moveModeProperty() {
        return moveMode;
    }

    public boolean getMoveMode() {
        return moveMode.get();
    }

    public SimpleBooleanProperty rotateModeProperty() {
        return rotateMode;
    }

    public boolean getRotateMode() {
        return rotateMode.get();
    }

    public boolean getShowAxis() {
        return showAxis.get();
    }

    public SimpleBooleanProperty showAxisProperty() {
        return showAxis;
    }

    public void setShowAxis(boolean showAxis) {
        this.showAxis.set(showAxis);
    }

    public AutoScalingGroup getAutoScalingGroup() {
        return autoScalingGroup;
    }

    public ObjectProperty<Node> contentProperty() {
        return content;
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

    public boolean getMsaa() {
        return msaa.get();
    }

    public SimpleBooleanProperty msaaProperty() {
        return msaa;
    }

    public void setMsaa(boolean msaa) {
        this.msaa.set(msaa);
    }

    public SubScene getSubScene() {
        return subScene.get();
    }

    public SimpleObjectProperty<SubScene> subSceneProperty() {
        return subScene;
    }

    public Group getRoot3D() {
        return root3D;
    }

    public void setCametController(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    public void rebuildSubScene() {
        SubScene oldSubScene = this.subScene.get();
        if (oldSubScene != null) {
            oldSubScene.setRoot(new Region());
            oldSubScene.setCamera(null);
            oldSubScene.removeEventHandler(MouseEvent.ANY, cameraController.getMouseEventHandler());
            oldSubScene.removeEventHandler(KeyEvent.ANY, cameraController.getKeyEventHandler());
            oldSubScene.removeEventHandler(ScrollEvent.ANY, cameraController.getScrollEventHandler());
        }

        javafx.scene.SceneAntialiasing aaVal = msaa.get() ? javafx.scene.SceneAntialiasing.BALANCED
                : javafx.scene.SceneAntialiasing.DISABLED;
        SubScene subScene = new SubScene(root3D, 400, 400, true, aaVal);
        this.subScene.set(subScene);
        subScene.setFill(Color.ALICEBLUE);
        subScene.setCamera(cameraController.getCamera());
        // SCENE EVENT HANDLING FOR CAMERA NAV
        subScene.addEventHandler(MouseEvent.ANY, cameraController.getMouseEventHandler());
        subScene.addEventHandler(KeyEvent.ANY, cameraController.getKeyEventHandler());
        // subScene.addEventFilter(KeyEvent.ANY, keyEventHandler);
        subScene.addEventHandler(ZoomEvent.ANY, cameraController.getZoomEventHandler());
        subScene.addEventHandler(ScrollEvent.ANY, cameraController.getScrollEventHandler());
    }

    private void createAxes() {
        double length = 200.0;
        double width = 1.0;
        double radius = 2.0;
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);
        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        xSphere = new Sphere(radius);
        ySphere = new Sphere(radius);
        zSphere = new Sphere(radius);
        xSphere.setMaterial(redMaterial);
        ySphere.setMaterial(greenMaterial);
        zSphere.setMaterial(blueMaterial);

        xSphere.setTranslateX(100.0);
        ySphere.setTranslateY(100.0);
        zSphere.setTranslateZ(100.0);

        xAxis = new Box(length, width, width);
        yAxis = new Box(width, length, width);
        zAxis = new Box(width, width, length);
        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);
    }

    public void Initialize() {
        root3D.getChildren().add(cameraController.getCameraXform());
        root3D.getChildren().add(autoScalingGroup);
        rebuildSubScene();
    }
}
