package org.plateaubuilder.core.citymodel.geometry;

import java.util.HashMap;
import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.citymodel.SurfaceDataView;
import org.plateaubuilder.core.editor.surfacetype.MultiSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD1MultiSurfaceView extends AbstractMultiSurfaceMeshView {
    public LOD1MultiSurfaceView(MultiSurface gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, 1, vertexBuffer, texCoordBuffer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MultiSurfaceTypeView createSurfaceTypeView() {
        return null;
    }

    @Override
    public HashMap<SurfaceDataView, List<PolygonView>> getSurfaceDataPolygonsMap() {
        throw new UnsupportedOperationException("Unimplemented method 'getSurfaceDataPolygonsMap'");
    }
}
