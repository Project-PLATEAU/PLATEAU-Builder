package org.plateaubuilder.core.io.mesh.converters;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.geometry.AbstractGeometry;
import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.citygml4j.model.gml.geometry.complexes.CompositeSurface;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.citygml4j.model.gml.geometry.primitives.Solid;
import org.citygml4j.model.gml.geometry.primitives.SurfaceProperty;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.geometry.ILODGeometryView;

abstract public class AbstractLODGeometryConverter<T extends IFeatureView, TGML extends AbstractCityObject> extends AbstractLODConverter<T, TGML> {
    public AbstractLODGeometryConverter(CityModelView cityModelView, T featureView, int lod, ConvertOption convertOption,
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
            var lod2View = (ILODGeometryView) featureView.getLODView(lod);
            if (lod2View == null) {
                return;
            }
            collectHref(hrefSet, lod2View.getGmlObject());
            break;
        case 3:
            var lod3View = (ILODGeometryView) featureView.getLODView(lod);
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

    protected void collectHref(Set<String> hrefSet, AbstractGeometry geometry) {
        if (geometry instanceof Solid) {
            var exterior = ((Solid) geometry).getExterior();
            var compositeSurface = (CompositeSurface) exterior.getObject();

            List<SurfaceProperty> surfaceMember = compositeSurface.getSurfaceMember();

            for (SurfaceProperty surfaceMemberElement : surfaceMember) {
                var polygon = (Polygon) surfaceMemberElement.getSurface();
                hrefSet.add(String.format("#%s", polygon.getId()));
            }
        } else if (geometry instanceof MultiSurface) {
            for (var member : ((MultiSurface) geometry).getSurfaceMember()) {
                var surface = member.getSurface();
                if (surface instanceof Polygon) {
                    var polygon = (Polygon) surface;
                    hrefSet.add(String.format("#%s", polygon.getId()));
                } else if (surface instanceof CompositeSurface) {
                    var compositeSurface = (CompositeSurface) surface;
                    for (var polygon : compositeSurface.getSurfaceMember()) {
                        hrefSet.add(String.format("#%s", polygon.getSurface().getId()));
                    }
                }
            }
        }
    }
}
