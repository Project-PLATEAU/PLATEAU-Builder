package org.plateaubuilder.core.citymodel.geometry;

import javafx.scene.shape.MeshView;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.editor.transform.TransformManipulator;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

import java.util.ArrayList;
import java.util.List;

public class GeometryView extends MeshView {
    private AbstractGeometry gmlObject;
    private ArrayList<PolygonView> polygons;
    private VertexBuffer vertexBuffer = new VertexBuffer();
    private TexCoordBuffer texCoordBuffer = new TexCoordBuffer();
    private TransformManipulator transformManipulator = new TransformManipulator(this);

    public GeometryView(AbstractGeometry gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        this.gmlObject = gmlObject;
        this.vertexBuffer = vertexBuffer;
        this.texCoordBuffer = texCoordBuffer;
    }

    public ArrayList<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }

    public VertexBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public void setVertexBuffer(VertexBuffer vertexBuffer) {
        this.vertexBuffer = vertexBuffer;
    }

    public MeshView getMeshView() {
        return this;
    }

    public TexCoordBuffer getTexCoordBuffer() {
        return texCoordBuffer;
    }

    public TransformManipulator getTransformManipulator() {
        return transformManipulator;
    }

    /**
     * 使用しているテクスチャパス
     * @return
     */
    public List<String> getTexturePaths() {
        var parentNode = this.getParent();
        while (parentNode != null) {
            if (parentNode instanceof CityModelView)
                break;
            parentNode = parentNode.getParent();
        }
        var cityModelView = (CityModelView)parentNode;
        var ret = new ArrayList<String>();
        for (var polygon : getPolygons()) {
            if (polygon.getSurfaceData() == null)
                continue;
            var parameterizedTexture = (org.citygml4j.model.citygml.appearance.ParameterizedTexture) polygon.getSurfaceData().getGML();
            var imageRelativePath = java.nio.file.Paths.get(parameterizedTexture.getImageURI());
            var imageAbsolutePath = java.nio.file.Paths.get(cityModelView.getGmlPath()).getParent().resolve(imageRelativePath);
            if (!ret.contains(imageAbsolutePath.toString()))
                ret.add(imageAbsolutePath.toString());
        }
        return ret;
    }

    public void refrectGML() {
        for (var polygon : getPolygons()) {
            var coordinates = polygon.getExteriorRing().getOriginCoords();//.getOriginal().getPosList().toList3d();
            polygon.getExteriorRing().getGML().getPosList().setValue(transformManipulator.unprojectTransforms(coordinates));

            for (var interiorRing : polygon.getInteriorRings()) {
                var coordinatesInteriorRing = interiorRing.getOriginCoords();//.getOriginal().getPosList().toList3d();
                polygon.getExteriorRing().getGML().getPosList().setValue(transformManipulator.unprojectTransforms(coordinatesInteriorRing));
            }
        }
        var vertexBuffer = new VertexBuffer();
        var vertices = transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }
}
