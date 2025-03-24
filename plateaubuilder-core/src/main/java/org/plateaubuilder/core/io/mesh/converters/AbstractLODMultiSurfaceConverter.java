package org.plateaubuilder.core.io.mesh.converters;

import java.util.HashSet;
import java.util.Set;

import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.geometry.ILODMultiSurfaceView;

abstract public class AbstractLODMultiSurfaceConverter<T extends IFeatureView, TGML extends AbstractCityObject> extends AbstractLODConverter<T, TGML> {
    public AbstractLODMultiSurfaceConverter(CityModelView cityModelView, T featureView, int lod, ConvertOption convertOption,
            Abstract3DFormatHandler formatHandler) {
        super(cityModelView, featureView, lod, convertOption, formatHandler);
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
            var lod2View = (ILODMultiSurfaceView) featureView.getLODView(lod);
            if (lod2View == null) {
                return;
            }
            collectHref(hrefSet, lod2View.getGmlObject());
            break;
        case 3:
            var lod3View = (ILODMultiSurfaceView) featureView.getLODView(lod);
            if (lod3View == null) {
                return;
            }
            collectHref(hrefSet, lod3View.getGmlObject());
            break;
        default:
            throw new IllegalArgumentException("Unsupported LOD");
        }

        removeTexture(appearance, hrefSet);
    }

    protected void collectHref(Set<String> hrefSet, MultiSurface multiSurface) {
        for (var surfaceMember : multiSurface.getSurfaceMember()) {
            hrefSet.add(surfaceMember.getHref());
        }
    }
}
