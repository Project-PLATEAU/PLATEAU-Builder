package org.plateau.citygmleditor.citymodel.factory;

import org.citygml4j.model.citygml.building.AbstractOpening;
import org.plateau.citygmleditor.citymodel.GMLObjectView;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;

import java.util.ArrayList;
import java.util.List;

public class OpeningView extends GMLObjectView<AbstractOpening> {
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
