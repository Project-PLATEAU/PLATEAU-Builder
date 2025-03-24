package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD2MultiSolidView extends AbstractMultiSolidMeshView {
    public LOD2MultiSolidView(MultiSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, 2, vertexBuffer, texCoordBuffer);
    }
}
