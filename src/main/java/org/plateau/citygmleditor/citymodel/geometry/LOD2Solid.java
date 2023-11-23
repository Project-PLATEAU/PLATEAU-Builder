package org.plateau.citygmleditor.citymodel.geometry;

import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citymodel.SurfaceData;

import java.util.ArrayList;
import java.util.HashMap;

public class LOD2Solid extends Parent implements ILODSolid {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurface> boundaries;

    public LOD2Solid(AbstractSolid gmlObject) {
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
        getChildren().add(meshView);
    }

    @Override
    public AbstractSolid getAbstractSolid() {
        return gmlObject;
    }
}
