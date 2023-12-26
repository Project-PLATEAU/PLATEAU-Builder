package org.plateau.citygmleditor.citymodel.geometry;

import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.citygmleditor.citymodel.GMLObjectView;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class PolygonView extends GMLObjectView<Polygon> {
    private final FaceBuffer faceBuffer = new FaceBuffer();

    private LinearRingView exteriorRing;

    private List<LinearRingView> interiorRings = new ArrayList<>();

    public PolygonView(Polygon original) {
        super(original);
    }

    public LinearRingView getExteriorRing() {
        return this.exteriorRing;

    }

    public void setExteriorRing(LinearRingView exteriorRing) {
        this.exteriorRing = exteriorRing;
    }

    public void addInteriorRing(LinearRingView interiorRing) {
        this.interiorRings.add(interiorRing);
    }

    public List<LinearRingView> getInteriorRings() {
        return this.interiorRings;
    }

    public SurfaceDataView getSurfaceData() {
        // Polygon内のAppearanceはすべて同じであること前提

        if (this.exteriorRing == null)
            return null;

        return this.exteriorRing.getSurfaceData();
    }

    public FaceBuffer getFaceBuffer() {
        return faceBuffer;
    }
}
