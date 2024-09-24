package org.plateaubuilder.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AlertController {

    public static void showEditAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "編集エラー";
        String content = "編集可能な対象ではありません。";
        showAlert(alertType, title, content, null);
    }

    public static void showAddAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "追加エラー";
        String content = "追加できる要素がありません。";
        showAlert(alertType, title, content, null);
    }

    public static void showDeleteAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "削除エラー";
        String content = "削除できない要素です。";
        showAlert(alertType, title, content, null);
    }

    public static void showValueAlert(String type, Window owner) {
        AlertType alertType = AlertType.WARNING;
        String title = "値エラー";
        String content = "入力された値が要素の条件を満たしていません。\n" + type + "に従ってください";
        showAlert(alertType, title, content, owner);
    }

    private static void showAlert(AlertType alertType, String title, String content, Window owner) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets()
                .add(AlertController.class.getResource("/org/plateaubuilder/gui/viewer.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setContentText(content);

        // オーナーウィンドウを設定
        alert.initOwner(owner);

        // アラートが最前面に表示されるようにする設定
        alert.setOnShown(event -> {
            ((Stage) alert.getDialogPane().getScene().getWindow()).toFront(); // Stage にキャストしてから toFront() を呼び出す
        });
        // アラートを表示
        alert.showAndWait();
    }
}