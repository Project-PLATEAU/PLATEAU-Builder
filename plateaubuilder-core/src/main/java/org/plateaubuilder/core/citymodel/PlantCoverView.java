package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.plateaubuilder.core.citymodel.geometry.AbstractLODMeshView;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSurfaceView;
import org.plateaubuilder.core.editor.Editor;

import javafx.scene.Node;

public class PlantCoverView extends ManagedGMLView<PlantCover> implements IFeatureView {
    private LOD1MultiSolidView lod1MultiSolid;
    private LOD2MultiSolidView lod2MultiSolid;
    private LOD3MultiSolidView lod3MultiSolid;
    private LOD2MultiSurfaceView lod2MultiSurface;
    private LOD3MultiSurfaceView lod3MultiSurface;

    public PlantCoverView(PlantCover gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }

    public void toggleLODView(int lod) {
        var meshViews = new AbstractLODMeshView[] { lod1MultiSolid, lod2MultiSolid != null ? lod2MultiSolid : lod2MultiSurface,
                lod3MultiSolid != null ? lod3MultiSolid : lod3MultiSurface };
        for (int i = 1; i <= 3; ++i) {
            var meshView = meshViews[i - 1];
            if (meshView == null) {
                continue;
            }

            meshView.setVisible(lod == i);
        }
    }

    public void setDefaultVisible() {
        if (this.lod3MultiSolid != null || this.lod3MultiSurface != null) {
            if (this.lod3MultiSolid != null) {
                this.lod3MultiSolid.setVisible(true);
            }
            if (this.lod3MultiSurface != null) {
                this.lod3MultiSurface.setVisible(true);
            }
            if (this.lod2MultiSolid != null) {
                this.lod2MultiSolid.setVisible(false);
            }
            if (this.lod2MultiSurface != null) {
                this.lod2MultiSurface.setVisible(false);
            }
            if (this.lod1MultiSolid != null) {
                this.lod1MultiSolid.setVisible(false);
            }
        } else if (this.lod2MultiSolid != null || this.lod2MultiSurface != null) {
            if (this.lod2MultiSolid != null) {
                this.lod2MultiSolid.setVisible(true);
            }
            if (this.lod2MultiSurface != null) {
                this.lod2MultiSurface.setVisible(true);
            }
            if (this.lod1MultiSolid != null) {
                this.lod1MultiSolid.setVisible(false);
            }
        } else if (this.lod1MultiSolid != null) {
            this.lod1MultiSolid.setVisible(true);
        }
    }

    public Node getNode() {
        return this;
    }

    public ILODView getLODView(int lod) {
        var multiSolid = getMultiSolid(lod);
        if (multiSolid != null) {
            return multiSolid;
        }

        return getMultiSurface(lod);
    }

    public void setLODView(int lod, ILODView lodView) {
        if (lodView instanceof LOD1MultiSolidView) {
            setLOD1MultiSolid((LOD1MultiSolidView) lodView);
        } else if (lodView instanceof LOD2MultiSolidView) {
            setLOD2MultiSolid((LOD2MultiSolidView) lodView);
        } else if (lodView instanceof LOD3MultiSolidView) {
            setLOD3MultiSolid((LOD3MultiSolidView) lodView);
        } else if (lodView instanceof LOD2MultiSurfaceView) {
            setLOD2MultiSurface((LOD2MultiSurfaceView) lodView);
        } else if (lodView instanceof LOD3MultiSurfaceView) {
            setLOD3MultiSurface((LOD3MultiSurfaceView) lodView);
        }
    }

    public ILODMultiSolidView getMultiSolid(int lod) {
        switch (lod) {
        case 1:
            return lod1MultiSolid;
        case 2:
            return lod2MultiSolid;
        case 3:
            return lod3MultiSolid;
        default:
            return null;
        }
    }

    public ILODMultiSurfaceView getMultiSurface(int lod) {
        switch (lod) {
        case 2:
            return lod2MultiSurface;
        case 3:
            return lod3MultiSurface;
        default:
            return null;
        }
    }

    public void setLOD1MultiSolid(LOD1MultiSolidView multiSolid) {
        if (this.lod1MultiSolid == null) {
            this.getChildren().remove(this.lod1MultiSolid);
        }
        this.lod1MultiSolid = multiSolid;
        this.getChildren().add(multiSolid);
        multiSolid.getTransformManipulator().updateOrigin();
    }

    public LOD1MultiSolidView getLOD1MultiSolid() {
        return this.lod1MultiSolid;
    }

    public void setLOD2MultiSolid(LOD2MultiSolidView multiSolid) {
        if (multiSolid == null)
            return;

        if (this.lod2MultiSolid != null) {
            this.getChildren().remove(this.lod2MultiSolid);
        }
        this.lod2MultiSolid = multiSolid;
        this.getChildren().add(multiSolid);
        multiSolid.getTransformManipulator().updateOrigin();
    }

    public LOD2MultiSolidView getLOD2MultiSolid() {
        return this.lod2MultiSolid;
    }

    public void setLOD3MultiSolid(LOD3MultiSolidView multiSolid) {
        if (multiSolid == null)
            return;

        if (this.lod3MultiSolid != null) {
            this.getChildren().remove(this.lod3MultiSolid);
        }
        this.lod3MultiSolid = multiSolid;
        this.getChildren().add(multiSolid);
        multiSolid.getTransformManipulator().updateOrigin();
    }

    public LOD3MultiSolidView getLOD3MultiSolid() {
        return this.lod3MultiSolid;
    }

    public void setLOD2MultiSurface(LOD2MultiSurfaceView multiSurface) {
        if (multiSurface == null)
            return;

        if (this.lod2MultiSurface != null) {
            this.getChildren().remove(this.lod2MultiSurface);
        }
        this.lod2MultiSurface = multiSurface;
        this.getChildren().add(multiSurface);
        multiSurface.getTransformManipulator().updateOrigin();
    }

    public LOD2MultiSurfaceView getLOD2MultiSurface() {
        return this.lod2MultiSurface;
    }

    public void setLOD3MultiSurface(LOD3MultiSurfaceView multiSurface) {
        if (multiSurface == null)
            return;

        if (this.lod3MultiSurface != null) {
            this.getChildren().remove(this.lod3MultiSurface);
        }
        this.lod3MultiSurface = multiSurface;
        this.getChildren().add(multiSurface);
        multiSurface.getTransformManipulator().updateOrigin();
    }

    public LOD3MultiSurfaceView getLOD3MultiSurface() {
        return this.lod3MultiSurface;
    }
}
