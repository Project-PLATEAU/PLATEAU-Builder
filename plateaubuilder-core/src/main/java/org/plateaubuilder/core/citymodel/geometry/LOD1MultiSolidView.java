package org.plateaubuilder.core.citymodel.geometry;

import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.editor.surfacetype.MultiSolidTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD1MultiSolidView extends AbstractMultiSolidMeshView {
    public LOD1MultiSolidView(MultiSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, 1, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MultiSolidTypeView createSurfaceTypeView() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<SurfaceDataView, List<PolygonView>> getSurfaceDataPolygonsMap() {
        throw new UnsupportedOperationException("Unimplemented method 'getSurfaceDataPolygonsMap'");
    }
}
