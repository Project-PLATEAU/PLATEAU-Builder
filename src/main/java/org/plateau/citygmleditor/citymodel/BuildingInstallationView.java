package org.plateau.citygmleditor.citymodel;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.plateau.citygmleditor.citymodel.geometry.GeometryView;

public class BuildingInstallationView extends Parent {
    private BuildingInstallation gmlObject;
    private Group lod3ParentGroup;
    private HashMap<String, Group> lod3Group;
    private GeometryView lod3Geometry;

    public BuildingInstallationView(BuildingInstallation gmlObject) {
        this.gmlObject = gmlObject;
    }

    public BuildingInstallation getGMLObject() {
        return this.gmlObject;
    }

    public void setLod3Geometry(GeometryView geometry) {
        this.lod3Geometry = geometry;
    }

    GeometryView getLod3Geometry() {
        return this.lod3Geometry;
    }
    
    public void addLod3MeshView(String id, MeshView meshView) {
        if (lod3Group == null)
            lod3Group = new HashMap<String, Group>();
        lod3Group.computeIfAbsent(id, k -> new Group());
        if (lod3Group.get(id).getChildren().isEmpty()) {
            getChildren().add(lod3Group.get(id));
            lod3Group.get(id).setId(id);

            if (lod3ParentGroup == null) {
                lod3ParentGroup = new Group();
                lod3ParentGroup.setId("LOD3");
                getChildren().add(lod3ParentGroup);
            }
            lod3ParentGroup.getChildren().add(lod3Group.get(id));
        }
        lod3Group.get(id).getChildren().add(meshView);
    }
}
