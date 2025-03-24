package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.gml.base.AbstractGML;
import org.plateaubuilder.core.citymodel.geometry.AbstractLODGeometryMeshView;
import org.plateaubuilder.core.citymodel.geometry.ILODGeometryView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.LOD1GeometryView;
import org.plateaubuilder.core.citymodel.geometry.LOD2GeometryView;
import org.plateaubuilder.core.citymodel.geometry.LOD3GeometryView;
import org.plateaubuilder.core.editor.Editor;

import javafx.scene.Node;

abstract public class AbstractGeometryView<T extends AbstractGML> extends ManagedGMLView<T> {
    private LOD1GeometryView lod1GeometryView;
    private LOD2GeometryView lod2GeometryView;
    private LOD3GeometryView lod3GeometryView;

    public AbstractGeometryView(T gml) {
        super(gml);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int) newValue);
        });
    }

    public void toggleLODView(int lod) {
        var meshViews = new AbstractLODGeometryMeshView[] { lod1GeometryView, lod2GeometryView, lod3GeometryView };
        for (int i = 1; i <= 3; ++i) {
            var meshView = meshViews[i - 1];
            if (meshView == null) {
                continue;
            }

            meshView.setVisible(lod == i);
        }
    }

    public void setDefaultVisible() {
        if (this.lod3GeometryView != null) {
            this.lod3GeometryView.setVisible(true);
            if (this.lod2GeometryView != null) {
                this.lod2GeometryView.setVisible(false);
            }
            if (this.lod1GeometryView != null) {
                this.lod1GeometryView.setVisible(false);
            }
        } else if (this.lod2GeometryView != null) {
            this.lod2GeometryView.setVisible(true);
            if (this.lod1GeometryView != null) {
                this.lod1GeometryView.setVisible(false);
            }
        } else if (this.lod1GeometryView != null) {
            this.lod1GeometryView.setVisible(true);
        }
    }

    public Node getNode() {
        return this;
    }

    public ILODView getLODView(int lod) {
        return getGeometry(lod);
    }

    public void setLODView(int lod, ILODView lodView) {
        if (lodView instanceof LOD1GeometryView) {
            setLOD1Geometry((LOD1GeometryView) lodView);
        } else if (lodView instanceof LOD2GeometryView) {
            setLOD2Geometry((LOD2GeometryView) lodView);
        } else if (lodView instanceof LOD3GeometryView) {
            setLOD3Geometry((LOD3GeometryView) lodView);
        }
    }

    public ILODGeometryView getGeometry(int lod) {
        switch (lod) {
        case 1:
            return lod1GeometryView;
        case 2:
            return lod2GeometryView;
        case 3:
            return lod3GeometryView;
        default:
            return null;
        }
    }

    public void setLOD1Geometry(LOD1GeometryView geometry) {
        if (this.lod1GeometryView == null) {
            this.getChildren().remove(this.lod1GeometryView);
        }
        this.lod1GeometryView = geometry;
        this.getChildren().add(geometry);
        geometry.getTransformManipulator().updateOrigin();
    }

    public LOD1GeometryView getLOD1Geometry() {
        return this.lod1GeometryView;
    }

    public void setLOD2Geometry(LOD2GeometryView geometry) {
        if (geometry == null)
            return;

        if (this.lod2GeometryView != null) {
            this.getChildren().remove(this.lod2GeometryView);
        }
        this.lod2GeometryView = geometry;
        this.getChildren().add(geometry);
        geometry.getTransformManipulator().updateOrigin();
    }

    public LOD2GeometryView getLOD2Geometry() {
        return this.lod2GeometryView;
    }

    public void setLOD3Geometry(LOD3GeometryView geometry) {
        if (geometry == null)
            return;

        if (this.lod3GeometryView != null) {
            this.getChildren().remove(this.lod3GeometryView);
        }
        this.lod3GeometryView = geometry;
        this.getChildren().add(geometry);
        geometry.getTransformManipulator().updateOrigin();
    }

    public LOD3GeometryView getLOD3Geometry() {
        return this.lod3GeometryView;
    }
}
