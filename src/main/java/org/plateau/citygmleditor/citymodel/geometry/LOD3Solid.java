package org.plateau.citygmleditor.citymodel.geometry;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citymodel.SurfaceData;

import java.util.ArrayList;
import java.util.HashMap;

public class LOD3Solid extends Parent {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurface> boundaries;
    // private HashMap<String, ArrayList<Polygon>> group;
    private HashMap<String, Group> group;
    // TODO Solidに含めるべきではない？
    private ArrayList<Geometry> outerBuildingInstallations;

    public LOD3Solid(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public AbstractSolid getGmlObject() {
        return gmlObject;
    }

    public void setGmlObject(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<BoundarySurface> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(ArrayList<BoundarySurface> boundaries) {
        this.boundaries = boundaries;
    }

    public ArrayList<Geometry> getOuterBuildingInstallations() {
        return outerBuildingInstallations;
    }

    public void setOuterBuildingInstallations(ArrayList<Geometry> outerBuildingInstallations) {
        this.outerBuildingInstallations = outerBuildingInstallations;
    }

    public HashMap<SurfaceData, ArrayList<Polygon>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceData, ArrayList<Polygon>>();

        for (var boundary : boundaries) {
            for (var polygon : boundary.getPolygons()) {
                map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                map.get(polygon.getSurfaceData()).add(polygon);
            }
        }

        return map;
    }

    public void addMeshView(MeshView meshView) {
        //getChildren().add(meshView);
        addMeshView("1", meshView);
    }

    public void addMeshView(String id, MeshView meshView) {
        if (group == null)
            group = new HashMap<String, Group>();
        group.computeIfAbsent(id, k -> new Group());
        if (group.get(id).getChildren().isEmpty()) {
            getChildren().add(group.get(id));
            group.get(id).setId(id);
        }
        group.get(id).getChildren().add(meshView);
    }
}
