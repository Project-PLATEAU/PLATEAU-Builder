package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.plateaubuilder.core.citymodel.GMLView;

public class TrafficAreaView extends GMLView<TrafficArea> {
    private List<PolygonView> polygons = new ArrayList<>();

    public TrafficAreaView(TrafficArea gml) {
        super(gml);
    }

    public List<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }
}
