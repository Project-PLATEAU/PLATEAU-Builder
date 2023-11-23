package org.plateau.citygmleditor.citymodel.geometry;

import javafx.scene.Parent;
import javafx.scene.shape.MeshView;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.Solid;

import java.util.ArrayList;

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
