package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.transportation.Road;
import org.plateaubuilder.core.citymodel.geometry.AbstractMultiSurfaceMeshView;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSurfaceView;
import org.plateaubuilder.core.editor.Editor;

import javafx.scene.Node;

public class RoadView extends ManagedGMLView<Road> implements IFeatureView {
    private LOD1MultiSurfaceView lod1MultiSurface;
    private LOD2MultiSurfaceView lod2MultiSurface;
    private LOD3MultiSurfaceView lod3MultiSurface;

    public RoadView(Road gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }

    public void toggleLODView(int lod) {
        var meshViews = new AbstractMultiSurfaceMeshView[] {
                lod1MultiSurface, lod2MultiSurface, lod3MultiSurface
        };
        for (int i = 1; i <= 3; ++i) {
            var meshView = meshViews[i - 1];
            if (meshView == null) {
                continue;
            }

            meshView.setVisible(lod == i);
        }
    }

    public void setDefaultVisible() {
        if (this.lod3MultiSurface != null) {
            this.lod3MultiSurface.setVisible(true);
            if (this.lod2MultiSurface != null) {
                this.lod2MultiSurface.setVisible(false);
            }
            if (this.lod1MultiSurface != null) {
                this.lod1MultiSurface.setVisible(false);
            }
        } else if (this.lod2MultiSurface != null) {
            this.lod2MultiSurface.setVisible(true);
            if (this.lod1MultiSurface != null) {
                this.lod1MultiSurface.setVisible(false);
            }
        } else if (this.lod1MultiSurface != null) {
            this.lod1MultiSurface.setVisible(true);
        }
    }

    public Node getNode() {
        return this;
    }

    public ILODView getLODView(int lod) {
        return getMultiSurface(lod);
    }

    public void setLODView(int lod, ILODView lodView) {
        if (lodView instanceof LOD1MultiSurfaceView) {
            setLOD1MultiSurface((LOD1MultiSurfaceView) lodView);
        } else if (lodView instanceof LOD2MultiSurfaceView) {
            setLOD2MultiSurface((LOD2MultiSurfaceView) lodView);
        } else if (lodView instanceof LOD3MultiSurfaceView) {
            setLOD3MultiSurface((LOD3MultiSurfaceView) lodView);
        }
    }

    public ILODMultiSurfaceView getMultiSurface(int lod) {
        switch (lod) {
        case 1:
            return lod1MultiSurface;
        case 2:
            return lod2MultiSurface;
        case 3:
            return lod3MultiSurface;
        default:
            return null;
        }
    }

    public void setLOD1MultiSurface(LOD1MultiSurfaceView MultiSurface) {
        if (this.lod1MultiSurface == null) {
            this.getChildren().remove(this.lod1MultiSurface);
        }
        this.lod1MultiSurface = MultiSurface;
        this.getChildren().add(MultiSurface);
        MultiSurface.getTransformManipulator().updateOrigin();
    }

    public LOD1MultiSurfaceView getLOD1MultiSurface() {
        return this.lod1MultiSurface;
    }

    public void setLOD2MultiSurface(LOD2MultiSurfaceView MultiSurface) {
        if (MultiSurface == null)
            return;

        if (this.lod2MultiSurface != null) {
            this.getChildren().remove(this.lod2MultiSurface);
        }
        this.lod2MultiSurface = MultiSurface;
        this.getChildren().add(MultiSurface);
        MultiSurface.getTransformManipulator().updateOrigin();
    }

    public LOD2MultiSurfaceView getLOD2MultiSurface() {
        return this.lod2MultiSurface;
    }

    public void setLOD3MultiSurface(LOD3MultiSurfaceView MultiSurface) {
        if (MultiSurface == null)
            return;

        if (this.lod3MultiSurface != null) {
            this.getChildren().remove(this.lod3MultiSurface);
        }
        this.lod3MultiSurface = MultiSurface;
        this.getChildren().add(MultiSurface);
        MultiSurface.getTransformManipulator().updateOrigin();
    }

    public LOD3MultiSurfaceView getLOD3MultiSurface() {
        return this.lod3MultiSurface;
    }
}
