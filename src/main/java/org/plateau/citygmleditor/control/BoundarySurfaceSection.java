package org.plateau.citygmleditor.control;

import org.citygml4j.model.citygml.bridge.AbstractBoundarySurface;
import org.plateau.citygmleditor.citymodel.geometry.PolygonView;

import java.util.ArrayList;
import java.util.List;

/**
 * FaceBuffer内での１つのBoundarySurfaceに該当するセクションを表します。
 */
public class BoundarySurfaceSection {
    private AbstractBoundarySurface boundarySurface;
    private final List<PolygonSection> polygonSections = new ArrayList<>();

    public List<PolygonSection> getPolygonSections() {
        return polygonSections;
    }

    public AbstractBoundarySurface getBoundarySurface() {
        return boundarySurface;
    }

    public void setBoundarySurface(AbstractBoundarySurface boundarySurface) {
        this.boundarySurface = boundarySurface;
    }
}
