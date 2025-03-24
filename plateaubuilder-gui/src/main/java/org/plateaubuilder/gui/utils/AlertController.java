package org.plateaubuilder.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.util.List;
import org.plateaubuilder.core.citymodel.attribute.AttributeErrorInfo;

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

    public static void showNotInputValueAlert(String type, Window owner) {
        AlertType alertType = AlertType.WARNING;
        String title = "未入力エラー";
        String content = "未入力の属性があります。入力後に再度追加ボタンを押してください。";
        showAlert(alertType, title, content, owner);
    }

    /**
     * 複数属性のエラーを表示するアラートを表示します
     * 
     * @param attributeErrors 属性名とそのエラー情報のマップ
     * @param owner           親ウィンドウ
     */
    public static void showMultipleAttributeErrorAlert(List<AttributeErrorInfo> attributeErrors, Window owner) {
        AlertType alertType = AlertType.WARNING;
        String title = "属性エラー";
        StringBuilder content = new StringBuilder("以下の属性にエラーがあります：\n\n");

        for (AttributeErrorInfo error : attributeErrors) {
            content.append("【").append(error.getAttributeName()).append("】\n");
            if (error.isEmpty()) {
                content.append("・未入力の項目があります\n");
            }
            if (error.hasInvalidValue()) {
                content.append("・入力値が不正です（").append(error.getInvalidType()).append("）\n");
            }
            content.append("\n");
        }

        showAlert(alertType, title, content.toString(), owner);
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

    public static void showExportAlert() {
        AlertType alertType = AlertType.WARNING;
        String title = "エクスポートエラー";
        String content = "エクスポートする機能はありません。";
        showAlert(alertType, title, content, null);
    }
}