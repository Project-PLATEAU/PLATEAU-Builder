package org.plateau.citygmleditor.fxml.validation;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import org.plateau.citygmleditor.citygmleditor.CoordinateDialogController;
import org.plateau.citygmleditor.validation.ValidationResultMessage;

import java.io.IOException;

public class ValidationLogListItemController {
    @FXML
    private ToggleButton root;

    public static Region createUI(ToggleGroup toggleGroup, ValidationResultMessage message) {
        ToggleButton listItem = null;
        try {
            FXMLLoader loader = new FXMLLoader(CoordinateDialogController.class.getResource("fxml/validation/validation-log-list-item.fxml"));
            listItem = loader.load();

            // 最初の行のみ表示
            var text = message.getMessage();
            int newLineIndex = text.indexOf("\n");
            if (newLineIndex != -1)
                text = text.substring(0, newLineIndex);
            listItem.setText(text);

            switch (message.getType()) {
                case Info:
                    break;
                case Warning:
                    listItem.setStyle("-fx-text-fill: #be762d;");
                    break;
                case Error:
                    listItem.setStyle("-fx-text-fill: red;");
                    break;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        listItem.setToggleGroup(toggleGroup);
        listItem.setUserData(message);
        return listItem;
    }
}
