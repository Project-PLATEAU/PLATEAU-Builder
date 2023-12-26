package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;

import java.util.ArrayList;

public class BoundarySurfaceView {
    private AbstractBoundarySurface gmlObject;
    private ArrayList<PolygonView> polygons;

    public BoundarySurfaceView(AbstractBoundarySurface gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }

    /**
     * Get the id of the {@link AbstractBoundarySurface}
     * @return the id
     */
    public String getId() {
        return gmlObject.getId();
    }
}