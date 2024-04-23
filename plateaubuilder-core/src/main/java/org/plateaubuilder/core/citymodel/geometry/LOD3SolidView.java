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

public class LOD3SolidView extends AbstractLODSolidMeshView {
    private ArrayList<BoundarySurfaceView> boundaries;

    public LOD3SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
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
    protected BuildingSurfaceTypeView createSurfaceTypeView() {
        return new BuildingSurfaceTypeView(getLOD());
    }

    /**
     * {@inheritDoc}
     */
    @Override public List<BoundarySurfaceView> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(ArrayList<BoundarySurfaceView> boundaries) {
        this.boundaries = boundaries;
    }

    public HashMap<SurfaceDataView, ArrayList<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, ArrayList<PolygonView>>();

        for (var boundary : boundaries) {
            if (boundary.getPolygons() != null) {
                for (var polygon : boundary.getPolygons()) {
                    map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                    map.get(polygon.getSurfaceData()).add(polygon);
                }
            }
            if (boundary.getOpenings() != null) {
                for (var opening : boundary.getOpenings()) {
                    for (var polygon : opening.getPolygons()) {
                        map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                        map.get(polygon.getSurfaceData()).add(polygon);
                    }
                }
            }
        }

        return map;
    }

    public void addSurfaceTypeView(AbstractBuilding building) {
        var surfaceTypeView = getSurfaceTypeView();
        getChildren().add(surfaceTypeView);
        surfaceTypeView.setTarget(building, this);
        surfaceTypeView.updateVisual();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<PolygonView> getPolygons() {
        var polygons = new ArrayList<PolygonView>();

        for (var boundary : boundaries) {
            if (boundary.getPolygons() != null) {
                polygons.addAll(boundary.getPolygons());
            }
            if (boundary.getOpenings() != null) {
                for (var opening : boundary.getOpenings()) {
                    polygons.addAll(opening.getPolygons());
                }
            }
        }

        return polygons;
    }
}
