package org.plateau.plateaubuilder.plateaubuilder.fxml.attribute;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.common.child.ChildList;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultipleAttributesFormController {

    @FXML
    private TabPane tabPane;

    private List<InputAttributeFormController> controllers = new ArrayList<>();

    public void initialize() {
        tabPane.getTabs().clear();

        // TabPaneのタブリストにリスナーを追加
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            // タブリストの変更を監視
            while (change.next()) {
                if (change.wasRemoved() && tabPane.getTabs().isEmpty()) {
                    // 最後のタブが削除された場合、ウィンドウ（Stage）を閉じる
                    closeWindow();
                }
            }
        });
    }

    public void loadAttributeForms(ArrayList<ArrayList<String>> requiredChildAttributeList,
            ChildList<ADEComponent> childList, Document uroAttributeDocument, String parentAttributeName,
            ArrayList<ArrayList<String>> attributeList, int parentIndex) {
        for (List<String> attribute : requiredChildAttributeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("add-attribute-form.fxml"));
                javafx.scene.Node form = loader.load(); // フォームをロード
                InputAttributeFormController formController = loader.getController(); // コントローラを取得
                formController.initialize(childList, parentAttributeName, "uro:" + attribute.get(0), attribute.get(1),
                        attributeList,
                        uroAttributeDocument, null, parentIndex, parentIndex, 0);
                formController.hideButtons();
                controllers.add(formController);
                // 必要に応じてコントローラにデータを設定
                // formController.setData(...);

                Tab tab = new Tab(attribute.get(0), form); // タブ名をattributeの名前に設定
                tabPane.getTabs().add(tab); // タブをTabPaneに追加
                formController.setOnAddButtonPressedCallback(() -> {
                    removeTabByName(attribute.get(0));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeTabByName(String tabName) {
        // タブ名からタブを検索し、見つかった場合は削除する
        Tab tabToRemove = null;
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getText().equals(tabName)) {
                tabToRemove = tab;
                break;
            }
        }
        if (tabToRemove != null) {
            tabPane.getTabs().remove(tabToRemove);
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAdd() {
        for (var controller : controllers) {
            controller.requestAdd();
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
}