package org.plateau.citygmleditor.control;

import org.plateau.citygmleditor.citymodel.geometry.PolygonView;

public class SurfacePolygonSection {
    public int start;
    public int end;
    public PolygonView polygon;

    public SurfacePolygonSection(int start, int end, PolygonView polygon) {
        this.start = start;
        this.end = end;
        this.polygon = polygon;
    }
}
