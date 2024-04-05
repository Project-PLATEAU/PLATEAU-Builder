package org.plateaubuilder.core.world;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

/**
 * Class responsible for Various Lighting Processes
 */
public class Light {
    private AmbientLight ambientLight = new AmbientLight(Color.DARKGREY);
    private PointLight light1 = new PointLight(Color.WHITE);
    private PointLight light2 = new PointLight(Color.ANTIQUEWHITE);
    private PointLight light3 = new PointLight(Color.ALICEBLUE);
    private Group root3D;

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

    public SimpleBooleanProperty ambientLightEnabledProperty() {
        return ambientLightEnabled;
    }

    public SimpleBooleanProperty light1EnabledProperty() {
        return light1Enabled;
    }

    public SimpleBooleanProperty light2EnabledProperty() {
        return light2Enabled;
    }

    public SimpleBooleanProperty light3EnabledProperty() {
        return light3Enabled;
    }

    public boolean getAmbientLightEnabled() {
        return ambientLightEnabled.get();
    }

    public boolean getLight1Enabled() {
        return light1Enabled.get();
    }

    public boolean getLight2Enabled() {
        return light2Enabled.get();
    }

    public boolean getLight3Enabled() {
        return light3Enabled.get();
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

    public void setAmbientLightEnabled(boolean ambientLightEnabled) {
        this.ambientLightEnabled.set(ambientLightEnabled);
    }

    public void setLight1Enabled(boolean light1Enabled) {
        this.light1Enabled.set(light1Enabled);
    }

    public void setLight2Enabled(boolean light2Enabled) {
        this.light2Enabled.set(light2Enabled);
    }

    public void setLight3Enabled(boolean light3Enabled) {
        this.light3Enabled.set(light3Enabled);
    }

    public Light() {
        this.root3D = World.getRoot3D();
    }
}
