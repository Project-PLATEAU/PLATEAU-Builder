package org.plateau.citygmleditor.citymodel.geometry;

import java.util.ArrayList;
import java.util.HashMap;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;

import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;

public class LOD2SolidView extends Parent implements ILODSolidView {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurfaceView> boundaries;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();

    public LOD2SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
    }

    public AbstractSolid getGmlObject() {
        return gmlObject;
    }

    public void setGmlObject(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    public ArrayList<BoundarySurfaceView> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(ArrayList<BoundarySurfaceView> boundaries) {
        this.boundaries = boundaries;
    }

    public HashMap<SurfaceDataView, ArrayList<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, ArrayList<PolygonView>>();

        for (var boundary : boundaries) {
            for (var polygon : boundary.getPolygons()) {
                map.computeIfAbsent(polygon.getSurfaceData(), k -> new ArrayList<>());
                map.get(polygon.getSurfaceData()).add(polygon);
            }
        }

        return map;
    }

    public void addMeshView(MeshView meshView) {
        getChildren().add(meshView);
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<PolygonView> getPolygons() {
        var polygons = new ArrayList<PolygonView>();

        for (var boundary : boundaries) {
            polygons.addAll(boundary.getPolygons());
        }

        return polygons;
    }

    @Override
    public VertexBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    @Override
    public TexCoordBuffer getTexCoordBuffer() {
        return this.texCoordBuffer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractSolid getAbstractSolid() {
        return gmlObject;
    }
}
