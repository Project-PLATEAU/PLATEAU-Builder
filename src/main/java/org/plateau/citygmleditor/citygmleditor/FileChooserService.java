package org.plateau.citygmleditor.citygmleditor;

import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileChooserService {
    public static File chooseFile(String extensions, String sessionPropertyKey) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported files", extensions));
        chooser.setTitle("ファイルを選択してください");
        File file = chooser.showOpenDialog(CityGMLEditorApp.getWindow());

        if (file != null) {
            SessionManager.getSessionManager().getProperties().setProperty(sessionPropertyKey, file.toString());
        }

        return file;
    }
}
