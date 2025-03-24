package org.plateaubuilder.core.world;

import javafx.beans.property.SimpleBooleanProperty;
import org.plateaubuilder.core.editor.Editor;

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
        this.sceneContent = Editor.getSceneContent();
    }
}
