package org.plateau.citygmleditor.citymodel.geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.citygmleditor.TransformManipulator;
import org.plateau.citygmleditor.citymodel.SurfaceDataView;

import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.plateau.citygmleditor.control.BuildingSurfaceTypeView;
import org.plateau.citygmleditor.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.citygmleditor.utils3d.polygonmesh.VertexBuffer;

public class LOD2SolidView extends Parent implements ILODSolidView {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurfaceView> boundaries;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);
    private List<MeshView> meshViews = new ArrayList<>();

    private final BuildingSurfaceTypeView surfaceTypeView = new BuildingSurfaceTypeView();

    public LOD2SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;

        toggleSurfaceView(CityGMLEditorApp.getCityModelViewMode().isSurfaceViewMode());

        CityGMLEditorApp.getCityModelViewMode().isSurfaceViewModeProperty().addListener((observable, oldValue, newValue) -> {
            toggleSurfaceView(newValue);
        });
    }

    private void toggleSurfaceView(boolean isVisible) {
        for (var meshView : meshViews) {
            meshView.setVisible(!isVisible);
        }
        surfaceTypeView.setVisible(isVisible);
    }

    public BuildingSurfaceTypeView getSurfaceTypeView() {
        return surfaceTypeView;
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

        // TODO: temp
        getChildren().add(surfaceTypeView);
        surfaceTypeView.setTarget(this);
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
        meshViews.add(meshView);
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

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
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

    @Override
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    @Override
    public void refrectGML() {
        for (var polygon : getPolygons()) {
                var coordinates = polygon.getExteriorRing().getOriginCoords();//.getOriginal().getPosList().toList3d();
                polygon.getExteriorRing().getOriginal().getPosList().setValue(transformManipulator.unprojectTransforms(coordinates));

                for (var interiorRing : polygon.getInteriorRings()) {
                    var coordinatesInteriorRing = interiorRing.getOriginCoords();//.getOriginal().getPosList().toList3d();
                    polygon.getExteriorRing().getOriginal().getPosList().setValue(transformManipulator.unprojectTransforms(coordinatesInteriorRing));
                }
            }
            var vertexBuffer = new VertexBuffer();
            var vertices = transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
            for(var vertex : vertices){
                vertexBuffer.addVertex(vertex);
            }
            setVertexBuffer(vertexBuffer);
    }
}
