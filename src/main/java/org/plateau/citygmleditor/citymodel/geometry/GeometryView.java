package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.gml.geometry.AbstractGeometry;

import java.util.ArrayList;

public class GeometryView {
    private AbstractGeometry gmlObject;
    private ArrayList<PolygonView> polygons;

    public GeometryView(AbstractGeometry gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }
}
