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
    private Label name; // 名前ラベル（使用されていないため、可能であれば具体的な使用方法を検討）

    @FXML
    private TextField codeSpaceField; // CodeSpaceの入力フィールド

    @FXML
    private TextField uomField; // UOMの入力フィールド

    @FXML
    private TextField valueField; // 属性値の入力フィールド
    @FXML
    private VBox codeSpaceVbox;

    @FXML
    private VBox uomVbox;
    @FXML
    private VBox valueVbox;

    @FXML
    private Label purposeLabel;

    private ChildList<ADEComponent> childList;
    private String parentAttributeName;
    private String addAttributeName;
    private String type;
    private ArrayList<ArrayList<String>> attributeList;
    private org.w3c.dom.Document uroAttributeDocument;
    private String codeSpacePath;

    private Runnable onAddButtonPressed;
    private boolean editFlag = false;
    private boolean addFlag = false;
    private int parentIndex;
    private int selectedIndex;

    public void initialize(ChildList<ADEComponent> childList, String addAttributeName, String type, int parentIndex,
            int selectedIndex) {
        editFlag = true;
        name.setText(name.getText() + addAttributeName);
        purposeLabel.setText("属性の編集");
        this.childList = childList;
        this.addAttributeName = addAttributeName;
        this.type = type;
        this.parentIndex = parentIndex;
        this.selectedIndex = selectedIndex;

        if (type.matches("gml:CodeType")) {
            // setCodeSpaceField(value);
            String oldCodeSpace = getCodeSpace(childList, parentIndex, selectedIndex);
            setCodeSpaceField(oldCodeSpace.substring(oldCodeSpace.lastIndexOf("/") + 1));
        } else {
            codeSpaceVbox.setManaged(false);
            codeSpaceVbox.setVisible(false);
        }
        if (type.matches("gml:MeasureType") | type.matches("gml:LengthType")
                | type.matches("gml::MeasureOrNullListType")) {
            getUom(childList, parentIndex, selectedIndex);
            // setUomField(value);
        } else {
            uomVbox.setManaged(false);
            uomVbox.setVisible(false);
        }
        String value = getValue(childList, parentIndex, selectedIndex);
        setValueField(value);
    }

    public void initialize(ChildList<ADEComponent> childList, String parentAttributeName,
            String addAttributeName, String type, ArrayList<ArrayList<String>> attributeList,
            org.w3c.dom.Document uroAttributeDocument) {
        addFlag = true;
        name.setText(name.getText() + addAttributeName);
        purposeLabel.setText("属性の追加");
        this.childList = childList;
        this.parentAttributeName = parentAttributeName;
        this.addAttributeName = addAttributeName;
        this.type = type;
        this.attributeList = attributeList;
        this.uroAttributeDocument = uroAttributeDocument;

        if (!type.matches("gml:CodeType")) {
            codeSpaceVbox.setManaged(false);
            codeSpaceVbox.setVisible(false);
        }
        if (!type.matches("gml:MeasureType") & !type.matches("gml:LengthType")
                & !type.matches("gml::MeasureOrNullListType")) {
            uomVbox.setManaged(false);
            uomVbox.setVisible(false);
        }
        if (parentAttributeName == null) {
            valueVbox.setManaged(false);
            valueVbox.setVisible(false);
        }
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
        String value;
        if (targetNode instanceof Element) {
            value = targetNode.getChildNodes().item(0).getNodeValue();
        } else {
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
            if (codeSpace != null) {
                targetElement.setAttribute("codeSpace", "../../codelists/" + codeSpace);
            }
            if (uom != null) {
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

    // キャンセルボタンのイベントハンドラ
    @FXML
    private void handleCancel(ActionEvent event) {
        // キャンセル処理（例：フォームのクリア、ウィンドウのクローズなど）
    }

    // 追加ボタンのイベントハンドラ
    @FXML
    private void handleAdd(ActionEvent event) {
        // 属性の追加処理（例：入力値の検証、データの保存など）
        String codeSpace = codeSpaceField.getText();
        String uom = uomField.getText();
        String value = valueField.getText();
        // ここで取得した値を使用して、データの追加や更新を行います
        if (addFlag) {
            addAttribute(value, codeSpace, uom);
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
     *
     * @param doc     現在選択している地物のDocument
     * @param element 追加予定のElement
     */
    private void inputCodeSpace() {
        String datasetPath = CityGMLEditorApp.getDatasetPath();
        String codeListDirPath = datasetPath + "\\codelists";
        Stage codeTypeStage = new Stage();
        final Stage valueStage = new Stage();
        ListView<String> listView = new ListView<>();
        File folder = new File(codeListDirPath);
        // final CodeSpaceValueMenuController codeSpaceValueMenuController = null;
        Parent root = null;
        FXMLLoader loader = null;

        // フォルダ内のファイル名をリストビューに追加
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            listView.getItems().add(file.getName());
        }
        try {
            loader = new FXMLLoader(getClass().getResource("fxml/codeSpace-Menu.fxml"));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final CodeSpaceValueMenuController codeSpaceValueMenuController = loader.getController();
        final Parent finalRoot = root; // root を final にする
        CodeSpaceAttributeInfo codeSpaceAttributeInfo = new CodeSpaceAttributeInfo();

        // リストビューのアイテムがダブルクリックされた場合の処理
        listView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedFile = listView.getSelectionModel().getSelectedItem();
                String codeListPath = codeListDirPath + "\\" + selectedFile;
                setCodeSpaceField(selectedFile);
                // element.setAttribute("codeSpace", "../../codelists/" + selectedFile);
                codeSpacePath = "../../codelists/" + selectedFile;
                codeSpaceAttributeInfo.readCodeType(codeListPath);
                codeSpaceValueMenuController.setCodeType(codeSpaceAttributeInfo.getCodeTypeDocument());
                valueStage.setScene(new Scene(finalRoot));
                valueStage.show();

                // リストビューを閉じる
                codeTypeStage.close();
            }
        });

        // codeSpaceValueMenuControllerで表示されるメニューにおいて、選択行為がされたら呼び出される
        codeSpaceValueMenuController.setItemSelectedCallback(selectedItem -> {
            String name = selectedItem.nameProperty().getValue();
            setValueField(name);
            // element.setTextContent(name);
            // リストビューを閉じる
            valueStage.close();
            // refreshListView();
        });

        // 配置
        VBox vbRoot = new VBox();
        vbRoot.setAlignment(Pos.CENTER);
        vbRoot.setSpacing(20);
        vbRoot.getChildren().addAll(listView);

        codeTypeStage.setTitle("codeSpaceの選択");
        codeTypeStage.setWidth(500);
        codeTypeStage.setHeight(300);
        codeTypeStage.setScene(new Scene(vbRoot));
        codeTypeStage.show();
    }

    /**
     * addAttribute
     * 要素の追加を行う
     *
     * @param childList           選択中の地物の要素リスト
     * @param parentAttributeName 選択中のリストビューのアイテム名
     * @param addAttributeName    追加する要素の名前
     * @param type                追加する要素が持つタイプ
     * @param attributeList       パースしたuro要素の情報一覧
     */
    private void addAttribute(String value, String codeSpace, String uom) {
        String namespaceURI = uroAttributeDocument.getDocumentElement().getAttribute("xmlns:uro");
        if (parentAttributeName == null) {
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
                    if (!attributeList.get(i).isEmpty() && attributeList.get(i).get(2) != null) {
                        if (("uro:" + attributeList.get(i).get(2).toLowerCase())
                                .matches(addAttributeName.toLowerCase())) {
                            Node parentNode = newAdelement.getContent();
                            Element newChildElement = doc.createElementNS(namespaceURI,
                                    "uro:" + attributeList.get(i).get(2));
                            parentNode.appendChild(newChildElement);
                        }
                    }
                }
            }
        } else {
            for (int i = 0; i < childList.size(); i++) {
                var adeComponent = childList.get(i);
                var adeElement = (ADEGenericElement) adeComponent;
                Node node = adeElement.getContent();
                Element element = (Element) node;
                String nodeTagName = element.getTagName();

                // 親要素を見つけたら新要素を追加
                if (nodeTagName.equals(parentAttributeName)) {
                    NodeList childNodeList = node.getChildNodes();
                    Node childNode = childNodeList.item(0);
                    nodeTagName = nodeTagName.toLowerCase();
                    org.w3c.dom.Document doc = element.getOwnerDocument();
                    Element newElement = doc.createElementNS(namespaceURI, addAttributeName);
                    newElement.setTextContent(value);

                    if (codeSpace != null) {
                        newElement.setAttribute("codeSpace", codeSpacePath);
                    } else if (uom != null) {
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
}
