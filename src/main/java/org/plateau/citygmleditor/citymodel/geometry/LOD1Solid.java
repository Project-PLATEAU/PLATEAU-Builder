package org.plateau.citygmleditor.citymodel.geometry;

import java.util.ArrayList;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.Solid;

import javafx.scene.shape.MeshView;

public class LOD1Solid extends MeshView implements ILODSolid {
    private Solid gmlObject;
    private ArrayList<Polygon> polygons;

    public LOD1Solid(Solid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    @Override
    public AbstractSolid getAbstractSolid() {
        return gmlObject;
    }
}
