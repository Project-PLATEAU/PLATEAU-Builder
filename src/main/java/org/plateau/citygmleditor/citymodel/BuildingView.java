package org.plateau.citygmleditor.citymodel;

import javafx.scene.Node;
import javafx.scene.Parent;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.*;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;
import org.plateau.citygmleditor.citymodel.geometry.LOD3SolidView;
import org.plateau.citygmleditor.control.BuildingSurfaceTypeView;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;
import java.util.ArrayList;
import java.util.List;

public class BuildingView extends Parent {
    private AbstractBuilding gmlObject;

    private LOD1SolidView lod1Solid;
    private LOD2SolidView lod2Solid;
    private LOD3SolidView lod3Solid;

    private List<BuildingInstallationView> buildingInstallationViews = new ArrayList<>();

    public BuildingView(AbstractBuilding gmlObject) {
        this.gmlObject = gmlObject;

        CityGMLEditorApp.getCityModelViewMode().lodProperty().addListener((observable, oldValue, newValue) -> {
            toggleLODView((int)newValue);
        });
    }

    public AbstractBuilding getGMLObject() {
        return this.gmlObject;
    }

    public void toggleLODView(int lod) {
        var solids = new ILODSolidView[] {
                lod1Solid, lod2Solid, lod3Solid
        };
        for (int i = 1; i <= 3; ++i) {
            var solid = solids[i - 1];
            if (solid == null)
                continue;

            ((Node)solid).setVisible(lod == i);
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

        if (this.lod2Solid == null) {
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

        if (this.lod3Solid == null) {
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
        buildingInstallationView.getTransformManipulator().updateOrigin();
    }

    public Envelope getEnvelope() {
        return this.gmlObject.getBoundedBy().getEnvelope();
    }
}
