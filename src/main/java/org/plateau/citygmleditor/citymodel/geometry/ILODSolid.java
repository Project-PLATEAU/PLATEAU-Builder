package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;

import javafx.scene.Parent;

public interface ILODSolid {
    public AbstractSolid getAbstractSolid();
    public Parent getParent();
}
