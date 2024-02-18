package org.plateau.citygmleditor.citygmleditor;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CodeTypeMenuController {

    @FXML
    private ListView<String> codeTypeListView;
    @FXML
    private TextField searchField;

    // 選択されたコードスペースを通知するためのコールバック
    private Consumer<String> onSelectCallback;
    private ObservableList<String> originalListItems = FXCollections.observableArrayList();

    public void initialize() {
        // ListViewのアイテムがダブルクリックされたときのイベントリスナーを設定
        codeTypeListView.setOnMouseClicked(this::handleListViewDoubleClick);
        // 検索フィールドのリスナーを設定
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterList(newValue));
    }

    public void setList(File folder) {
        // フォルダ内のファイル名をリストビューに追加
        originalListItems.clear();
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            originalListItems.add(file.getName());
        }
        codeTypeListView.setItems(originalListItems);
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

    private void filterList(String filter) {
        if (filter == null || filter.isEmpty()) {
            codeTypeListView.setItems(originalListItems); // フィルタなしで全アイテムを表示
        } else {
            ObservableList<String> filteredList = FXCollections.observableArrayList();
            for (String item : originalListItems) {
                if (item.toLowerCase().contains(filter.toLowerCase())) { // 大文字小文字を無視したフィルタリング
                    filteredList.add(item);
                }
            }
            codeTypeListView.setItems(filteredList);
        }
    }

    public void setOnSelectCallback(Consumer<String> callback) {
        this.onSelectCallback = callback;
    }
}