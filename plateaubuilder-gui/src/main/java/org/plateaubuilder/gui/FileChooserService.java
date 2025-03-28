package org.plateaubuilder.gui;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.SessionManager;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * エクスプローラを介してファイルを選択する機能を提供します。
 */
public class FileChooserService {
    /**
     * ファイルをエクスプローラから指定します。
     * @param sessionPropertyKey 前回選択したファイルの情報をセッションに保存する際のキー
     * @param extensions 拡張子
     * @return 選択されたファイル
     */
    public static File showOpenDialog(String sessionPropertyKey, String... extensions) {
        var sessionProperties = SessionManager.getSessionManager().getProperties();
        File initialDirectory = getFilePropertyFromSession(sessionPropertyKey, sessionProperties);
        var chooser = createChooser(initialDirectory, extensions);

        var file = chooser.showOpenDialog(Editor.getWindow());

        if (file != null) {
            sessionProperties.setProperty(sessionPropertyKey, file.getParent());
        }

        return file;
    }

    /**
     * ディレクトリをエクスプローラから指定します。
     * @return 選択されたディレクトリ
     */
    public static File showDirectoryDialog(String initialDirectory) {
        var chooser = createDirectoryChooser(new File(initialDirectory));
        return chooser.showDialog(Editor.getWindow());
    }

    /**
     * ファイルをエクスプローラから指定します。
     * @param extensions 拡張子
     * @return 選択されたファイル
     */
    public static File showOpenDialogWithoutSession(String initialDirectory, String... extensions) {
        var chooser = createChooser(new File(initialDirectory), extensions);
        return chooser.showOpenDialog(Editor.getWindow());
    }

    /**
     * 保存するファイルをエクスプローラから指定します。
     * 
     * @param title              タイトル
     * @param fileName           ファイル名
     * @param sessionPropertyKey 前回選択したファイルの情報をセッションに保存する際のキー
     * @param extensions         拡張子
     * @return 選択されたファイル
     */
    public static File showSaveDialog(String title, String fileName, String sessionPropertyKey, String... extensions) {
        var sessionProperties = SessionManager.getSessionManager().getProperties();
        File initialDirectory = getFilePropertyFromSession(sessionPropertyKey, sessionProperties);
        var chooser = createChooser(title, initialDirectory, fileName, extensions);

        var file = chooser.showSaveDialog(Editor.getWindow());

        if (file != null) {
            sessionProperties.setProperty(sessionPropertyKey, file.getParent());
        }

        return file;
    }

    /**
     * 保存するファイルをエクスプローラから指定します。
     * 
     * @param title      タイトル
     * @param fileName   ファイル名
     * @param extensions 拡張子
     * @return 選択されたファイル
     */
    public static File showSaveDialogWithoutSession(String title, String initialDirectory, String fileName, String... extensions) {
        var chooser = createChooser(title, new File(initialDirectory), fileName, extensions);
        return chooser.showSaveDialog(Editor.getWindow());
    }

    /**
     * 複数ファイルをエクスプローラから指定します。
     * @param extensions 拡張子
     * @param sessionPropertyKey 前回選択したファイルの情報をセッションに保存する際のキー
     * @return 選択されたファイル
     */
    public static List<File> showMultipleOpenDialog(String sessionPropertyKey, String... extensions) {
        var sessionProperties = SessionManager.getSessionManager().getProperties();
        File initialDirectory = getFilePropertyFromSession(sessionPropertyKey, sessionProperties);
        var chooser = createChooser(initialDirectory, extensions);

        var files = chooser.showOpenMultipleDialog(Editor.getWindow());

        if (files != null) {
            sessionProperties.setProperty(sessionPropertyKey, files.get(0).getParent());
        }

        return files;
    }

    public static File getInitialDirectory(String sessionPropertyKey) {
        var sessionProperties = SessionManager.getSessionManager().getProperties();
        return getFilePropertyFromSession(sessionPropertyKey, sessionProperties);
    }

    private static File getFilePropertyFromSession(String sessionPropertyKey, Properties sessionProperties) {
        String initialDirectoryPath = sessionProperties.getProperty(sessionPropertyKey);
        if (initialDirectoryPath == null) {
            return null;
        }
        return new File(initialDirectoryPath);
    }

    private static FileChooser createChooser(File initialDirectory, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", extensions));
        chooser.setTitle("ファイルを選択してください");

        if (initialDirectory != null && initialDirectory.isDirectory())
            chooser.setInitialDirectory(initialDirectory);

        return chooser;
    }

    private static FileChooser createChooser(String title, File initialDirectory, String fileName, String... extensions) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", extensions));
        chooser.setTitle(title);
        chooser.setInitialFileName(fileName);

        if (initialDirectory != null && initialDirectory.isDirectory())
            chooser.setInitialDirectory(initialDirectory);

        return chooser;
    }

    private static DirectoryChooser createDirectoryChooser(File initialDirectory) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("フォルダを選択してください");

        if (initialDirectory != null && initialDirectory.isDirectory())
            chooser.setInitialDirectory(initialDirectory);

        return chooser;
    }
}
