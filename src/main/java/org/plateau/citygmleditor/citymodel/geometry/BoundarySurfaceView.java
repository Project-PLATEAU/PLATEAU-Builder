package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.plateau.citygmleditor.citymodel.GMLObjectView;
import org.plateau.citygmleditor.citymodel.factory.OpeningView;

import java.util.ArrayList;
import java.util.List;

public class BoundarySurfaceView extends GMLObjectView<AbstractBoundarySurface> {
    private List<PolygonView> polygons = new ArrayList<>();
    private List<OpeningView> openings = new ArrayList<>();

    public BoundarySurfaceView(AbstractBoundarySurface original) {
        super(original);
    }

    public List<PolygonView> getPolygons() {
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
        return getOriginal().getId();
    }

    public List<OpeningView> getOpenings() {
        return openings;
    }

    public void setOpenings(List<OpeningView> openings) {
        this.openings = openings;
    }
}
