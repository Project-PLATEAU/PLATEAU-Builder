package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateaubuilder.core.editor.surfacetype.BuildingSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

abstract public class AbstractLODSolidMeshView extends AbstractLODMeshView<AbstractSolid, BuildingSurfaceTypeView> implements ILODSolidView {

    public AbstractLODSolidMeshView(AbstractSolid gmlObject, int lod, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, lod, vertexBuffer, texCoordBuffer);
    }
}
