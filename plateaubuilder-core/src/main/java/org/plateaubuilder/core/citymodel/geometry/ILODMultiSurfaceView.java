package org.plateaubuilder.core.citymodel.geometry;

import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.editor.surfacetype.TrafficAreaSurfaceTypeView;

/**
 * {@code MultiSurface}のLODビューを表すインタフェースです。
 */
public interface ILODMultiSurfaceView extends ILODView {
    /**
     * {@code MultiSurface}を取得します。
     * 
     * @return {@code MultiSurface}
     */
    public MultiSurface getGmlObject();

    /**
     * {@code TrafficAreaView}の一覧を取得します。
     * 
     * @return {@code TrafficAreaView}の一覧
     */
    default public List<TrafficAreaView> getTrafficAreaViews() {
        return null;
    }

    /**
     * {@code TrafficAreaSurfaceTypeView}を取得します。
     * 
     * @return {@code TrafficAreaSurfaceTypeView}
     */
    public TrafficAreaSurfaceTypeView getSurfaceTypeView();
}
