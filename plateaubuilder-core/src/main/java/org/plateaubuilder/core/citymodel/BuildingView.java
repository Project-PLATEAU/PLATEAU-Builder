package org.plateaubuilder.core.citymodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.Envelope;
import org.citygml4j.model.gml.measures.Length;
import org.plateaubuilder.core.citymodel.geometry.AbstractLODSolidMeshView;
import org.plateaubuilder.core.citymodel.geometry.ILODSolidView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.citymodel.geometry.LOD1SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD2SolidView;
import org.plateaubuilder.core.citymodel.geometry.LOD3SolidView;
import org.plateaubuilder.core.editor.Editor;

import javafx.scene.Node;

public class BuildingView extends ManagedGMLView<AbstractBuilding> implements IFeatureView {
    private LOD1SolidView lod1Solid;
    private LOD2SolidView lod2Solid;
    private LOD3SolidView lod3Solid;

    private List<BuildingInstallationView> buildingInstallationViews = new ArrayList<>();

    public BuildingView(AbstractBuilding original) {
        super(original);

        Editor.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int)newValue);
        });
    }

    public void toggleLODView(int lod) {
        var meshViews = new AbstractLODSolidMeshView[] {
                lod1Solid, lod2Solid, lod3Solid
        };
        for (int i = 1; i <= 3; ++i) {
            var meshView = meshViews[i - 1];
            if (meshView == null) {
                continue;
            }

            meshView.setVisible(lod == i);
        }

        // BuildingInstallation
        for (var buildingInstallationView : buildingInstallationViews) {
            for (int i = 2; i <= 3; i++) {
                var geometryView = buildingInstallationView.getGeometryView(i);
                if (geometryView != null) {
                    geometryView.setVisible(i == lod);
                }
            }
        }
    }

    public void setDefaultVisible() {
        if (this.lod3Solid != null) {
            this.lod3Solid.setVisible(true);
            if (this.lod2Solid != null) {
                this.lod2Solid.setVisible(false);
            }
            if (this.lod1Solid != null) {
                this.lod1Solid.setVisible(false);
            }
        } else if (this.lod2Solid != null) {
            this.lod2Solid.setVisible(true);
            if (this.lod1Solid != null) {
                this.lod1Solid.setVisible(false);
            }
        } else if (this.lod1Solid != null) {
            this.lod1Solid.setVisible(true);
        }
    }

    public Node getNode() {
        return this;
    }

    public ILODView getLODView(int lod) {
        return getSolid(lod);
    }

    public void setLODView(int lod, ILODView lodView) {
        switch (lod) {
        case 1:
            setLOD1Solid((LOD1SolidView) lodView);
            break;
        case 2:
            setLOD2Solid((LOD2SolidView) lodView);
            break;
        case 3:
            setLOD3Solid((LOD3SolidView) lodView);
            break;
        default:
            break;
        }
    }

    public ILODSolidView getSolid(int lod) {
        switch (lod) {
        case 1:
            return lod1Solid;
        case 2:
            return lod2Solid;
        case 3:
            return lod3Solid;
        default:
            return null;
        }
    }

    public void setLOD1Solid(LOD1SolidView solid) {
        if (this.lod1Solid == null) {
            this.getChildren().remove(this.lod1Solid);
        }
        this.lod1Solid = solid;
        this.getChildren().add(solid);
        solid.getTransformManipulator().updateOrigin();
    }

    public LOD1SolidView getLOD1Solid() {
        return this.lod1Solid;
    }

    public void setLOD2Solid(LOD2SolidView solid) {
        if (solid == null)
            return;

        if (this.lod2Solid != null) {
            this.getChildren().remove(this.lod2Solid);
        }
        this.lod2Solid = solid;
        this.getChildren().add(solid);
        solid.getTransformManipulator().updateOrigin();
    }

    public LOD2SolidView getLOD2Solid() {
        return this.lod2Solid;
    }

    public void setLOD3Solid(LOD3SolidView solid) {
        if (solid == null)
            return;

        if (this.lod3Solid != null) {
            this.getChildren().remove(this.lod3Solid);
        }
        this.lod3Solid = solid;
        this.getChildren().add(solid);
        solid.getTransformManipulator().updateOrigin();
    }

    public LOD3SolidView getLOD3Solid() {
        return this.lod3Solid;
    }

    public void addBuildingInstallationView(BuildingInstallationView buildingInstallationView) {
        if(buildingInstallationView == null)
            return;

        this.buildingInstallationViews.add(buildingInstallationView);
        this.getChildren().add(buildingInstallationView);
    }

    public void addBuildingPart(BuildingView buildingPart) {
        if (buildingPart == null)
            return;

        this.getChildren().add(buildingPart);
    }

    public Envelope getEnvelope() {
        return this.getGML().getBoundedBy().getEnvelope();
    }

    @Override
    public boolean isSetMeasuredHeight() {
        return getGML().isSetMeasuredHeight();
    }

    @Override
    public Length getMeasuredHeight() {
        return getGML().getMeasuredHeight();
    }

    @Override
    public void setMeasuredHeight(Length length) {
        getGML().setMeasuredHeight(length);
    }

    @Override
    public void unsetMeasuredHeight() {
        getGML().unsetMeasuredHeight();
    }

    @Override
    public List<String> getTexturePaths() {
        var paths = new HashSet<String>();
        if (lod2Solid != null) {
            paths.addAll(lod2Solid.getTexturePaths());
        }
        if (lod3Solid != null) {
            paths.addAll(lod3Solid.getTexturePaths());
        }
        for (var buildingInstallationView : buildingInstallationViews) {
            for (int i = 2; i <= 3; i++) {
                var geometryView = buildingInstallationView.getGeometryView(i);
                if (geometryView != null) {
                    paths.addAll(geometryView.getTexturePaths());
                }
            }
        }
        return new ArrayList<String>(paths);
    }
}
