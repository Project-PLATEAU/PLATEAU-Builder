package org.plateaubuilder.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertController {

    public static void showEditAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "編集エラー";
        String content = "編集可能な対象ではありません。";
        showAlert(alertType, title, content);
    }

    public static void showAddAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "追加エラー";
        String content = "追加できる要素がありません。";
        showAlert(alertType, title, content);
    }

    public static void showDeleteAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "削除エラー";
        String content = "削除できない要素です。";
        showAlert(alertType, title, content);
    }

    public static void showChangeAlert(String type) {
        AlertType alertType = AlertType.WARNING;
        String title = "変更エラー";
        String content = "変更後の値が要素の条件を満たしていません。\n" + type + "に従ってください";
        showAlert(alertType, title, content);
    }

    private static void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets()
                .add(AlertController.class.getResource("/org/plateaubuilder/gui/viewer.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setContentText(content);
        // アラートを表示
        alert.showAndWait();
    }
}