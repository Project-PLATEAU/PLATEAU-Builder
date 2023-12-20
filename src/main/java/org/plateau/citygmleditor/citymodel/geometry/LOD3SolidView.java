package org.plateau.citygmleditor.citymodel.geometry;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;

import java.util.ArrayList;
import java.util.HashMap;

public class LOD3SolidView extends Parent {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurfaceView> boundaries;
    private HashMap<String, Group> group;

    public LOD3SolidView(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public AbstractSolid getGmlObject() {
        return gmlObject;
    }

    public void setGmlObject(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<BoundarySurfaceView> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(ArrayList<BoundarySurfaceView> boundaries) {
        this.boundaries = boundaries;
    }

    public HashMap<SurfaceDataView, ArrayList<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, ArrayList<PolygonView>>();

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
