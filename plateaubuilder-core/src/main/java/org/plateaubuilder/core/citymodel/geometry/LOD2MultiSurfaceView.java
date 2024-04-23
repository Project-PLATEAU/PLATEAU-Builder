package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.editor.surfacetype.TrafficAreaSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD2MultiSurfaceView extends AbstractMultiSurfaceMeshView {
    private List<TrafficAreaView> trafficAreaViews;

    public LOD2MultiSurfaceView(MultiSurface gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLOD() {
        return 2;
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
    public List<TrafficAreaView> getTrafficAreaViews() {
        return trafficAreaViews;
    }

    public void setTrafficAreaViews(List<TrafficAreaView> trafficAreaViews) {
        this.trafficAreaViews = trafficAreaViews;
    }

    public void addSurfaceTypeView(Road road) {
        var surfaceTypeView = getSurfaceTypeView();
        getChildren().add(getSurfaceTypeView());
        surfaceTypeView.setTarget(road, this);
        surfaceTypeView.updateVisual();
    }

    public HashMap<SurfaceDataView, List<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, List<PolygonView>>();

        for (var trafficAreaView : trafficAreaViews) {
            for (var polygon : trafficAreaView.getPolygons()) {
                map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                map.get(polygon.getSurfaceData()).add(polygon);
            }
        }

        return map;
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
