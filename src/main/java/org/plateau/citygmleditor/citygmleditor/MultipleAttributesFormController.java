package org.plateau.citygmleditor.citygmleditor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import org.plateau.citygmleditor.citymodel.AttributeValue;
import org.plateau.citygmleditor.citymodel.CodeSpaceAttributeInfo;

import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import java.io.File;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.input.MouseEvent;
import org.w3c.dom.Element;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.common.child.ChildList;
import java.util.ArrayList;
import org.w3c.dom.Node;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.w3c.dom.NodeList;
import java.util.Collections;
import java.util.Comparator;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import org.w3c.dom.Document;
import javafx.collections.ListChangeListener;

public class MultipleAttributesFormController {

    @FXML
    private TabPane tabPane;

    public void initialize() {
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
        for (ArrayList<String> attribute : requiredChildAttributeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/add-attribute-form.fxml"));
                javafx.scene.Node form = loader.load(); // フォームをロード
                InputAttributeFormController formController = loader.getController(); // コントローラを取得
                formController.initialize(childList, parentAttributeName, "uro:" + attribute.get(0), attribute.get(1),
                        attributeList,
                        uroAttributeDocument, null, parentIndex, parentIndex);
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
}