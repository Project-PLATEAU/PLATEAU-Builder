package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.editor.surfacetype.GeometrySurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

abstract public class AbstractLODGeometryMeshView extends AbstractLODMeshView<AbstractGeometry, GeometrySurfaceTypeView> implements ILODGeometryView {
    private List<PolygonView> polygons = new ArrayList<>();

    private String gmlType;

    private String surfaceType;

    public AbstractLODGeometryMeshView(AbstractGeometry gmlObject, int lod, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, lod, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PolygonView> getPolygons() {
        return polygons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GeometrySurfaceTypeView createSurfaceTypeView() {
        return new GeometrySurfaceTypeView(getLOD());
    }

    public void setPolygons(List<PolygonView> polygons) {
        this.polygons = polygons;
    }

    public void addPolygons(List<PolygonView> polygons) {
        this.polygons.addAll(polygons);
    }

    public void addSurfaceTypeView(AbstractCityObject cityObject) {
        var surfaceTypeView = getSurfaceTypeView();
        getChildren().add(surfaceTypeView);
        surfaceTypeView.setTarget(cityObject, this);
        surfaceTypeView.updateVisual();
    }

    public HashMap<SurfaceDataView, List<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, List<PolygonView>>();

        for (var polygon : polygons) {
            map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
            map.get(polygon.getSurfaceData()).add(polygon);
        }

        return map;
    }

    public String getGmlType() {
        return gmlType;
    }

    public void setGmlType(String gmlType) {
        this.gmlType = gmlType;
    }

    public String getSurfaceType() {
        return surfaceType;
    }

    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
    }
}
