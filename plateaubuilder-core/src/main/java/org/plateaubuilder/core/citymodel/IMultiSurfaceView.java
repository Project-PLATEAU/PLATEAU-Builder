package org.plateaubuilder.core.citymodel;

import org.plateaubuilder.core.citymodel.geometry.LOD1MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD2MultiSurfaceView;
import org.plateaubuilder.core.citymodel.geometry.LOD3MultiSurfaceView;

public interface IMultiSurfaceView extends IFeatureView {
    public void setLOD1MultiSurface(LOD1MultiSurfaceView multiSurface);

    public void setLOD2MultiSurface(LOD2MultiSurfaceView multiSurface);

    public void setLOD3MultiSurface(LOD3MultiSurfaceView multiSurface);
}
