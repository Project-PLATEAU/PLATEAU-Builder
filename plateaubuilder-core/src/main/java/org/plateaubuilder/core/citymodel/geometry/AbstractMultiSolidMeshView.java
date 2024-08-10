package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.editor.surfacetype.MultiSolidTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

abstract public class AbstractMultiSolidMeshView extends AbstractLODMeshView<MultiSolid, MultiSolidTypeView> implements ILODMultiSolidView {
    private List<PolygonView> polygons = new ArrayList<>();

    public AbstractMultiSolidMeshView(MultiSolid gmlObject, int lod, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
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
    protected MultiSolidTypeView createSurfaceTypeView() {
        return new MultiSolidTypeView(getLOD());
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
}
