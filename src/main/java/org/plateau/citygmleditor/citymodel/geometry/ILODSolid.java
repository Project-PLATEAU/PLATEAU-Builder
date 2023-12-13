package org.plateau.citygmleditor.citymodel.geometry;

import java.util.ArrayList;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;

import javafx.scene.Parent;

/**
 * Interface for LOD Solid
 */
public interface ILODSolid {
    /**
     * Get the AbstractSolid
     * @return the AbstractSolid
     */
    public AbstractSolid getAbstractSolid();

    /**
     * Get the parent
     * @return the parent
     */
    public Parent getParent();

    /**
     * Get the polygons
     * @return the polygons
     */
    public ArrayList<Polygon> getPolygons();
}
