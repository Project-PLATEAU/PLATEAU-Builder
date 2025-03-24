package org.plateaubuilder.core.io.mesh.exporters;

import org.plateaubuilder.core.citymodel.geometry.ILODView;

/**
 * 抽象的なLODExporterの基底クラスです。
 */
abstract public class AbstractLODExporter<T extends ILODView> {
    private final T lodView;

    private final String featureId;

    private final ExportOption exportOption;

    /**
     * AbstractLodExporterクラスのコンストラクターです。
     * 
     * @param lodView      {@link ILODView}
     * @param featureId    Feature ID
     * @param exportOption エクスポートオプション
     */
    public AbstractLODExporter(T lodView, String featureId, ExportOption exportOption) {
        this.lodView = lodView;
        this.featureId = featureId;
        this.exportOption = exportOption;
    }

    /**
     * {@link ILODView}を取得します。
     * 
     * @return {@link ILODView}
     */
    public T getLODView() {
        return lodView;
    }

    /**
     * Feature IDを取得します。
     * 
     * @return ID
     */
    public String getFeatureId() {
        return featureId;
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
