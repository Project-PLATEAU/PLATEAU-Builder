package org.plateau.plateaubuilder.citymodel.geometry;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.plateau.plateaubuilder.citymodel.GMLView;
import org.plateau.plateaubuilder.citymodel.factory.OpeningView;

import java.util.ArrayList;
import java.util.List;

public class BoundarySurfaceView extends GMLView<AbstractBoundarySurface> {
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

    public List<OpeningView> getOpenings() {
        return openings;
    }

    public void setOpenings(List<OpeningView> openings) {
        this.openings = openings;
    }
}
