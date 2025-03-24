package org.plateaubuilder.core.citymodel.geometry;

import org.citygml4j.model.gml.geometry.aggregates.MultiSurface;
import org.plateaubuilder.core.editor.surfacetype.MultiSurfaceTypeView;

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
     * {@code MultiSurfaceTypeView}を取得します。
     * 
     * @return {@code MultiSurfaceTypeView}
     */
    public MultiSurfaceTypeView getSurfaceTypeView();
}
