package org.plateaubuilder.core.citymodel;

import java.time.ZonedDateTime;
import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.measures.Length;
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

    public String getFeatureType() {
        return "veg:PlantCover";
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

    @Override
    public List<ADEComponent> getADEComponents() {
        return getGML().getGenericApplicationPropertyOfPlantCover();
    }

    @Override
    public Object getAttribute(String[] attributePaths) {
        var names = attributePaths[0].split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
        var name = names[1];
        var gml = getGML();
        switch (name) {
        case "class":
            return gml.getClazz();
        case "function":
            var functionList = gml.getFunction();
            return functionList.size() == 0 ? null : functionList.get(0);
        case "usage":
            var usageList = gml.getUsage();
            return usageList.size() == 0 ? null : usageList.get(0);
        case "averageHeight":
            return gml.getAverageHeight();
        case "creationDate":
            return gml.getCreationDate();
        case "terminationDate":
            return gml.getTerminationDate();
        default:
            var attributeIndex = name.split("-");
            if (attributeIndex.length == 2) {
                return getAttribute(attributeIndex[0], Integer.parseInt(attributeIndex[1]));
            }
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }

    private Object getAttribute(String name, int index /* 1-based */) {
        var gml = getGML();
        switch (name) {
        case "function":
            var functionList = gml.getFunction();
            return functionList.size() < index ? null : functionList.get(index - 1);
        case "usage":
            var usageList = gml.getUsage();
            return usageList.size() < index ? null : usageList.get(index - 1);
        default:
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }

    @Override
    public void setAttribute(String[] attributePaths, Object value) {
        var names = attributePaths[0].split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
        var name = names[1];
        var gml = getGML();
        switch (name) {
        case "class":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setClazz((Code) value);
            break;
        case "function":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var functionList = gml.getFunction();
            if (functionList.size() > 0) {
                functionList.remove(0);
            }
            functionList.add(0, (Code) value);
            break;
        case "usage":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var usageList = gml.getUsage();
            if (usageList.size() > 0) {
                usageList.remove(0);
            }
            usageList.add(0, (Code) value);
            break;
        case "averageHeight":
            if (!(value instanceof Length)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setAverageHeight((Length) value);
            break;
        case "creationDate":
            if (!(value instanceof ZonedDateTime)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setCreationDate((ZonedDateTime) value);
            break;
        case "terminationDate":
            if (!(value instanceof ZonedDateTime)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            gml.setTerminationDate((ZonedDateTime) value);
            break;
        default:
            var attributeIndex = name.split("-");
            if (attributeIndex.length == 2) {
                setAttribute(attributeIndex[0], Integer.parseInt(attributeIndex[1]), value);
            }
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }

    private void setAttribute(String name, int index /* 1-based */, Object value) {
        var gml = getGML();
        switch (name) {
        case "function":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var functionList = gml.getFunction();
            if (functionList.size() < index) {
                functionList.remove(index - 1);
            }
            functionList.add(index - 1, (Code) value);
            break;
        case "usage":
            if (!(value instanceof Code)) {
                throw new IllegalArgumentException("Invalid value type: " + value.getClass().getName());
            }
            var usageList = gml.getUsage();
            if (usageList.size() < index) {
                usageList.remove(index - 1);
            }
            usageList.add(index - 1, (Code) value);
            break;
        default:
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }

    @Override
    public boolean isSetAttribute(String[] attributePaths) {
        var names = attributePaths[0].split(":");
        if (names.length != 2) {
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
        var name = names[1];
        var gml = getGML();
        switch (name) {
        case "class":
            return gml.isSetClazz();
        case "function":
            return gml.isSetFunction();
        case "usage":
            return gml.isSetUsage();
        case "averageHeight":
            return gml.isSetAverageHeight();
        case "creationDate":
            return gml.isSetCreationDate();
        case "terminationDate":
            return gml.isSetTerminationDate();
        default:
            throw new IllegalArgumentException("Invalid name: " + attributePaths[0]);
        }
    }
}
