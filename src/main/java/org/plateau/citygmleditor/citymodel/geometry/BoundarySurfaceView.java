package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.plateau.citygmleditor.citymodel.GMLObjectView;

import java.util.ArrayList;

public class BoundarySurfaceView extends GMLObjectView<AbstractBoundarySurface> {
    private ArrayList<PolygonView> polygons;
    private ArrayList<PolygonView> openingPolygons;

    public BoundarySurfaceView(AbstractBoundarySurface original) {
        super(original);
    }

    public ArrayList<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }

    public ArrayList<PolygonView> getOpeningPolygons() {
        return openingPolygons;
    }

    public void setOpeningPolygons(ArrayList<PolygonView> polygons) {
        this.openingPolygons = polygons;
    }

    /**
     * Get the id of the {@link AbstractBoundarySurface}
     * @return the id
     */
    public String getId() {
        return getOriginal().getId();
    }
}
