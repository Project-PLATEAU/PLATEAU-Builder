package org.plateaubuilder.core.io.mesh.converters;

import java.nio.file.Paths;

import org.apache.commons.lang3.NotImplementedException;
import org.plateaubuilder.core.citymodel.BuildingView;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.RoadView;
import org.plateaubuilder.core.io.mesh.AxisDirection;
import org.plateaubuilder.core.io.mesh.AxisTransformer;
import org.plateaubuilder.core.io.mesh.FormatEnum;

/**
 * LODConverterBuilderは、LOD（Level of Detail）コンバータのビルダークラスです。 LODConverterBuilderを使用して、LODコンバータのインスタンスを構築することができます。
 */
public class LODConverterBuilder {
    private CityModelView cityModelView;

    private IFeatureView featureView;

    private int lod;

    private ConvertOption convertOption;

    private FormatEnum format;

    /**
     * CityModelViewを設定します。
     * 
     * @param cityModelView CityModelViewのインスタンス
     * @return LODConverterBuilderのインスタンス
     */
    public LODConverterBuilder cityModelView(CityModelView cityModelView) {
        this.cityModelView = cityModelView;
        return this;
    }

    /**
     * FeatureViewを設定します。
     * 
     * @param featureView FeatureViewのインスタンス
     * @return LODConverterBuilderのインスタンス
     */
    public LODConverterBuilder featureView(IFeatureView featureView) {
        this.featureView = featureView;
        return this;
    }

    /**
     * LODを設定します。
     * 
     * @param lod LODの値
     * @return LODConverterBuilderのインスタンス
     */
    public LODConverterBuilder lod(int lod) {
        this.lod = lod;
        return this;
    }

    /**
     * ConvertOptionを設定します。
     * 
     * @param convertOption ConvertOptionのインスタンス
     * @return LODConverterBuilderのインスタンス
     */
    public LODConverterBuilder convertOption(ConvertOption convertOption) {
        this.convertOption = convertOption;
        return this;
    }

    /**
     * Formatを設定します。
     * 
     * @param format Formatの値
     * @return LODConverterBuilderのインスタンス
     */
    public LODConverterBuilder format(FormatEnum format) {
        this.format = format;
        return this;
    }

    /**
     * LODコンバータのインスタンスを構築します。
     * 
     * @return AbstractLODConverterのインスタンス
     * @throws NotImplementedException サポートされていないフォーマットが指定された場合にスローされます
     */
    public AbstractLODConverter build() {
        Abstract3DFormatHandler formatHandler;
        if (format == FormatEnum.gLTF) {
            formatHandler = new GltfHandler(
                    new AxisTransformer(new AxisDirection(convertOption.getAxisEast(), convertOption.getAxisUp(), true), AxisDirection.TOOL_AXIS_DIRECTION));
        } else if (format == FormatEnum.OBJ) {
            var gmlFileName = Paths.get(cityModelView.getGmlPath()).getFileName().toString();
            formatHandler = new ObjHandler(
                    new AxisTransformer(new AxisDirection(convertOption.getAxisEast(), convertOption.getAxisUp(), false), AxisDirection.TOOL_AXIS_DIRECTION),
                    gmlFileName);
        } else {
            throw new NotImplementedException(format + " is not supported.");
        }

        return buildLODConverter(formatHandler);
    }

    private AbstractLODConverter buildLODConverter(Abstract3DFormatHandler formatHandler) {
        if (featureView instanceof BuildingView) {
            return new LODBuildingConverter(cityModelView, (BuildingView) featureView, lod, convertOption, formatHandler);
        } else if (featureView instanceof RoadView) {
            return new LODRoadConverter(cityModelView, (RoadView) featureView, lod, convertOption, formatHandler);
        } else {
            throw new NotImplementedException(featureView.getClass().getName() + " is not supported.");
        }
    }
}
