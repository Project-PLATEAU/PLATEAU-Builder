package org.plateau.citygmleditor.citymodel;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.plateau.citygmleditor.citymodel.geometry.Geometry;

public class BuildingInstallationView extends Parent {
    private AbstractBuilding gmlObject;
    private Group lod3ParentGroup;
    private HashMap<String, Group> lod3Group;
    private ArrayList<Geometry> lod3OuterBuildingInstallations;

    public BuildingInstallationView(AbstractBuilding gmlObject) {
        this.gmlObject = gmlObject;
    }

    public AbstractBuilding getGMLObject() {
        return this.gmlObject;
    }

    public void setLod3OuterBuildingInstallations(ArrayList<Geometry> outerBuildingInstallations) {
        this.lod3OuterBuildingInstallations = outerBuildingInstallations;
    }

    ArrayList<Geometry> getLod3OuterBuildingInstallations() {
        return this.lod3OuterBuildingInstallations;
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
