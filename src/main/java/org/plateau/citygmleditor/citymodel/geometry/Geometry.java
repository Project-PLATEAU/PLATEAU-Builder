package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.gml.geometry.AbstractGeometry;

import java.util.ArrayList;

public class Geometry {
    private AbstractGeometry gmlObject;
    private ArrayList<Polygon> polygons;

    public Geometry(AbstractGeometry gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }
}
