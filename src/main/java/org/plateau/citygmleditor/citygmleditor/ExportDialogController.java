package org.plateau.citygmleditor.citygmleditor;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ExportDialogController {
    @FXML
    private TextField folderNameTextField;

    private String folderName;
    private Stage dialogStage;

    @FXML
    private void handleConfirm() {
        folderName = folderNameTextField.getText();
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public String getFolderName() {
        return folderName;
    }
}