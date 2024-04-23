package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.editor.surfacetype.TrafficAreaSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

abstract public class AbstractMultiSurfaceMeshView extends AbstractLODMeshView<MultiSurface, TrafficAreaSurfaceTypeView>
        implements ILODMultiSurfaceView {

    public AbstractMultiSurfaceMeshView(MultiSurface gmlObject, VertexBuffer vertexBuffer,
            TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }
}
