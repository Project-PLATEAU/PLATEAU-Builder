package org.plateaubuilder.gui.attribute;

import java.util.List;
import java.util.function.Consumer;

import org.plateaubuilder.core.citymodel.attribute.AttributeValue;
import org.plateaubuilder.gui.utils.StageController;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class AddableAttributeListPanelController {

    @FXML
    private TableView<AttributeValue> attributeTableView;
    @FXML
    private TableColumn<AttributeValue, String> name;
    @FXML
    private TableColumn<AttributeValue, String> description;

    @FXML
    private TextField searchField;

    private final ObservableList<AttributeValue> allAttributes = FXCollections.observableArrayList();
    private Consumer<SelectionModel<AttributeValue>> onItemSelected;
    private StageController stageController;

    /**
     * 追加可能な属性の一覧画面を表示します
     * 
     * @param list  表示したい属性のリスト
     * @param panel パネル
     */
    public void showAddableAttributePanel(List<AttributeValue> list, Parent panel) {
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 検索フィールドのリスナーを設定
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterList(newValue));

        allAttributes.setAll(list);
        attributeTableView.setItems(allAttributes); // TableViewにアイテムを設定
        filterList(""); // 初期表示時に全ての項目を表示

        // カラムの自動リサイズを有効化
        name.setResizable(true);
        name.setMinWidth(300);
        description.setResizable(true);
        description.setMinWidth(100);

        showPanel(panel);
    }

    /**
     * コールバックを設定するメソッド
     */
    public void setItemSelectedCallback(Consumer<SelectionModel<AttributeValue>> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    private void showPanel(Parent panel) {
        stageController = new StageController(panel, "追加属性の選択");
        // TableViewの内容が変更された後に幅を自動調整
        Platform.runLater(() -> {
            // TableViewの全体の幅を計算
            double totalWidth = attributeTableView.getColumns().stream()
                    .mapToDouble(TableColumn::getWidth)
                    .sum();

            // パディングなどの余白を考慮して少し広めに設定
            totalWidth += 30; // 左右のパディング分

            // ステージの幅を設定
            stageController.getStage().setWidth(totalWidth);
        });
        stageController.showStage();
    }

    private void filterList(String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            attributeTableView.setItems(allAttributes);
        } else {
            ObservableList<AttributeValue> filteredList = FXCollections.observableArrayList();
            for (AttributeValue attribute : allAttributes) {
                if (attribute.nameProperty().getValue().toLowerCase().contains(filter.toLowerCase())) {
                    filteredList.add(attribute);
                }
            }
            attributeTableView.setItems(filteredList);
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // TableViewの選択モデルを取得
            SelectionModel<AttributeValue> selectionModel = attributeTableView.getSelectionModel();
            if (selectionModel != null && onItemSelected != null) {
                // コンシューマに選択モデルを渡す
                onItemSelected.accept(selectionModel);
                stageController.closeStage();
            }
        }
    }
}