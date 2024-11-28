package org.plateaubuilder.gui.attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.citygml4j.model.citygml.ade.ADEComponent;
import org.citygml4j.model.common.child.ChildList;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.attribute.AttributeItem;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.manager.AttributeSchemaManagerFactory;
import org.plateaubuilder.core.citymodel.attribute.manager.BuildingSchemaManager;
import org.plateaubuilder.core.citymodel.attribute.reader.CodeListReader;
import org.plateaubuilder.core.citymodel.attribute.reader.XSDSchemaDocument;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.attribute.AttributeEditor;
import org.plateaubuilder.core.editor.commands.AbstractCityGMLUndoableCommand;
import org.plateaubuilder.gui.utils.AlertController;
import org.plateaubuilder.gui.utils.StageController;
import org.plateaubuilder.validation.AttributeValidator;
import org.w3c.dom.Node;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AttributeInputFormController {

    @FXML
    private Button cancelButton, addButton;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField codeSpaceField, uomField, valueField;
    @FXML
    private VBox codeSpaceVbox, uomVbox, valueVbox;

    private ChildList<ADEComponent> bldgAttributeTree;
    private String addAttributeName;
    private String codeSpacePath;
    private String addAttributeType;
    private Runnable onAddButtonPressed, onCancelButtonPressed;
    private boolean editFlag = false, addFlag = false;
    private ArrayList<ArrayList<String>> requiredChildAttributeList,
            childAttributeList = new ArrayList<ArrayList<String>>();
    private XSDSchemaDocument uroSchemaDocument = Editor.getUroSchemaDocument();
    private AttributeItem targetAttributeItem, baseAttributeItem;
    private ArrayList<String> treeViewChildItemList;
    private StageController stageController;
    private AttributeSchemaManager attributeSchemaManager = AttributeSchemaManagerFactory
            .getSchemaManager(Editor.getFeatureSellection().getActiveFeatureProperty().get().getGML());
    private MultipleAttributeInputFormatController multipleAttributeInputFormatController;

    /**
     * 属性追加用フォームを表示するための初期設定を行います
     * 
     * @param baseAttributeItem     追加する属性の親となるAttributeItem
     * @param addAttributeName      追加する属性の名前
     * @param treeViewChildItemList 追加済みの属性の名前のリスト
     * @param bldgAttributeTree     選択中の地物のツリー情報
     * @param form                  フォーム
     */
    public void initialize(AttributeItem baseAttributeItem, String addAttributeName,
            ArrayList<String> treeViewChildItemList, ChildList<ADEComponent> bldgAttributeTree,
            Parent form) {
        this.baseAttributeItem = baseAttributeItem;
        this.addAttributeName = addAttributeName;
        this.bldgAttributeTree = bldgAttributeTree;
        this.addFlag = true;

        configureForm(baseAttributeItem.getName(), addAttributeName, treeViewChildItemList, form, "属性の追加");
        nameLabel.setText(nameLabel.getText() + addAttributeName);
    }

    /**
     * 属性編集用フォームを表示するための初期設定を行います
     * 
     * @param targetAttributeItem 編集対象のAttributeItem
     * @param form                フォーム
     */
    public void initialize(AttributeItem targetAttributeItem, Parent form) {
        this.targetAttributeItem = targetAttributeItem;
        this.editFlag = true;

        configureForm(targetAttributeItem.getName(), null, null, form, "属性の編集");
        nameLabel.setText(nameLabel.getText() + targetAttributeItem.getName());
    }

    /**
     * UIの初期設定を実施し、フォームを表示します。
     */
    private void configureForm(String parentAttributeName, String addAttributeName,
            ArrayList<String> treeViewChildItemList, Parent form, String formTitle) {
        uroSchemaDocument = Editor.getUroSchemaDocument();

        if (addFlag) {
            if (addAttributeName.split(":")[0].matches("uro")) {
                addAttributeType = uroSchemaDocument.getType(addAttributeName, parentAttributeName, "uro");
                requiredChildAttributeList = uroSchemaDocument.getElementList(addAttributeName, true,
                        treeViewChildItemList,
                        "uro");
                childAttributeList = uroSchemaDocument.getElementList(addAttributeName, false, null, "uro");
            } else {
                if (parentAttributeName.matches("root")) {
                    addAttributeType = attributeSchemaManager.getAttributeType(addAttributeName.split(":")[1]);
                } else {
                    addAttributeType = attributeSchemaManager.getChildAttributeType(parentAttributeName.split(":")[1],
                            addAttributeName.split(":")[1]);
                }
            }
        } else {
            addAttributeType = targetAttributeItem.getType();
        }

        configureInputBoxVisibility(addAttributeType);

        // 追加必須の子属性を持つ場合は親要素の処理をスキップ
        if (requiredChildAttributeList != null && !requiredChildAttributeList.isEmpty()) {
            handleAdd();
            return;
        }
        if (form != null) {
            stageController = new StageController(form, formTitle);
            stageController.showStage();
        }
    }

    /**
     * 各種入力BOXの初期設定を実施します
     */
    private void configureInputBoxVisibility(String addAttributeType) {
        // codeType入力BOXの表示設定
        if (addAttributeType.matches("gml:CodeType")) {
            valueField.setDisable(true);
            if (editFlag) {
                String codeSpace = AttributeEditor.getCodeSpace(targetAttributeItem);
                if (codeSpace != null) {
                    setCodeSpaceField(codeSpace.substring(codeSpace.lastIndexOf("/") + 1));
                } else {
                    setCodeSpaceField(""); // デフォルト値を設定
                }
            }
        } else {
            setVisibility(codeSpaceVbox, false);
        }

        // uom入力BOXの表示設定
        if (!addAttributeType.matches("gml:MeasureType") && !addAttributeType.matches("gml:LengthType")
                & !addAttributeType.matches("gml::MeasureOrNullListType")) {
            setVisibility(uomVbox, false);
        } else {
            if (editFlag)
                setUomField(AttributeEditor.getUom(targetAttributeItem));
        }

        // 属性値入力BOXの表示設定
        if (addFlag) {
            if (!childAttributeList.isEmpty()) {
                valueVbox.setManaged(false);
                valueVbox.setVisible(false);
            }
        } else if (editFlag) {
            setValueField(AttributeEditor.getValue(targetAttributeItem));
        }
    }

    /**
     * CodeSpace属性を入力するためのフォームを表示します
     */
    private void inputCodeSpace() {
        String codeListDirPath = Editor.getDatasetPath() + "\\codelists";
        Parent valueRoot = null;
        Parent typeRoot = null;
        FXMLLoader valueLoader = null;
        CodeSpaceTypeMenuController codeSpaceTypeMenuController = null;

        // CodeSpaceタイプの入力フォームを表示
        try {
            FXMLLoader typeLoader = new FXMLLoader(getClass().getResource("codeSpace-Type-Menu.fxml"));
            typeRoot = typeLoader.load();
            codeSpaceTypeMenuController = typeLoader.getController();
            codeSpaceTypeMenuController.setList(new File(codeListDirPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final StageController codeTypeStageController = new StageController(typeRoot, "CodeSpaceの選択");
        codeTypeStageController.showStage();

        try {
            valueLoader = new FXMLLoader(getClass().getResource("codeSpace-Value-Menu.fxml"));
            valueRoot = valueLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final CodeSpaceValueMenuController codeSpaceValueMenuController = valueLoader.getController();
        final Parent finalValueRoot = valueRoot;
        final StageController codeValueStageController = new StageController(finalValueRoot, "codeSpace値の入力");

        CodeListReader CodeListReader = new CodeListReader();
        // codeTypeMenuControllerで表示されるメニューにおいて、選択行為がされたら呼び出される
        codeSpaceTypeMenuController.setOnSelectCallback(selectedCodeSpace -> {
            setCodeSpaceField(selectedCodeSpace);
            codeSpacePath = "../../codelists/" + selectedCodeSpace;
            CodeListReader.readCodeList(codeListDirPath + "\\" + selectedCodeSpace);
            codeSpaceValueMenuController.setCodeType(CodeListReader.getCodeListDocument());

            codeValueStageController.showStage();
            codeTypeStageController.closeStage();
        });

        // codeSpaceValueMenuControllerで表示されるメニューにおいて、選択行為がされたら呼び出される
        codeSpaceValueMenuController.setItemSelectedCallback(selectedItem -> {
            String name = selectedItem.nameProperty().getValue();
            setValueField(name);

            codeValueStageController.closeStage();
        });
    }

    /**
     * CodeSpaceの値を変更するためのフォームを表示します
     */
    private void changeCodeSpaceValue(String codeSpaceValue) {
        Parent valueRoot = null;
        FXMLLoader valueLoader = null;
        String codeSpacePath = Editor.getDatasetPath() + "\\codelists" + "\\" + codeSpaceValue;

        try {
            valueLoader = new FXMLLoader(getClass().getResource("codeSpace-Value-Menu.fxml"));
            valueRoot = valueLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final CodeSpaceValueMenuController codeSpaceValueMenuController = valueLoader.getController();
        CodeListReader codeListReader = new org.plateaubuilder.core.citymodel.attribute.reader.CodeListReader();
        codeListReader.readCodeList(codeSpacePath);

        codeSpaceValueMenuController.setCodeType(codeListReader.getCodeListDocument());

        StageController stageController = new StageController(valueRoot, "codeSpace値の入力");
        stageController.showStage();

        codeSpaceValueMenuController.setItemSelectedCallback(selectedItem -> {
            setValueField(selectedItem.nameProperty().getValue());
            stageController.closeStage();
        });
    }

    /**
     * 対象のVboxの表示・非表示を制御します
     */
    private void setVisibility(VBox vbox, boolean isVisible) {
        vbox.setManaged(isVisible);
        vbox.setVisible(isVisible);
    }

    private void setCodeSpaceField(String value) {
        codeSpaceField.setText(value);
    }

    private void setValueField(String value) {
        valueField.setText(value);
    }

    private void setUomField(String value) {
        uomField.setText(value);
    }

    // CodeSpace選択ボタンのイベントハンドラ
    @FXML
    private void handleSelectCodeSpace(ActionEvent event) {
        inputCodeSpace();
    }

    // CodeSpace選択ボタンのイベントハンドラ
    @FXML
    private void handleSelectCodeSpaceValue(ActionEvent event) {
        changeCodeSpaceValue(codeSpaceField.getText());
    }

    public void requestAdd() {
        handleAdd();
    }

    public void hideButtons() {
        addButton.setVisible(false);
        cancelButton.setVisible(false);
    }

    /**
     * 追加ボタンが押された場合のCallback
     */
    public void setOnAddButtonPressedCallback(Runnable callback) {
        this.onAddButtonPressed = callback;
    }

    /**
     * キャンセルボタンが押された場合のCallback
     */
    public void setOnCancelButtonPressedCallback(Runnable callback) {
        this.onCancelButtonPressed = callback;
    }

    /**
     * キャンセルボタンのイベントハンドラ
     */
    @FXML
    private void handleCancel() {
        if (onCancelButtonPressed != null) {
            onCancelButtonPressed.run();
        }
        if (stageController != null)
            stageController.closeStage();
    }

    /**
     * 追加ボタンのイベントハンドラ
     */
    @FXML
    private void handleAdd() {
        String codeSpace = codeSpaceField.getText();
        String uom = uomField.getText();
        String value = valueField.getText();
        if (AttributeValidator.checkValue(value, addAttributeType)) {
            if (addFlag) {
                Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {
                    private final IFeatureView focusTarget = Editor.getFeatureSellection().getActive();
                    private final ChildList<ADEComponent> bldgAttributeTreeCache = bldgAttributeTree;
                    private final String addAttributeNameCache = addAttributeName;
                    private final AttributeItem baseAttributeItemCache = baseAttributeItem;

                    private final String newCodeSpace = codeSpacePath;
                    private final String newUom = uom;
                    private final String newValue = value;

                    private Node addedNode;
                    private AttributeItem addedAttributeItem;

                    @Override
                    public void redo() {
                        baseAttributeItem = AttributeEditor.addAttribute(baseAttributeItemCache, addAttributeNameCache,
                                newValue,
                                newCodeSpace, newUom, bldgAttributeTreeCache);
                        addedAttributeItem = baseAttributeItem;
                    }

                    @Override
                    public void undo() {
                        AttributeEditor.removeAttribute(addAttributeNameCache, baseAttributeItemCache,
                                addedAttributeItem,
                                bldgAttributeTreeCache);
                    }

                    @Override
                    public javafx.scene.Node getRedoFocusTarget() {
                        return focusTarget.getNode();
                    }

                    @Override
                    public javafx.scene.Node getUndoFocusTarget() {
                        return focusTarget.getNode();
                    }
                });
            } else if (editFlag) {
                Editor.getUndoManager().addCommand(new AbstractCityGMLUndoableCommand() {

                    private final IFeatureView focusTarget = Editor.getFeatureSellection().getActive();

                    private final String oldCodeSpace = AttributeEditor.getCodeSpace(targetAttributeItem);
                    private final String oldUom = AttributeEditor.getUom(targetAttributeItem);
                    private final String oldValue = AttributeEditor.getValue(targetAttributeItem);
                    private final String newCodeSpace = codeSpace;
                    private final String newUom = uom;
                    private final String newValue = value;

                    @Override
                    public void redo() {
                        AttributeEditor.editAttribute(targetAttributeItem, newCodeSpace, newUom, newValue);
                    }

                    @Override
                    public void undo() {
                        AttributeEditor.editAttribute(targetAttributeItem, oldCodeSpace, oldUom, oldValue);
                    }

                    @Override
                    public javafx.scene.Node getRedoFocusTarget() {
                        return focusTarget.getNode();
                    }

                    @Override
                    public javafx.scene.Node getUndoFocusTarget() {
                        return focusTarget.getNode();
                    }

                });
            }
        } else {

            Stage stage = (Stage) addButton.getScene().getWindow();
            AlertController.showValueAlert(addAttributeType, stage);
            return;
        }
        if (this.multipleAttributeInputFormatController != null) {
            this.multipleAttributeInputFormatController.notifyFormCompleted(addAttributeName);
        }
        if (requiredChildAttributeList != null && requiredChildAttributeList.size() != 0) {
            showMultipleAttributeInputForm(requiredChildAttributeList);
        }

        if (onAddButtonPressed != null) {
            onAddButtonPressed.run();
        }
        if (stageController != null) {
            stageController.closeStage();
        }
    }

    /**
     * 追加必須の子属性情報の入力フォームを表示します
     */
    private void showMultipleAttributeInputForm(ArrayList<ArrayList<String>> requiredChildAttributeList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("multiple-attribute-input-format.fxml"));
            Parent root = loader.load();
            MultipleAttributeInputFormatController multipleAttributeInputFormatController = loader.getController();
            StageController stageController = new StageController(root, baseAttributeItem.getName() + " 配下の必須属性の入力");
            multipleAttributeInputFormatController.loadAttributeForms(baseAttributeItem, treeViewChildItemList,
                    requiredChildAttributeList, bldgAttributeTree, stageController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMultipleAttributeController(MultipleAttributeInputFormatController controller) {
        this.multipleAttributeInputFormatController = controller;
    }

    public boolean isSkipped() {
        return requiredChildAttributeList != null && !requiredChildAttributeList.isEmpty();
    }
}
