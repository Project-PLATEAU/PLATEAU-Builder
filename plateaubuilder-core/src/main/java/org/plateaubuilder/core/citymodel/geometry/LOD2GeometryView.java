package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD2GeometryView extends AbstractLODGeometryMeshView {
    public LOD2GeometryView(AbstractGeometry gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, 2, vertexBuffer, texCoordBuffer);
    }
}
