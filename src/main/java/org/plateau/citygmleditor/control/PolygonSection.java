package org.plateau.citygmleditor.control;

import org.citygml4j.model.citygml.building.AbstractBoundarySurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateau.citygmleditor.utils3d.polygonmesh.FaceBuffer;

/**
 * FaceBuffer内での１つのポリゴンに該当するセクションを表します。
 */
public class PolygonSection {
    public int start;
    public int end;
    private AbstractBoundarySurface boundarySurface;
    private Polygon polygon;
    private FaceBuffer faceBuffer;

    public PolygonSection(int start, int end, AbstractBoundarySurface boundarySurface, Polygon polygon, FaceBuffer faceBuffer) {
        this.start = start;
        this.end = end;
        this.boundarySurface = boundarySurface;
        this.polygon = polygon;
        this.faceBuffer = faceBuffer;
    }

    public AbstractBoundarySurface getBoundarySurface() {
        return boundarySurface;
    }

    public void setBoundarySurface(AbstractBoundarySurface boundarySurface) {
        this.boundarySurface = boundarySurface;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public FaceBuffer getFaceBuffer() {
        return faceBuffer;
    }

    public void setFaceBuffer(FaceBuffer faceBuffer) {
        this.faceBuffer = faceBuffer;
    }
}
