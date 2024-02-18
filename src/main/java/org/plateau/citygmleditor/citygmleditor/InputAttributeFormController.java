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

public class InputAttributeFormController {

    @FXML
    private Button cancelButton;
    @FXML
    private Button addButton;
    @FXML
    private Label name;// 要素名の入力ラベル

    @FXML
    private TextField codeSpaceField; // CodeSpaceの入力フィールド

    @FXML
    private TextField uomField; // UOMの入力フィールド

    @FXML
    private TextField valueField; // 属性値の入力フィールド

    @FXML
    private VBox codeSpaceVbox; // codeSpaceのVBox
    @FXML
    private VBox uomVbox; // uomのVBox
    @FXML
    private VBox valueVbox; // 属性値のVBox

    private ChildList<ADEComponent> childList;
    private String parentAttributeName;
    private String addAttributeName;
    private String addAttributeType;
    private ArrayList<ArrayList<String>> attributeList;
    private org.w3c.dom.Document uroAttributeDocument;
    private String codeSpacePath;

    private Runnable onAddButtonPressed;
    private Runnable onCancelButtonPressed;
    private boolean editFlag = false;
    private boolean addFlag = false;
    private int parentIndex;
    private int selectedIndex;
    private ArrayList<ArrayList<String>> requiredChildAttributeList;

    /**
     * initialize（編集）
     * inputフォームを動作するために必要な値やフォーム自体の初期化を実施
     *
     * @param childList        地物情報のリスト
     * @param addAttributeName 追加する属性の名前
     * @param parentIndex      ツリービュー上で選択した属性の親のインデックス
     * @param selectedIndex    ツリービュー上で選択した属性のインデックス
     * 
     */
    public void initialize(ChildList<ADEComponent> childList, String addAttributeName, String addAttributeType,
            int parentIndex,
            int selectedIndex) {
        editFlag = true;
        name.setText(name.getText() + addAttributeName);
        this.childList = childList;
        this.addAttributeName = addAttributeName;
        this.addAttributeType = addAttributeType;
        this.parentIndex = parentIndex;
        this.selectedIndex = selectedIndex;

        if (addAttributeType.matches("gml:CodeType")) {
            // setCodeSpaceField(value);
            String oldCodeSpace = getCodeSpace(childList, parentIndex, selectedIndex);
            valueField.setDisable(true);
            setCodeSpaceField(oldCodeSpace.substring(oldCodeSpace.lastIndexOf("/") + 1));
        } else {
            codeSpaceVbox.setManaged(false);
            codeSpaceVbox.setVisible(false);
        }
        if (addAttributeType.matches("gml:MeasureType") | addAttributeType.matches("gml:LengthType")
                | addAttributeType.matches("gml::MeasureOrNullListType")) {
            getUom(childList, parentIndex, selectedIndex);
        } else {
            uomVbox.setManaged(false);
            uomVbox.setVisible(false);
        }
        String value = getValue(childList, parentIndex, selectedIndex);
        setValueField(value);
    }

    /**
     * initialize（追加）
     * inputフォームを動作するために必要な値やフォーム自体の初期化を実施
     *
     * @param childList                  地物情報のリスト
     * @param parentAttributeName        ツリービュー上で選択した属性の親の名前
     * @param addAttributeName           追加する属性の名前
     * @param addAttributeType           追加する属性のタイプ
     * @param attributeList              追加する属性の名前
     * @param uroAttributeDocument       uroの情報を格納しているドキュメント
     * @param requiredChildAttributeList 追加が必須となっている子属性のリスト
     * @param parentIndex                ツリービュー上で選択した属性の親のインデックス
     * @param selectedIndex              ツリービュー上で選択した属性のインデックス
     */
    public void initialize(ChildList<ADEComponent> childList, String parentAttributeName,
            String addAttributeName, String addAttributeType, ArrayList<ArrayList<String>> attributeList,
            org.w3c.dom.Document uroAttributeDocument, ArrayList<ArrayList<String>> requiredChildAttributeList,
            int parentIndex, int selectedIndex) {
        addFlag = true;
        name.setText(name.getText() + addAttributeName);
        this.childList = childList;
        this.parentAttributeName = parentAttributeName;
        this.addAttributeName = addAttributeName;
        this.addAttributeType = addAttributeType;
        this.attributeList = attributeList;
        this.uroAttributeDocument = uroAttributeDocument;
        this.requiredChildAttributeList = requiredChildAttributeList;
        this.parentIndex = parentIndex;
        this.selectedIndex = selectedIndex;
        if (addAttributeType.matches("gml:CodeType")) {
            valueField.setDisable(true);
        } else {
            codeSpaceVbox.setManaged(false);
            codeSpaceVbox.setVisible(false);
        }
        if (!addAttributeType.matches("gml:MeasureType") & !addAttributeType.matches("gml:LengthType")
                & !addAttributeType.matches("gml::MeasureOrNullListType")) {
            uomVbox.setManaged(false);
            uomVbox.setVisible(false);
        }
        if (parentAttributeName == null) {
            valueVbox.setManaged(false);
            valueVbox.setVisible(false);
        }

        // 親要素の場合は強制的に即時追加（ウィンドウの表示をスキップするため）
        if (requiredChildAttributeList != null && !requiredChildAttributeList.isEmpty()) {
            handleAdd();
        }
    }

    public void requestAdd() {
        handleAdd();
    }

    public void hideButtons() {
        addButton.setVisible(false);
        cancelButton.setVisible(false);
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

    public void setOnAddButtonPressedCallback(Runnable callback) {
        this.onAddButtonPressed = callback;
    }

    public void setOnCancelButtonPressedCallback(Runnable callback) {
        this.onCancelButtonPressed = callback;
    }

    private String getUom(ChildList<ADEComponent> childList, int parentIndex, int index) {
        var adeComponent = childList.get(parentIndex - 1);
        var adeElement = (ADEGenericElement) adeComponent;
        Node content = adeElement.getContent();
        Node parentNode = content.getChildNodes().item(0);
        Node targetNode = parentNode.getChildNodes().item(index);
        String uomValue;
        if (targetNode instanceof Element) {
            Element targetElement = (Element) targetNode;
            uomValue = targetElement.getAttribute("uom");
        } else {
            Element targetElement = (Element) parentNode;
            uomValue = targetElement.getAttribute("uom");
        }
        return uomValue;
    }

    private String getCodeSpace(ChildList<ADEComponent> childList, int parentIndex, int index) {
        var adeComponent = childList.get(parentIndex - 1);
        var adeElement = (ADEGenericElement) adeComponent;
        Node content = adeElement.getContent();
        Node parentNode = content.getChildNodes().item(0);
        Node targetNode = parentNode.getChildNodes().item(index);
        String codeSpace;
        if (targetNode instanceof Element) {
            Element targetElement = (Element) targetNode;
            codeSpace = targetElement.getAttribute("codeSpace");
        } else {
            Element targetElement = (Element) parentNode;
            codeSpace = targetElement.getAttribute("codeSpace");
        }

        return codeSpace;
    }

    private String getValue(ChildList<ADEComponent> childList, int parentIndex, int index) {
        var adeComponent = childList.get(parentIndex - 1);
        var adeElement = (ADEGenericElement) adeComponent;
        Node content = adeElement.getContent();
        Node parentNode = content.getChildNodes().item(0);
        Node targetNode = parentNode.getChildNodes().item(index);
        String value = null;
        if ((targetNode instanceof Element) && (targetNode.getChildNodes().getLength() != 0)) {
            value = targetNode.getChildNodes().item(0).getNodeValue();
        } else if ((parentNode instanceof Element) && (parentNode.getChildNodes().getLength() != 0)) {
            value = parentNode.getChildNodes().item(0).getNodeValue();
        }
        return value;
    }

    private void editAttribute(ChildList<ADEComponent> childList, int parentIndex, int index, String codeSpace,
            String uom, String value) {
        var adeComponent = childList.get(parentIndex - 1);
        var adeElement = (ADEGenericElement) adeComponent;
        Node content = adeElement.getContent();
        Node parentNode = content.getChildNodes().item(0);
        Node targetNode = parentNode.getChildNodes().item(index);

        if (targetNode instanceof Element) {
            Element targetElement = (Element) targetNode;
            if (codeSpace != "") {
                targetElement.setAttribute("codeSpace", "../../codelists/" + codeSpace);
            }
            if (uom != "") {
                targetElement.setAttribute("uom", uom);
            }
            targetElement.setTextContent(value);
        } else {
            Element targetElement = (Element) parentNode;
            if (codeSpace != null) {
                targetElement.setAttribute("codeSpace", "../../codelists/" + codeSpace);
            }
            if (uom != null) {
                targetElement.setAttribute("uom", uom);
            }
            targetElement.setTextContent(value);
        }
    }

    // CodeSpace選択ボタンのイベントハンドラ
    @FXML
    private void handleSelectCodeSpace(ActionEvent event) {
        // CodeSpace選択のロジックを実装
        inputCodeSpace();
    }

    // CodeSpace選択ボタンのイベントハンドラ
    @FXML
    private void handleSelectCodeSpaceValue(ActionEvent event) {
        changeCodeSpaceValue();
    }

    // キャンセルボタンのイベントハンドラ
    @FXML
    private void handleCancel() {
        if (onCancelButtonPressed != null) {
            onCancelButtonPressed.run();
        }
    }

    // 追加ボタンのイベントハンドラ
    @FXML
    private void handleAdd() {
        // 属性の追加処理（例：入力値の検証、データの保存など）
        String codeSpace = codeSpaceField.getText();
        String uom = uomField.getText();
        String value = valueField.getText();
        // ここで取得した値を使用して、データの追加や更新を行います
        if (addFlag) {
            addAttribute(value, codeSpace, uom);
            if (requiredChildAttributeList != null && requiredChildAttributeList.size() != 0) {
                showMultipleAttributesForm(requiredChildAttributeList);
            }
        } else if (editFlag) {
            editAttribute(childList, parentIndex, selectedIndex, codeSpace, uom, value);
        }
        if (onAddButtonPressed != null) {
            onAddButtonPressed.run();
        }
    }

    /**
     * inputCodeSpace
     * CodeSpace属性を入力させ、属性として格納する
     */
    private void inputCodeSpace() {
        String datasetPath = CityGMLEditorApp.getDatasetPath();
        String codeListDirPath = datasetPath + "\\codelists";
        Stage codeTypeStage = new Stage();
        codeTypeStage.setAlwaysOnTop(true);
        final Stage valueStage = new Stage();
        valueStage.setAlwaysOnTop(true);
        File folder = new File(codeListDirPath);
        Parent valueRoot = null;
        FXMLLoader valueLoader = null;
        CodeTypeMenuController codeTypeMenuController = null;
        try {
            FXMLLoader typeLoader = new FXMLLoader(getClass().getResource("fxml/codeSpace-Type-Menu.fxml"));
            Parent typeRoot = typeLoader.load();
            codeTypeMenuController = typeLoader.getController();
            codeTypeMenuController.setList(folder);
            codeTypeStage.setTitle("CodeSpaceの選択");
            codeTypeStage.setScene(new Scene(typeRoot));
            codeTypeStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            valueLoader = new FXMLLoader(getClass().getResource("fxml/codeSpace-Value-Menu.fxml"));
            valueRoot = valueLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final CodeSpaceValueMenuController codeSpaceValueMenuController = valueLoader.getController();
        final Parent finalValueRoot = valueRoot; // root を final にする
        CodeSpaceAttributeInfo codeSpaceAttributeInfo = new CodeSpaceAttributeInfo();
        codeTypeMenuController.setOnSelectCallback(selectedCodeSpace -> {
            String selectedFile = selectedCodeSpace;
            String codeListPath = codeListDirPath + "\\" + selectedFile;
            setCodeSpaceField(selectedFile);
            codeSpacePath = "../../codelists/" + selectedFile;
            codeSpaceAttributeInfo.readCodeType(codeListPath);
            codeSpaceValueMenuController.setCodeType(codeSpaceAttributeInfo.getCodeTypeDocument());
            valueStage.setScene(new Scene(finalValueRoot));
            valueStage.show();

            // リストビューを閉じる
            codeTypeStage.close();
        });

        // codeSpaceValueMenuControllerで表示されるメニューにおいて、選択行為がされたら呼び出される
        codeSpaceValueMenuController.setItemSelectedCallback(selectedItem -> {
            String name = selectedItem.nameProperty().getValue();
            setValueField(name);
            // リストビューを閉じる
            valueStage.close();
        });
    }

    /**
     * changeCodeSpaceValue
     * CodeSpaceの値を変更する
     */
    private void changeCodeSpaceValue() {
        String datasetPath = CityGMLEditorApp.getDatasetPath();
        String codeListDirPath = datasetPath + "\\codelists";
        final Stage valueStage = new Stage();
        valueStage.setAlwaysOnTop(true);
        Parent valueRoot = null;
        FXMLLoader valueLoader = null;
        String codeSpaceValue = codeSpaceField.getText();

        try {
            valueLoader = new FXMLLoader(getClass().getResource("fxml/codeSpace-Value-Menu.fxml"));
            valueRoot = valueLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final CodeSpaceValueMenuController codeSpaceValueMenuController = valueLoader.getController();
        final Parent finalValueRoot = valueRoot; // root を final にする
        CodeSpaceAttributeInfo codeSpaceAttributeInfo = new CodeSpaceAttributeInfo();
        codeSpacePath = codeListDirPath + "\\" + codeSpaceValue;
        codeSpaceAttributeInfo.readCodeType(codeSpacePath);
        codeSpaceValueMenuController.setCodeType(codeSpaceAttributeInfo.getCodeTypeDocument());
        valueStage.setScene(new Scene(finalValueRoot));
        valueStage.show();

        // codeSpaceValueMenuControllerで表示されるメニューにおいて、選択行為がされたら呼び出される
        codeSpaceValueMenuController.setItemSelectedCallback(selectedItem -> {
            String name = selectedItem.nameProperty().getValue();
            setValueField(name);
            // リストビューを閉じる
            valueStage.close();
        });
    }

    /**
     * addAttribute
     * 要素の追加を行う
     *
     * @param value     属性値入力フォーム上の値
     * @param codeSpace codeSpace入力フォーム上の値
     * @param uom       uom入力フォーム上の値
     */
    private void addAttribute(String value, String codeSpace, String uom) {
        String namespaceURI = uroAttributeDocument.getDocumentElement().getAttribute("xmlns:uro");
        if (parentIndex == -2) {
            var adeComponent = childList.get(0);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            Element element = (Element) node;
            org.w3c.dom.Document doc = node.getOwnerDocument();

            if (addAttributeName != null) {
                Element newElement = doc.createElementNS(namespaceURI, addAttributeName);
                ADEGenericElement newAdelement = new ADEGenericElement(newElement);
                childList.add(childList.size(), (ADEComponent) newAdelement);

                // 型要素があるかどうかを確認し、あれば追加
                for (int i = 0; i < attributeList.size(); i++) {
                    if (!attributeList.get(i).isEmpty() && attributeList.get(i).get(3) != null) {
                        if (("uro:" + attributeList.get(i).get(3).toLowerCase())
                                .matches(addAttributeName.toLowerCase())) {
                            Node parentNode = newAdelement.getContent();
                            Element newChildElement = doc.createElementNS(namespaceURI,
                                    "uro:" + attributeList.get(i).get(3));
                            parentNode.appendChild(newChildElement);
                        }
                    }
                }
            }
        } else {
            var adeComponent = childList.get(selectedIndex - 1);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            Element element = (Element) node;

            org.w3c.dom.Document doc = node.getOwnerDocument();
            String nodeTagName = element.getTagName().toLowerCase();
            NodeList childNodeList = node.getChildNodes();
            Node childNode = childNodeList.item(0);
            Element newElement = doc.createElementNS(namespaceURI, addAttributeName);
            newElement.setTextContent(value);

            if (codeSpacePath != "") {
                newElement.setAttribute("codeSpace", codeSpacePath);
            }
            if (uom != "") {
                newElement.setAttribute("uom", uom);
            }

            if (childNode != null) {
                if (nodeTagName.matches(((Element) childNode).getTagName().toLowerCase())) {
                    childNode.appendChild(newElement);
                } else {
                    node.appendChild(newElement);
                }
            } else {
                node.appendChild(newElement);
            }

        }
        // 要素をソート
        sortElement(childList, parentAttributeName, attributeList);
    }

    /**
     * sortElement
     * 要素をソートしてモデル情報に反映する
     *
     * @param childList           選択中の地物のNodeList
     * @param parentAttributeName ソート対象要素の親の名前
     * @param attributeList       パースしたuro要素の情報一覧（ソートの基準となる）
     */
    private void sortElement(ChildList<ADEComponent> childList, String parentAttributeName,
            ArrayList<ArrayList<String>> attributeList) {
        NodeList targetNodeList = null;
        ArrayList<String> nameOrder = new ArrayList<>();
        Element parentElement = null;

        // 名前のリストの作成
        for (ArrayList<String> attribute : attributeList) {
            if (!attribute.isEmpty()) {
                // 各リストの最初の要素をnameOrderに追加
                nameOrder.add("uro:" + attribute.get(0));
            }
        }

        // ソート対象のNodeListの取得
        for (int i = 0; i < childList.size(); i++) {
            var adeComponent = childList.get(i);
            var adeElement = (ADEGenericElement) adeComponent;
            Element element = (Element) adeElement.getContent();

            // 親要素を見つけたら新要素を追加
            if (element.getTagName().equals(parentAttributeName)) {
                parentElement = element;
                targetNodeList = element.getChildNodes();
                Node childNode = targetNodeList.item(0);
                Element childElement = (Element) childNode;
                if (childElement.getTagName().toLowerCase().equals(element.getTagName().toLowerCase())) {
                    targetNodeList = childNode.getChildNodes();
                    parentElement = childElement;
                }
            }
        }

        // NodeListをArrayListに変換
        ArrayList<Node> sortedNodes = new ArrayList<>();
        if (targetNodeList != null) {
            for (int i = 0; i < targetNodeList.getLength(); i++) {
                sortedNodes.add(targetNodeList.item(i));
            }
            // ソート
            Collections.sort(sortedNodes, new Comparator<Node>() {
                @Override
                public int compare(Node node1, Node node2) {
                    int index1 = nameOrder.indexOf(node1.getNodeName());
                    int index2 = nameOrder.indexOf(node2.getNodeName());
                    // nameOrderに含まれていない要素はリストの最後に配置
                    index1 = index1 == -1 ? Integer.MAX_VALUE : index1;
                    index2 = index2 == -1 ? Integer.MAX_VALUE : index2;
                    return Integer.compare(index1, index2);
                }
            });

            clearNodeChildren((Node) parentElement);
            setNewNodeChildren((Node) parentElement, sortedNodes);
        }
    }

    /**
     * clearNodeChildren
     * 子ノードをクリアする
     */
    private void clearNodeChildren(Node node) {
        while (node.hasChildNodes()) {
            node.removeChild(node.getFirstChild());
        }
    }

    /**
     * setNewNodeChildren
     * 新しいNodeListをNodeに格納する
     *
     * @param node       親ノード
     * @param childNodes 追加したいノード
     */
    private void setNewNodeChildren(Node node, ArrayList<Node> childNodes) {
        for (int i = 0; i < childNodes.size(); i++) {
            node.appendChild(childNodes.get(i));
        }
    }

    private void showMultipleAttributesForm(ArrayList<ArrayList<String>> requiredChildAttributeList) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/multiple-attributes-form.fxml"));
            Parent root = loader.load();
            MultipleAttributesFormController controller = loader.getController();
            controller.loadAttributeForms(requiredChildAttributeList, childList, uroAttributeDocument,
                    addAttributeName, attributeList, childList.size());
            Stage stage = new Stage();
            stage.setAlwaysOnTop(true);
            stage.setTitle("必須属性の入力");
            stage.setScene(new Scene(root));
            stage.show();
            // ウィンドウを閉じるリクエストがあったときのイベントハンドラを設定
            stage.setOnHidden(event -> {
                onAddButtonPressed.run();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
