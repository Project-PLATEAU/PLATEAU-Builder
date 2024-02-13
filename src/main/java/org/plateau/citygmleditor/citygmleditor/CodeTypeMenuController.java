package org.plateau.citygmleditor.citygmleditor;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

public class CodeTypeMenuController {

    @FXML
    private ListView<String> codeTypeListView;

    // 選択されたコードスペースを通知するためのコールバック
    private Consumer<String> onSelectCallback;

    public void initialize() {
        // ListViewのアイテムがダブルクリックされたときのイベントリスナーを設定
        codeTypeListView.setOnMouseClicked(this::handleListViewDoubleClick);
    }

    public void setList(File folder) {
        // フォルダ内のファイル名をリストビューに追加
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            codeTypeListView.getItems().add(file.getName());
        }
    }

    private void handleListViewDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // 選択されたアイテムを取得
            String selectedCodeSpace = codeTypeListView.getSelectionModel().getSelectedItem();
            if (selectedCodeSpace != null && onSelectCallback != null) {
                onSelectCallback.accept(selectedCodeSpace); // コールバックを実行
            }
        }
    }

    public void setOnSelectCallback(Consumer<String> callback) {
        this.onSelectCallback = callback;
    }
}