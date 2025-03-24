package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.editor.surfacetype.BuildingSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD2SolidView extends AbstractLODSolidMeshView {
    private List<BoundarySurfaceView> boundaries;

    public LOD2SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, 2, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BuildingSurfaceTypeView createSurfaceTypeView() {
        return new BuildingSurfaceTypeView(getLOD());
    }

    public List<BoundarySurfaceView> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(List<BoundarySurfaceView> boundaries) {
        this.boundaries = boundaries;
    }

    public void addSurfaceTypeView(AbstractBuilding building) {
        var surfaceTypeView = getSurfaceTypeView();
        getChildren().add(surfaceTypeView);
        surfaceTypeView.setTarget(building, this);
        surfaceTypeView.updateVisual();
    }

    public HashMap<SurfaceDataView, List<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, List<PolygonView>>();

        for (var boundary : boundaries) {
            for (var polygon : boundary.getPolygons()) {
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

        for (var boundary : boundaries) {
            polygons.addAll(boundary.getPolygons());
        }

        return polygons;
    }
}
