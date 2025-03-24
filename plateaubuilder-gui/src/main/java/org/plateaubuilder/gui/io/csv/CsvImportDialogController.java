package org.plateaubuilder.gui.io.csv;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.plateaubuilder.core.io.csv.importers.CSVImporter;
import org.plateaubuilder.gui.FileChooserService;
import org.plateaubuilder.core.editor.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class CsvImportDialogController implements Initializable {
    private Stage root;

    private String fileUrl;

    private boolean dialogResult;

    @FXML
    private TextField textFieldFile;

    @FXML
    private ListView<Text> messageListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * FXMLのStageを設定
     *
     * @param stage
     */
    public void setRoot(Stage stage) {
        root = stage;
    }

    /**
     * 出力先を取得
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * ダイアログ結果を取得
     *
     * @return
     */
    public boolean getDialogResult() {
        return dialogResult;
    }

    /**
     * 出力先選択時イベント
     *
     * @param actionEvent
     */
    public void onSelectFile(ActionEvent actionEvent) {
        var extensions = new String[] { "*.csv" };
        var file = FileChooserService.showOpenDialog(SessionManager.GLTF_FILE_PATH_PROPERTY, extensions);
        if (file == null) {
            return;
        }

        textFieldFile.setText(file.getAbsolutePath());
    }

    /**
     * エクスポートボタン選択時イベント
     *
     * @param actionEvent
     */
    public void onSubmit(ActionEvent actionEvent) {
        this.fileUrl = textFieldFile.getText();
        if (StringUtils.isEmpty(this.fileUrl)) {
            return;
        }
        messageListView.getItems().clear();
        CSVImporter importer = new CSVImporter();
        importer.validateCsv(this.fileUrl, errorMessage -> {
            Platform.runLater(() -> {
                Text errorText = new Text(errorMessage);
                errorText.setFill(Color.RED);
                messageListView.getItems().add(errorText);
            });
        }, () -> {
            Platform.runLater(() -> {
                if (messageListView.getItems().isEmpty()) {
                    importer.importCsv();
                    messageListView.getItems().add(new Text("対象のCSVの読み込みが完了しました。"));
                } else {
                    Text errorText = new Text(
                            String.format("エラーが %d 件発生しています。", messageListView.getItems().size()));
                    errorText.setFill(Color.RED);
                    messageListView.getItems().add(errorText);
                }
            });
        });
    }

    /**
     * CSVエクスポートダイアログ表示
     */
    public static CsvImportDialogController create() {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(CsvExportDialogController.class.getResource("csv-import-dialog.fxml")));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("CSVファイルをインポートする");
            var controller = (CsvImportDialogController) loader.getController();
            controller.setRoot(stage);
            stage.showAndWait();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
