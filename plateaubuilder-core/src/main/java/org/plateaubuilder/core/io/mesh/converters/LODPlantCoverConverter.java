package org.plateaubuilder.core.io.mesh.converters;

import java.util.HashSet;
import java.util.Set;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.citygml4j.model.gml.geometry.aggregates.MultiSolidProperty;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurfaceProperty;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SolidProperty;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.PlantCoverView;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSolidView;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.commands.ReplacePlantCoverCommand;
import org.plateaubuilder.core.editor.surfacetype.PlantCoverModuleComponentManipulator;

public class LODPlantCoverConverter extends AbstractLODConverter<PlantCoverView, PlantCover> {

    public LODPlantCoverConverter(CityModelView cityModelView, PlantCoverView featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD1(PlantCoverView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (PlantCover) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new PlantCoverModuleComponentManipulator(newFeature, 1).clear();

        // lod1MultiSolid
        MultiSolid multiSolid = new MultiSolid();
        for (var polygon : getPolygons()) {
            CompositeSurface compositeSurface = new CompositeSurface();
            compositeSurface.addSurfaceMember(new SurfaceProperty(polygon));
            Solid solid = new Solid();
            solid.setExterior(new SurfaceProperty(compositeSurface));
            multiSolid.addSolidMember(new SolidProperty(solid));
        }
        MultiSolidProperty multiSolidProperty = new MultiSolidProperty(multiSolid);
        newFeature.setLod1MultiSolid(multiSolidProperty);

        Editor.getUndoManager().addCommand(new ReplacePlantCoverCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD2(PlantCoverView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (PlantCover) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new PlantCoverModuleComponentManipulator(newFeature, 2).clear();

        if (feature.getLOD2MultiSolid() != null) {
            // lod2MultiSolid
            MultiSolid multiSolid = new MultiSolid();
            for (var polygon : getPolygons()) {
                CompositeSurface compositeSurface = new CompositeSurface();
                compositeSurface.addSurfaceMember(new SurfaceProperty(polygon));
                Solid solid = new Solid();
                solid.setExterior(new SurfaceProperty(compositeSurface));
                multiSolid.addSolidMember(new SolidProperty(solid));
            }
            MultiSolidProperty multiSolidProperty = new MultiSolidProperty(multiSolid);
            newFeature.setLod2MultiSolid(multiSolidProperty);
        } else if (feature.getLOD2MultiSurface() != null) {
            // lod2MultiSurface
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : getPolygons()) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }
            newFeature.setLod2MultiSurface(new MultiSurfaceProperty(multiSurface));
        }

        Editor.getUndoManager().addCommand(new ReplacePlantCoverCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createLOD3(PlantCoverView feature) {
        var oldFeature = feature.getGML();
        var newFeature = (PlantCover) oldFeature.copy(new DeepCopyBuilder());

        // 古いsurfaceを削除
        new PlantCoverModuleComponentManipulator(newFeature, 3).clear();

        if (feature.getLOD3MultiSolid() != null) {
            // lod3MultiSolid
            MultiSolid multiSolid = new MultiSolid();
            for (var polygon : getPolygons()) {
                CompositeSurface compositeSurface = new CompositeSurface();
                compositeSurface.addSurfaceMember(new SurfaceProperty(polygon));
                Solid solid = new Solid();
                solid.setExterior(new SurfaceProperty(compositeSurface));
                multiSolid.addSolidMember(new SolidProperty(solid));
            }
            MultiSolidProperty multiSolidProperty = new MultiSolidProperty(multiSolid);
            newFeature.setLod3MultiSolid(multiSolidProperty);
        } else if (feature.getLOD3MultiSurface() != null) {
            // lod3MultiSurface
            MultiSurface multiSurface = new MultiSurface();
            for (var polygon : getPolygons()) {
                multiSurface.addSurfaceMember(new SurfaceProperty(polygon));
            }
            newFeature.setLod3MultiSurface(new MultiSurfaceProperty(multiSurface));
        }

        Editor.getUndoManager().addCommand(new ReplacePlantCoverCommand(getCityModelView().getGML(), oldFeature, newFeature));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void removeTexture(Appearance appearance) {
        // もともと参照していたテクスチャを削除する
        var hrefSet = new HashSet<String>();
        var lod = getLOD();
        var featureView = getFeatureView();
        switch (lod) {
        case 2:
            var lod2View = featureView.getLODView(lod);
            if (lod2View instanceof ILODMultiSolidView) {
                collectHref(hrefSet, ((ILODMultiSolidView) lod2View).getGmlObject());
            }
            if (lod2View instanceof ILODMultiSurfaceView) {
                collectHref(hrefSet, ((ILODMultiSurfaceView) lod2View).getGmlObject());
            }
            break;
        case 3:
            var lod3View = featureView.getLODView(lod);
            if (lod3View instanceof ILODMultiSolidView) {
                collectHref(hrefSet, ((ILODMultiSolidView) lod3View).getGmlObject());
            }
            if (lod3View instanceof ILODMultiSurfaceView) {
                collectHref(hrefSet, ((ILODMultiSurfaceView) lod3View).getGmlObject());
            }
            break;
        default:
            throw new IllegalArgumentException("Unsupported LOD");
        }

        removeTexture(appearance, hrefSet);
    }

    protected void collectHref(Set<String> hrefSet, MultiSolid multiSolid) {
        for (var solidMember : multiSolid.getSolidMember()) {
            hrefSet.add(solidMember.getHref());
        }
    }

    protected void collectHref(Set<String> hrefSet, MultiSurface multiSurface) {
        for (var surfaceMember : multiSurface.getSurfaceMember()) {
            hrefSet.add(surfaceMember.getHref());
        }
    }
}
