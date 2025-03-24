package org.plateaubuilder.core.citymodel.geometry;

import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD3RoadMultiSurfaceView extends LOD3MultiSurfaceView {
    private LOD3MultiSurfaceView lOD3MultiSurfaceView;
    private List<TrafficAreaView> trafficAreas;
    private List<AuxiliaryTrafficAreaView> auxiliaryTrafficAreas;

    public LOD3RoadMultiSurfaceView(MultiSurface gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    public void setLOD3MultiSurfaceView(LOD3MultiSurfaceView lOD3MultiSurfaceView) {
        this.lOD3MultiSurfaceView = lOD3MultiSurfaceView;
    }

    public LOD3MultiSurfaceView getLOD3MultiSurfaceView() {
        return lOD3MultiSurfaceView;
    }

    public void setTrafficAreas(List<TrafficAreaView> trafficAreas) {
        this.trafficAreas = trafficAreas;
    }

    public List<TrafficAreaView> getTrafficAreas() {
        return trafficAreas != null ? trafficAreas : List.of();
    }

    public void setAuxiliaryTrafficAreas(List<AuxiliaryTrafficAreaView> auxiliaryTrafficAreas) {
        this.auxiliaryTrafficAreas = auxiliaryTrafficAreas;
    }

    public List<AuxiliaryTrafficAreaView> getAuxiliaryTrafficAreas() {
        return auxiliaryTrafficAreas != null ? auxiliaryTrafficAreas : List.of();
    }
}
