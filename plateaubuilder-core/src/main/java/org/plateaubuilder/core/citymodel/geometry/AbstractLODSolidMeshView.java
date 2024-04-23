package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateaubuilder.core.editor.surfacetype.BuildingSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

abstract public class AbstractLODSolidMeshView extends AbstractLODMeshView<AbstractSolid, BuildingSurfaceTypeView> implements ILODSolidView {
    public AbstractLODSolidMeshView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reflectGML() {
        var transformManipulator = getTransformManipulator();
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
        var vertices = transformManipulator.unprojectVertexTransforms(getVertexBuffer().getVertices());
        for (var vertex : vertices) {
            vertexBuffer.addVertex(vertex);
        }
        setVertexBuffer(vertexBuffer);
    }
}
