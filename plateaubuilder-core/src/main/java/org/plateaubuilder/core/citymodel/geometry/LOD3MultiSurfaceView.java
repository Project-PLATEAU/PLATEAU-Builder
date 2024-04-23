package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.editor.surfacetype.TrafficAreaSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD3MultiSurfaceView extends AbstractMultiSurfaceMeshView {
    private List<TrafficAreaView> trafficAreaViews;

    public LOD3MultiSurfaceView(MultiSurface gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLOD() {
        return 3;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TrafficAreaSurfaceTypeView createSurfaceTypeView() {
        return new TrafficAreaSurfaceTypeView(getLOD());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PolygonView> getPolygons() {
        var polygons = new ArrayList<PolygonView>();

        for (var trafficAreaView : trafficAreaViews) {
            polygons.addAll(trafficAreaView.getPolygons());
        }

        return polygons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reflectGML() {
        // TODO: Implement this method
    }
}
