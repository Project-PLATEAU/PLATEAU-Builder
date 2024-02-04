package org.plateau.citygmleditor.citygmleditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.function.Consumer;
import javafx.scene.control.MultipleSelectionModel;

public class AddingAttributeController {

    @FXML
    private ListView<String> attributeListView;
    @FXML
    private TextField searchField;

    private final ObservableList<String> allAttributes = FXCollections.observableArrayList();
    private Consumer<MultipleSelectionModel<String>> onItemSelected;

    public void initialize() {
        // 検索フィールドのリスナーを設定
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterList(newValue));
    }

    // コールバックを設定するメソッド
    public void setItemSelectedCallback(Consumer<MultipleSelectionModel<String>> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    public void setList(List<String> list) {
        allAttributes.setAll(list);
        filterList(""); // 初期表示時に全ての項目を表示
    }

    private void filterList(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            attributeListView.setItems(allAttributes);
        } else {
            ObservableList<String> filteredList = FXCollections.observableArrayList();
            for (String attribute : allAttributes) {
                if (attribute.toLowerCase().contains(filter.toLowerCase())) {
                    filteredList.add(attribute);
                }
            }
            attributeListView.setItems(filteredList);
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            MultipleSelectionModel<String> selectedItem = attributeListView.getSelectionModel();

            if (selectedItem != null && onItemSelected != null) {
                onItemSelected.accept(selectedItem);
            }
        }
    }
}