package org.plateaubuilder.gui.attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MultipleAttributeInputFormatController {

    @FXML
    private TabPane tabPane;

    private List<AttributeInputFormController> controllers = new ArrayList<>();

    public void initialize() {
        tabPane.getTabs().clear();

        // TabPaneのタブリストにリスナーを追加
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            // タブリストの変更を監視
            while (change.next()) {
                // 最後のタブが削除された場合、ウィンドウ（Stage）を閉じる
                if (change.wasRemoved() && tabPane.getTabs().isEmpty()) {
                    closeWindow();
                }
            }
        });
    }

    /**
     * 追加必須子属性を入力するためのフォームを設定します
     * 
     * @param baseAttribute              追加属性の親となるAttributeItem
     * @param treeViewChildItemList      追加済みの子属性一覧
     * @param requiredChildAttributeList 追加必須の属性の一覧
     * @param bldgAttributeTree          対象の地物のツリー情報
     */
    public void loadAttributeForms(AttributeItem baseAttribute, ArrayList<String> treeViewChildItemList,
            ArrayList<ArrayList<String>> requiredChildAttributeList,
            ChildList<ADEComponent> bldgAttributeTree) {
        for (ArrayList<String> attribute : requiredChildAttributeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("attribute-input-form.fxml"));
                javafx.scene.Node form = loader.load();
                AttributeInputFormController attributeInputFormController = loader.getController();
                attributeInputFormController.initialize(baseAttribute, attribute.get(0),
                        treeViewChildItemList,
                        bldgAttributeTree, null);
                attributeInputFormController.hideButtons();
                controllers.add(attributeInputFormController);

                // タブに設定
                Tab tab = new Tab(attribute.get(0), form);
                tabPane.getTabs().add(tab);

                attributeInputFormController.setOnAddButtonPressedCallback(() -> {
                    removeTabByName(attribute.get(0));
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeTabByName(String tabName) {
        // タブ名で対象のタブを検索し、見つかった場合は削除
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