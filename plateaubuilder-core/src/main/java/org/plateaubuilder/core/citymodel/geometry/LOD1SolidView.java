package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;

import org.citygml4j.model.gml.geometry.primitives.AbstractSolid;
import org.plateaubuilder.core.editor.surfacetype.BuildingSurfaceTypeView;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD1SolidView extends AbstractLODSolidMeshView {
    private ArrayList<PolygonView> polygons;

    public LOD1SolidView(AbstractSolid gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    @Override
    public int getLOD() {
        return 1;
    }

    @Override
    protected BuildingSurfaceTypeView createSurfaceTypeView() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<PolygonView> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<PolygonView> polygons) {
        this.polygons = polygons;
    }
}
