package org.plateau.citygmleditor.citygmleditor;

import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * エクスプローラを介してファイルを選択する機能を提供します。
 */
public class FileChooserService {
    /**
     * 入力ファイルをエクスプローラから指定します。
     * @param extensions 拡張子
     * @param sessionPropertyKey 前回選択したファイルの情報をセッションに保存する際のキー
     * @return 選択されたファイル
     */
    public static File showOpenDialog(String extensions, String sessionPropertyKey) {
        var sessionProperties = SessionManager.getSessionManager().getProperties();
        File initialDirectory = getFilePropertyFromSession(sessionPropertyKey, sessionProperties);
        var chooser = createChooser(extensions, initialDirectory);

        var file = chooser.showOpenDialog(CityGMLEditorApp.getWindow());

        if (file != null) {
            sessionProperties.setProperty(sessionPropertyKey, file.getParent());
        }

        return file;
    }

    /**
     * 複数入力ファイルをエクスプローラから指定します。
     * @param extensions 拡張子
     * @param sessionPropertyKey 前回選択したファイルの情報をセッションに保存する際のキー
     * @return 選択されたファイル
     */
    public static List<File> showMultipleOpenDialog(String extensions, String sessionPropertyKey) {
        var sessionProperties = SessionManager.getSessionManager().getProperties();
        File initialDirectory = getFilePropertyFromSession(sessionPropertyKey, sessionProperties);
        var chooser = createChooser(extensions, initialDirectory);

        var files = chooser.showOpenMultipleDialog(CityGMLEditorApp.getWindow());

        if (files != null) {
            sessionProperties.setProperty(sessionPropertyKey, files.get(0).getParent());
        }

        return files;
    }

    private static File getFilePropertyFromSession(String sessionPropertyKey, Properties sessionProperties) {
        String initialDirectoryPath = sessionProperties.getProperty(sessionPropertyKey);
        if (initialDirectoryPath == null) {
            return null;
        }
        return new File(initialDirectoryPath);
    }

    private static FileChooser createChooser(String extensions, File initialDirectory) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", extensions));
        chooser.setTitle("ファイルを選択してください");

        if (initialDirectory != null && initialDirectory.isDirectory())
            chooser.setInitialDirectory(initialDirectory);

        return chooser;
    }
}
