package org.plateaubuilder.core.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractOpening;
import org.plateaubuilder.core.citymodel.GMLView;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;

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
