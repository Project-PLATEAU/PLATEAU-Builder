package org.plateaubuilder.gui.attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.xerces.impl.xpath.XPath.Step;
import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.gui.utils.AlertController;
import org.plateaubuilder.gui.utils.StageController;
import org.plateaubuilder.validation.AttributeValidator;
import org.plateaubuilder.core.citymodel.attribute.AttributeErrorInfo;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.control.Alert;

public class MultipleAttributeInputFormatController {

    @FXML
    private TabPane tabPane;
    @FXML
    private Button addButton;

    private List<AttributeInputFormController> controllers = new ArrayList<>();
    private String name = null;
    private StageController stageController;
    private Set<IFeatureView> selectedFeatures;
    final double maxWidth = 1000.0;
    final double minWidth = 500.0;

    public void initialize() {
        tabPane.getTabs().clear();
        // TabPaneのタブリストにリスナーを追加
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            // タブリストの変更を監視
            while (change.next()) {
                // 最後のタブが削除された場合、ウィンドウ（Stage）を閉じる
                if (change.wasRemoved() && tabPane.getTabs().isEmpty()) {
                    stageController.closeStage();
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
            ChildList<ADEComponent> bldgAttributeTree, StageController stageController) {
        this.stageController = stageController;

        // タブの最大幅を追跡する変数（finalとして宣言）
        final double maxTabWidth = calculateMaxTabWidth(requiredChildAttributeList);

        for (ArrayList<String> attribute : requiredChildAttributeList) {
            name = attribute.get(0);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("attribute-input-form.fxml"));
                javafx.scene.Node form = loader.load();
                AttributeInputFormController attributeInputFormController = loader.getController();
                attributeInputFormController.setMultipleAttributeController(this);

                attributeInputFormController.initialize(baseAttribute, attribute.get(0),
                        treeViewChildItemList,
                        bldgAttributeTree, null);
                attributeInputFormController.hideButtons();

                // タブに設定
                Tab tab = new Tab(attribute.get(0), form);
                tabPane.getTabs().add(tab);

                // スキップされている場合は、タブやコントローラーを追加せずに削除
                if (attributeInputFormController.isSkipped()) {
                    Platform.runLater(() -> removeTabByName(name));
                    continue;
                }
                controllers.add(attributeInputFormController);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // ウィンドウの幅を調整
        Platform.runLater(() -> {
            Stage stage = stageController.getStage();
            double requiredWidth = maxTabWidth * tabPane.getTabs().size();
            double newWidth = Math.max(minWidth, Math.min(requiredWidth, maxWidth));
            stage.setWidth(newWidth);
        });

        stageController.showStage();
    }

    // 最大タブ幅を計算するヘルパーメソッド
    private double calculateMaxTabWidth(ArrayList<ArrayList<String>> attributeList) {
        double maxWidth = 0;
        for (ArrayList<String> attribute : attributeList) {
            double tabWidth = attribute.get(0).length() * 10 + 40; // 文字幅 * 10px + パディング
            maxWidth = Math.max(maxWidth, tabWidth);
        }
        return maxWidth;
    }

    private void removeTabByName(String tabName) {
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
        if (tabPane.getTabs().isEmpty()) {
            closeWindow();
        }
    }

    public void notifyFormCompleted(String name) {
        removeTabByName(name);
    }

    private void closeWindow() {

        stageController.closeStage();
    }

    @FXML
    private void handleAdd() {
        // 削除するコントローラーを一時的に保持するリスト
        List<AttributeInputFormController> controllersToRemove = new ArrayList<>();
        // 属性ごとのエラー情報を保持するリスト
        List<AttributeErrorInfo> attributeErrors = new ArrayList<>();

        // 全てのコントローラーの入力値を検証
        for (var controller : controllers) {
            AttributeErrorInfo error = new AttributeErrorInfo(controller.getAttributeName());

            if (controller.isRequiredFieldEmpty()) {
                error.setIsEmpty(true);
                attributeErrors.add(error);
            } else {
                String value = controller.getValueField().getText();
                String attributeType = controller.getAttributeType();

                if (!AttributeValidator.checkValue(value, attributeType)) {
                    error.setHasInvalidValue(true);
                    error.setInvalidType(attributeType);
                    attributeErrors.add(error);
                } else {
                    try {
                        controller.requestAdd();
                        removeTabByName(controller.getAttributeName());
                        controllersToRemove.add(controller);
                    } catch (Exception e) {
                        error.setHasInvalidValue(true);
                        error.setInvalidType(controller.getAttributeType());
                        attributeErrors.add(error);
                        e.printStackTrace();
                    }
                }
            }
        }

        // エラーがある場合はアラートを表示
        if (!attributeErrors.isEmpty()) {
            Stage stage = (Stage) addButton.getScene().getWindow();
            AlertController.showMultipleAttributeErrorAlert(attributeErrors, stage);
        }

        // 処理が完了した後にコントローラーを削除
        controllers.removeAll(controllersToRemove);
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
}