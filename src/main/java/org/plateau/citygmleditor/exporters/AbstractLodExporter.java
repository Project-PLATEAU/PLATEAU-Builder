package org.plateau.citygmleditor.exporters;

import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;

/**
 * 抽象的なLODエクスポーターの基底クラスです。
 */
abstract public class AbstractLodExporter {
    private final ILODSolidView lodSolid;

    private final String buildingId;

    private final ExportOption exportOption;

    /**
     * AbstractLodExporterクラスのコンストラクターです。
     * 
     * @param lodSolid {@link ILODSolidView}
     * @param buildingId Building ID
     * @param exportOption エクスポートオプション
     */
    public AbstractLodExporter(ILODSolidView lodSolid, String buildingId, ExportOption exportOption) {
        this.lodSolid = lodSolid;
        this.buildingId = buildingId;
        this.exportOption = exportOption;
    }

    /**
     * {@link ILODSolidView}を取得します。
     * 
     * @return {@link ILODSolidView}
     */
    public ILODSolidView getLodSolid() {
        return lodSolid;
    }

    /**
     * Building IDを取得します。
     * 
     * @return Building ID
     */
    public String getBuildingId() {
        return buildingId;
    }

    /**
     * エクスポートオプションを取得します。
     * 
     * @return エクスポートオプション
     */
    public ExportOption getExportOption() {
        return exportOption;
    }

    /**
     * 指定されたファイルURLにエクスポートします。
     * 
     * @param fileUrl ファイルURL
     */
    abstract public void export(String fileUrl);
}
