package org.plateau.plateaubuilder.citymodel.geometry;

import javafx.scene.Parent;
import javafx.scene.shape.MeshView;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.control.transform.TransformManipulator;
import org.plateau.plateaubuilder.citymodel.SurfaceDataView;
import org.plateau.plateaubuilder.control.surfacetype.BuildingSurfaceTypeView;
import org.plateau.plateaubuilder.utils3d.polygonmesh.TexCoordBuffer;
import org.plateau.plateaubuilder.utils3d.polygonmesh.VertexBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LOD3SolidView extends Parent implements ILODSolidView {
    private AbstractSolid gmlObject;
    private ArrayList<BoundarySurfaceView> boundaries;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);
    private final List<MeshView> meshViews = new ArrayList<>();
    private final BuildingSurfaceTypeView surfaceTypeView = new BuildingSurfaceTypeView(3);

    public LOD3SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;

        toggleSurfaceView(PLATEAUBuilderApp.getCityModelViewMode().isSurfaceViewMode());

        PLATEAUBuilderApp.getCityModelViewMode().isSurfaceViewModeProperty().addListener((observable, oldValue, newValue) -> {
            toggleSurfaceView(newValue);
        });
    }

    private void toggleSurfaceView(boolean isVisible) {
        for (var meshView : meshViews) {
            meshView.setVisible(!isVisible);
        }
        surfaceTypeView.setVisible(isVisible);
    }

    public AbstractSolid getGmlObject() {
        return gmlObject;
    }

    public void setGmlObject(AbstractSolid gmlObject) {
        this.gmlObject = gmlObject;
    }

    @Override public List<BoundarySurfaceView> getBoundaries() {
        return boundaries;
    }

    public void setBoundaries(ArrayList<BoundarySurfaceView> boundaries) {
        this.boundaries = boundaries;
    }

    public HashMap<SurfaceDataView, ArrayList<PolygonView>> getSurfaceDataPolygonsMap() {
        var map = new HashMap<SurfaceDataView, ArrayList<PolygonView>>();

        for (var boundary : boundaries) {
            if (boundary.getPolygons() != null)
            {
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
        getChildren().add(surfaceTypeView);
        surfaceTypeView.setTarget(building, this);
        surfaceTypeView.updateVisual();
    }

    public void addMeshView(MeshView meshView) {
        getChildren().add(meshView);
        meshViews.add(meshView);
    }

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

    @Override
    public VertexBuffer getVertexBuffer() {
        return this.vertexBuffer;
    }

    @Override
    public MeshView getMeshView() {
        return meshViews.isEmpty() ? null : meshViews.get(0);
    }

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    @Override
    public TexCoordBuffer getTexCoordBuffer() {
        return this.texCoordBuffer;
    }

    @Override
    public BuildingSurfaceTypeView getSurfaceTypeView() {
        return surfaceTypeView;
    }

    @Override
    public AbstractSolid getAbstractSolid() {
        return gmlObject;
    }

    @Override
    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    @Override
    public void reflectGML() {
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();// .getOriginal().getPosList().toList3d();
            polygon.getExteriorRing().getGML().getPosList()
                    .setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();// .getOriginal().getPosList().toList3d();
                polygon.getExteriorRing().getGML().getPosList().setValue(
                        transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertexBuffer = new VertexBuffer();
        var vertices =
                transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }
}
