package org.plateaubuilder.gui.attribute;

import java.util.function.Consumer;

import org.plateaubuilder.core.citymodel.attribute.CodeSpaceValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class CodeSpaceValueMenuController {
    @FXML
    private TableView<CodeSpaceValue> codeSpaceValueTable;
    @FXML
    private TableColumn<CodeSpaceValue, String> id;
    @FXML
    private TableColumn<CodeSpaceValue, String> description;
    @FXML
    private TableColumn<CodeSpaceValue, String> name;

    private Document document;
    private Consumer<CodeSpaceValue> onItemSelected;

    @FXML
    public void initialize() {
        id.setCellValueFactory(new PropertyValueFactory<>("id"));
        description.setCellValueFactory(new PropertyValueFactory<>("description"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        ObservableList<CodeSpaceValue> data = FXCollections.observableArrayList();
        codeSpaceValueTable.setItems(data);
    }

    public void setCodeType(Document document) {
        this.document = document;
        NodeList nodeList = this.document.getDocumentElement().getChildNodes();
        ObservableList<CodeSpaceValue> dataList = FXCollections.observableArrayList();
        CodeSpaceValue codeSpaceValue;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                // ID, 説明、名前を各ノードから取得
                String id = element.getAttribute("gml:id");
                String description = getElementTextContentByTagName(element, "gml:description");
                String name = getElementTextContentByTagName(element, "gml:name");

                codeSpaceValue = new CodeSpaceValue(id, description, name);
                dataList.add(codeSpaceValue);
            }
        }
        codeSpaceValueTable.setItems(dataList);
    }

    private String getElementTextContentByTagName(Element parentElement, String tagName) {
        NodeList nodes = parentElement.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return ""; // タグが存在しない場合は空文字を返す
    }

    // コールバックを設定するメソッド
    public void setItemSelectedCallback(Consumer<CodeSpaceValue> onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            // 選択された行のデータを取得
            CodeSpaceValue selectedItem = codeSpaceValueTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null && onItemSelected != null) {
                onItemSelected.accept(selectedItem);
            }
        }
    }

    public void initializeTableWidth() {
        // テーブルの列幅を設定
        double baseColumnWidth = 200.0; // 各列の最小幅

        // 各列に最小幅を設定
        id.setPrefWidth(baseColumnWidth + 50);
        description.setPrefWidth(baseColumnWidth);
        name.setPrefWidth(baseColumnWidth - 50);

        // テーブル全体の幅を計算
        final double finalTotalWidth = baseColumnWidth * 3 + 20;

        // テーブルの幅を設定
        codeSpaceValueTable.setPrefWidth(finalTotalWidth);

        // ウィンドウサイズを設定
        Platform.runLater(() -> {
            Stage stage = (Stage) codeSpaceValueTable.getScene().getWindow();
            stage.setMinWidth(finalTotalWidth);
            stage.setWidth(finalTotalWidth);
        });

        // 列の自動リサイズを有効化
        codeSpaceValueTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }
}