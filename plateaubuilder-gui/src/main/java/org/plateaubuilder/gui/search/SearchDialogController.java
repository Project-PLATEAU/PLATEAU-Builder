package org.plateaubuilder.gui.search;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 地物検索機能のコントローラクラスです。
 * このクラスはFXMLファイルに定義されたGUI要素にアクセスし、それらの要素のイベントやプロパティを処理します。
 */
public class SearchDialogController implements Initializable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    /**
     * 検索ダイアログを生成します。
     */
    public static void createSearchDialog() {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    SearchDialogController.class.getResource("search-dialog.fxml")));
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
