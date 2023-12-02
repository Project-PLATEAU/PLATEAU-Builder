package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;

import java.util.ArrayList;

public class BoundarySurface {
    private AbstractBoundarySurface gmlObject;
    private ArrayList<Polygon> polygons;

    public BoundarySurface(AbstractBoundarySurface gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public String getId() {
        return gmlObject.getId();
    }
}
