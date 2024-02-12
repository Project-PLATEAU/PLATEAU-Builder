package org.plateau.citygmleditor.citygmleditor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import org.plateau.citygmleditor.citymodel.CodeSpaceValue;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import org.w3c.dom.Element;

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

        // テーブルビューにデータをロードする
        ObservableList<CodeSpaceValue> data = FXCollections.observableArrayList();
        // データを data に追加
        codeSpaceValueTable.setItems(data);
    }

    public void setCodeType(Document document) {
        this.document = document;
        Node rootNode = this.document.getDocumentElement();
        NodeList nodeList = rootNode.getChildNodes();
        ObservableList<CodeSpaceValue> dataList = FXCollections.observableArrayList();
        CodeSpaceValue codeSpaceValue;
        for (int i = 0; i < nodeList.getLength(); i++) {
            NodeList childNodeList = nodeList.item(i).getChildNodes();
            for (int j = 0; j < childNodeList.getLength(); j++) {
                if (childNodeList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    codeSpaceValue = new CodeSpaceValue(((Element) childNodeList.item(j)).getAttribute("gml:id"),
                            childNodeList.item(j).getChildNodes().item(1).getTextContent(),
                            childNodeList.item(j).getChildNodes().item(3).getTextContent());
                    dataList.add(codeSpaceValue);
                }
            }
        }
        codeSpaceValueTable.setItems(dataList);
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
}