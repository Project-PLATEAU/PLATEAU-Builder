package org.plateaubuilder.core.citymodel.geometry;

import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.utils3d.polygonmesh.TexCoordBuffer;
import org.plateaubuilder.core.utils3d.polygonmesh.VertexBuffer;

public class LOD2RoadMultiSurfaceView extends LOD2MultiSurfaceView {
    private LOD2MultiSurfaceView lOD2MultiSurfaceView;
    private List<TrafficAreaView> trafficAreas;
    private List<AuxiliaryTrafficAreaView> auxiliaryTrafficAreas;

    public LOD2RoadMultiSurfaceView(MultiSurface gmlObject, VertexBuffer vertexBuffer, TexCoordBuffer texCoordBuffer) {
        super(gmlObject, vertexBuffer, texCoordBuffer);
    }

    public void setLOD2MultiSurfaceView(LOD2MultiSurfaceView lOD2MultiSurfaceView) {
        this.lOD2MultiSurfaceView = lOD2MultiSurfaceView;
    }

    public LOD2MultiSurfaceView getLOD2MultiSurfaceView() {
        return lOD2MultiSurfaceView;
    }

    public void setTrafficAreas(List<TrafficAreaView> trafficAreas) {
        this.trafficAreas = trafficAreas;
    }

    public List<TrafficAreaView> getTrafficAreas() {
        return trafficAreas;
    }

    public void setAuxiliaryTrafficAreas(List<AuxiliaryTrafficAreaView> auxiliaryTrafficAreas) {
        this.auxiliaryTrafficAreas = auxiliaryTrafficAreas;
    }

    public List<AuxiliaryTrafficAreaView> getAuxiliaryTrafficAreas() {
        return auxiliaryTrafficAreas;
    }
}
