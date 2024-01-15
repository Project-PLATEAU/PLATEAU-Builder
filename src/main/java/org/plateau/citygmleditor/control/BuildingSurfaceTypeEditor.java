package org.plateau.citygmleditor.control;

import org.plateau.citygmleditor.citymodel.geometry.LOD2SolidView;

public class BuildingSurfaceTypeEditor {
    private final BuildingSurfaceTypeView view;

    public BuildingSurfaceTypeEditor() {
        view = BuildingSurfaceTypeView.createBuildingSurfaceTypeView();
    }

    public void setTarget(LOD2SolidView lod2SolidView) {
        view.setTarget(lod2SolidView);
    }
}
