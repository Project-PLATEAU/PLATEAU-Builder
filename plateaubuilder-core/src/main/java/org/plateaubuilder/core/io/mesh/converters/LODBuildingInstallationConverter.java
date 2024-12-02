package org.plateaubuilder.core.io.mesh.converters;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.building.BuildingInstallation;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.BuildingInstallationView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplaceBuildingInstallationCommand;
import org.plateaubuilder.core.editor.surfacetype.BuildingInstallationModuleComponentManipulator;

public class LODBuildingInstallationConverter extends AbstractLODGeometryConverter<BuildingInstallationView, BuildingInstallation> {

    public LODBuildingInstallationConverter(CityModelView cityModelView, BuildingInstallationView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD1(BuildingInstallationView feature) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD2(BuildingInstallationView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (BuildingInstallation) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new BuildingInstallationModuleComponentManipulator(newFeature, 2).clear();

        // lod2Geometry
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        newFeature.setLod2Geometry(new MultiSurfaceProperty(multiSurface));

        Editor.getUndoManager().addCommand(new ReplaceBuildingInstallationCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD3(BuildingInstallationView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (BuildingInstallation) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new BuildingInstallationModuleComponentManipulator(newFeature, 3).clear();

        // lod3Geometry
        MultiSurface multiSurface = new MultiSurface();
        for (var polygon : getPolygons()) {
            multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
        }
        newFeature.setLod3Geometry(new MultiSurfaceProperty(multiSurface));

        Editor.getUndoManager().addCommand(new ReplaceBuildingInstallationCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }
}
