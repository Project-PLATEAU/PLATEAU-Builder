package org.plateau.plateaubuilder.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractOpening;
import org.plateau.plateaubuilder.citymodel.GMLView;
import org.plateau.plateaubuilder.citymodel.geometry.PolygonView;

import java.util.ArrayList;
import java.util.List;

public class OpeningView extends GMLView<AbstractOpening> {
    private List<PolygonView> polygons = new ArrayList<>();

    public OpeningView(AbstractOpening original) {
        super(original);
    }

    public List<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(List<PolygonView> polygons) {
        this.polygons = polygons;
    }
}
