package org.plateaubuilder.gui.attribute;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class CodeSpaceTypeMenuController {

    @FXML
    private ListView<String> codeTypeListView;
    @FXML
    private TextField searchField;

    private Consumer<String> onSelectCallback;
    private ObservableList<String> originalListItems = FXCollections.observableArrayList();
    final double listViewWidth = 300.0;
    final double searchFieldWidth = listViewWidth;
    final double padding = 40.0;

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

    public void initializeTableWidth() {
        // リストビューと検索フィールドの幅を設定
        codeTypeListView.setPrefWidth(listViewWidth);
        searchField.setPrefWidth(searchFieldWidth);
        // ウィンドウサイズを設定
        Platform.runLater(() -> {
            Stage stage = (Stage) codeTypeListView.getScene().getWindow();
            double totalWidth = listViewWidth + padding;
            stage.setMinWidth(totalWidth);
            stage.setWidth(totalWidth);
        });
    }
}