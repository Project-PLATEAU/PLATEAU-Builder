package org.plateau.citygmleditor.citymodel.geometry;

import org.plateau.citygmleditor.citymodel.Appearance;
import org.plateau.citygmleditor.citymodel.GMLObject;
import org.plateau.citygmleditor.citymodel.SurfaceData;

public class LinearRing extends GMLObject<org.citygml4j.model.gml.geometry.primitives.LinearRing> {
    private float[] vertices;
    private float[] uvs;
    private SurfaceData surfaceData;

    public LinearRing(org.citygml4j.model.gml.geometry.primitives.LinearRing original) {
        super(original);
    }


    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public float[] getUVs() {
        return uvs;
    }

    public void setUVs(float[] uvs) {
        this.uvs = uvs;
    }

    public SurfaceData getSurfaceData() {
        return surfaceData;
    }

    public void setSurfaceData(SurfaceData surfaceData) {
        this.surfaceData = surfaceData;
    }
}
