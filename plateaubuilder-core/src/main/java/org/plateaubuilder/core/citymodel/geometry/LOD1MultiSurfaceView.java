package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.editor.surfacetype.TrafficAreaSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD1MultiSurfaceView extends AbstractMultiSurfaceMeshView {
    private ArrayList<PolygonView> polygons;

    public LOD1MultiSurfaceView(MultiSurface gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLOD() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TrafficAreaSurfaceTypeView createSurfaceTypeView() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }

    @Override
    public void reflectGML() {
        // TODO: Implement this method
    }
}
