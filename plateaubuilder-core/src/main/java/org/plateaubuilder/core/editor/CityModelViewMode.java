package org.plateaubuilder.core.editor;

import javafx.beans.property.*;

public class CityModelViewMode {
    private final BooleanProperty isSurfaceViewMode = new SimpleBooleanProperty();
    private final IntegerProperty lod = new SimpleIntegerProperty();

    private boolean isSurfaceViewModeInternal = false;

    public boolean isSurfaceViewMode() {
        return isSurfaceViewMode.get();
    }

    public ReadOnlyBooleanProperty isSurfaceViewModeProperty() {
        return isSurfaceViewMode;
    }

    public void toggleSurfaceViewMode(boolean value) {
        isSurfaceViewModeInternal = value;
        updateSurfaceViewMode();
    }

    public int getLOD() {
        return lod.get();
    }

    public IntegerProperty lodProperty() {
        return lod;
    }

    public CityModelViewMode() {
        lod.addListener(observable -> {
            updateSurfaceViewMode();
        });

        isSurfaceViewMode.set(false);
        lod.set(1);
    }

    private void updateSurfaceViewMode() {
        isSurfaceViewMode.set(isSurfaceViewModeInternal && lod.get() >= 2);
    }
}
