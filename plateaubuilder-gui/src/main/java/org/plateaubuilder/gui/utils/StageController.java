package org.plateaubuilder.gui.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StageController {
    private Stage stage = new Stage();

    public StageController(Parent form, String title) {
        if (form != null) {
            stage.setAlwaysOnTop(true);
            stage.setTitle(title);
            stage.setScene(new Scene(form));
        }
    }

    public void showStage() {
        stage.show();
    }

    public void closeStage() {
        stage.close();
    }

    public Stage getStage() {
        return stage;
    }
}
