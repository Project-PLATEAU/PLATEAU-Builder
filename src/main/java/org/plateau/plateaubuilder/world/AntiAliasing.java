package org.plateau.plateaubuilder.world;

import javafx.beans.property.SimpleBooleanProperty;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;

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
        this.sceneContent = PLATEAUBuilderApp.getSceneContent();
    }
}
