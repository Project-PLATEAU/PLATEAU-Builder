package org.plateau.citygmleditor.world;

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
import javax.crypto.Cipher;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.util.Duration;
import org.plateau.citygmleditor.citygmleditor.*;

/**
 * Class responsible for AntiAliasing
 */
public class AntiAliasing {
    private SceneContent sceneContent;

    private SimpleBooleanProperty msaa = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            sceneContent.rebuildSubScene();
        }
    };

    public boolean getMsaa() {
        return msaa.get();
    }

    public SimpleBooleanProperty msaaProperty() {
        return msaa;
    }

    public void setMsaa(boolean msaa) {
        this.msaa.set(msaa);
    }

    public void setSceneContent() {
        this.sceneContent = CityGMLEditorApp.getSceneContent();
    }
}
