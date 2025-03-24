package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.transportation.TrafficArea;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.citymodel.GMLView;
import org.plateaubuilder.core.editor.surfacetype.MultiSurfaceTypeView;
import org.plateaubuilder.core.editor.transform.TransformManipulator;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

import javafx.scene.shape.MeshView;

public class TrafficAreaView extends GMLView<TrafficArea> implements ILODMultiSurfaceView {
    private List<PolygonView> polygons = new ArrayList<>();
    private AbstractMultiSurfaceMeshView multiSurfaceMeshView;

    public TrafficAreaView(TrafficArea gml) {
        super(gml);
    }

    public List<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(List<PolygonView> polygons) {
        this.polygons = polygons;
    }

    public void setMultiSurfaceMeshView(AbstractMultiSurfaceMeshView multiSurfaceMeshView) {
        this.multiSurfaceMeshView = multiSurfaceMeshView;
    }

    public AbstractMultiSurfaceMeshView getMultiSurfaceMeshView() {
        return multiSurfaceMeshView;
    }

    @Override
    public MeshView getMeshView() {
        return multiSurfaceMeshView.getMeshView();
    }

    @Override
    public VertexBuffer getVertexBuffer() {
        return multiSurfaceMeshView.getVertexBuffer();
    }

    @Override
    public TexCoordBuffer getTexCoordBuffer() {
        return multiSurfaceMeshView.getTexCoordBuffer();
    }

    @Override
    public void reflectGML() {
        multiSurfaceMeshView.reflectGML();
    }

    @Override
    public TransformManipulator getTransformManipulator() {
        return multiSurfaceMeshView.getTransformManipulator();
    }

    @Override
    public MultiSurface getGmlObject() {
        return multiSurfaceMeshView.getGmlObject();
    }

    @Override
    public MultiSurfaceTypeView getSurfaceTypeView() {
        return multiSurfaceMeshView.getSurfaceTypeView();
    }
}
