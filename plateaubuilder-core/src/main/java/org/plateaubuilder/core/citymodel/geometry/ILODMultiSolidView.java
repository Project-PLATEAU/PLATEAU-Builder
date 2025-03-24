package org.plateaubuilder.core.citymodel.geometry;

import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.gml.geometry.aggregates.MultiSolid;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.editor.surfacetype.MultiSolidTypeView;

/**
 * LODSolidのインターフェースを表します。 これを実装するSolidクラスではSolidの頂点とテクスチャ座標情報が保持され、面情報は各Polygonが保持します。
 */
public interface ILODMultiSolidView extends ILODView {
    /**
     * {@code AbstractSolid}を取得します。
     * 
     * @return {@code AbstractSolid}
     */
    public MultiSolid getGmlObject();

    /**
     * {@code MultiSolidTypeView}を取得します。
     * 
     * @return {@code MultiSolidTypeView}
     */
    public MultiSolidTypeView getSurfaceTypeView();

    /**
     * 使用しているテクスチャパス
     * 
     * @return
     */
    default public List<String> getTexturePaths() {
        var parentNode = this.getParent();
        while (parentNode != null) {
            if (parentNode instanceof CityModelView)
                break;
            parentNode = parentNode.getParent();
        }
        var cityModelView = (CityModelView) parentNode;
        var ret = new ArrayList<String>();
        for (var polygon : getPolygons()) {
            if (polygon.getSurfaceData() == null)
                continue;
            var parameterizedTexture = (org.citygml4j.model.citygml.appearance.ParameterizedTexture) polygon.getSurfaceData().getGML();
            var imagePath = java.nio.file.Paths.get(parameterizedTexture.getImageURI());
            if (imagePath.startsWith("..")) {
                imagePath = java.nio.file.Paths.get(cityModelView.getGmlPath()).getParent().resolve(imagePath);
            }
            if (!ret.contains(imagePath.toString()))
                ret.add(imagePath.toString());
        }
        return ret;
    }
}
