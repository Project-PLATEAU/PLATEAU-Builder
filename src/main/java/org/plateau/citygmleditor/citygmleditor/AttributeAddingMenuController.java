package org.plateau.citygmleditor.citygmleditor;

import javafx.beans.binding.ObjectBinding;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import org.citygml4j.model.citygml.ade.generic.ADEGenericElement;
import org.plateau.citygmleditor.citymodel.UroAttributeInfo;
import org.plateau.citygmleditor.citymodel.AttributeItem;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.validation.AttributeValidator;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import java.beans.VetoableChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.Flow.Subscriber;

import javax.swing.text.Document;
import javax.swing.text.ElementIterator;

import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeTableRow;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.common.child.ChildList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.citygml4j.model.citygml.ade.ADEComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TextInputDialog;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class AttributeAddingMenuController implements Initializable {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> attributeListView;

    private TreeItem<AttributeItem> selectedItem;
    private TreeTableView<AttributeItem> attributeTreeTable;
    private org.w3c.dom.Document uroAttributeDocument;
    private FXMLLoader loader;

    private Parent root;

    public AttributeAddingMenuController() {
        try {

            loader = new FXMLLoader(getClass()
                    .getResource("fxml/add-attribute-list-view.fxml"));
            URL url = loader.getLocation();
            System.out.println("Loaded FXML Path: " + url.toExternalForm());
            // root = loader.load();
            // System.out.println(root);
            // var controller = loader.getController();
            // System.out.println(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.err.println(attributeListView);
    }

    /**
     * showListView
     * 追加メニューの一覧に乗せるUro要素の一覧を表示し、追加を行う
     *
     * @param childList                選択中の地物の要素リスト
     * @param selectedAttributeKeyName 選択中のリストビューのアイテム名
     */
    @FXML
    public ArrayList<ArrayList<String>> showListView(ChildList<ADEComponent> childList,
            String selectedAttributeKeyName, TreeItem<AttributeItem> selectedItem,
            TreeTableView<AttributeItem> attributeTreeTable) throws IOException {
        this.selectedItem = selectedItem;
        this.attributeTreeTable = attributeTreeTable;

        Stage pStage = new Stage();
        // System.out.println(loader.getController().toString());
        ArrayList<ArrayList<String>> attributeList = getUroList(selectedAttributeKeyName);
        if (attributeList.size() == 0) {
            // アラートを作成
            Alert alert = new Alert(AlertType.WARNING);
            alert.getDialogPane().getStylesheets().add(getClass().getResource("viewer.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("alert");
            alert.setTitle("追加エラー");
            alert.setHeaderText(null);
            alert.setContentText("追加できる要素がありません。");

            // アラートを表示
            alert.showAndWait();
            return null;
        }
        // ListView
        // attributeListView = new ListView<String>();

        for (ArrayList<String> attribute : attributeList) {
            attributeListView.getItems().add(attribute.get(0));
            System.out.println(attribute.get(0));
        }

        // メニュー内の要素をダブルクリックで要素を追加
        attributeListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedItemName = attributeListView.getSelectionModel().getSelectedItem();
                int selectedIndex = attributeListView.getSelectionModel().getSelectedIndex();
                // 要素を追加
                addAttribute(childList, selectedAttributeKeyName, "uro:" + selectedItemName,
                        attributeList.get(selectedIndex).get(1), attributeList);
                pStage.close();
            }
        });

        // // 検索欄
        searchField = new TextField();
        // searchField.setPromptText("Search");

        // 検索用リスナーを追加
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            attributeListView.getItems().clear();
            if (newValue == null || newValue.isEmpty()) {
                for (var attribute : attributeList) {
                    attributeListView.getItems().add(attribute.get(0));
                }
            } else {
                for (var attribute : attributeList) {
                    if (attribute.get(0).toLowerCase().startsWith(newValue.toLowerCase())) {
                        attributeListView.getItems().add(attribute.get(0));
                    }
                }
            }
        });

        // 配置
        // VBox vbRoot = new VBox();
        // vbRoot.setAlignment(Pos.CENTER);
        // vbRoot.setSpacing(20);
        // vbRoot.getChildren().addAll(searchField);
        // vbRoot.getChildren().addAll(attributeListView);
        pStage.setScene(new Scene(root));
        pStage.setTitle("要素の追加");
        pStage.setWidth(500);
        pStage.setHeight(300);
        pStage.show();
        return attributeList;
    }

    /**
     * getUroList
     * 追加メニューの一覧に乗せるUro要素の一覧を返す
     *
     * @param targetName 地物情報のリスト
     * @return メニューに表示させる要素リスト
     */
    private ArrayList<ArrayList<String>> getUroList(String targetName) {
        ArrayList<ArrayList<String>> attributeList = new ArrayList<ArrayList<String>>();
        ArrayList<String> treeViewRootItemList = new ArrayList<String>();
        ArrayList<String> treeViewChildItemList = new ArrayList<String>();
        // 追加済みのルート要素の名前を取得
        for (TreeItem<AttributeItem> item : attributeTreeTable.getRoot().getChildren()) {
            treeViewRootItemList.add(item.getValue().keyProperty().get());
        }

        // 追加済みの子要素の名前を取得
        if (selectedItem != null) {
            // 子アイテムのリストを取得
            var children = selectedItem.getChildren();
            // 子アイテムを処理する
            for (TreeItem<AttributeItem> child : children) {
                treeViewChildItemList.add(child.getValue().keyProperty().get());
            }
        }
        if (targetName == null) {
            // Root要素の追加
            // Uro要素の取得
            uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
            Node rootNode = uroAttributeDocument.getDocumentElement();
            Element targetElement = (Element) rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    targetElement = (Element) node;
                    // すでに追加済みのアイテムは除く
                    if (!treeViewRootItemList.contains("uro:" + targetElement.getAttribute("name"))) {
                        ArrayList<String> attributeSet = new ArrayList<String>();
                        attributeSet.add(targetElement.getAttribute("name"));
                        attributeSet.add(targetElement.getAttribute("type"));

                        Node childNode = node.getChildNodes().item(0);
                        Element childElement = (Element) childNode;
                        if (childElement != null) {
                            attributeSet.add(childElement.getAttribute("name"));
                        } else {
                            attributeSet.add(null);
                        }
                        attributeList.add(attributeSet);
                    }
                }
            }
        } else {
            // 第二階層以下の要素の追加
            targetName = targetName.substring(4);

            // 追加対象の基準となる親要素を取得
            uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
            Node rootNode = uroAttributeDocument.getDocumentElement();
            Element targetElement = (Element) rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (targetName.equals(element.getAttribute("name"))) {
                        targetElement = (Element) node;
                    }
                }
            }
            if (!targetElement.getTagName().equals("uro")) {
                // 基準となる要素の子要素を取得
                NodeList targetNodeList = targetElement.getElementsByTagName("xs:element");

                for (int j = 0; j < targetNodeList.getLength(); j++) {
                    Node node = targetNodeList.item(j);
                    Element element = (Element) node;
                    int count = 0;
                    for (String itemName : treeViewChildItemList) {
                        if (itemName.equals("uro:" + element.getAttribute("name"))) {
                            count++;
                        }
                    }
                    if (!targetName.toLowerCase().matches(element.getAttribute("name").toLowerCase())) {
                        String maxOccurs = element.getAttribute("maxOccurs");
                        int max;
                        if (maxOccurs.equals("unbounded")) {
                            max = Integer.MAX_VALUE;
                        } else if (maxOccurs == "") {
                            max = 1;
                        } else {
                            max = Integer.parseInt(maxOccurs);
                        }
                        if (count < max) {
                            ArrayList<String> attributeSet = new ArrayList<>();
                            attributeSet.add(element.getAttribute("name"));
                            attributeSet.add(element.getAttribute("type"));
                            attributeSet.add(element.getAttribute("minOccurs"));
                            attributeList.add(attributeSet);
                        }
                    }
                }
            }
        }
        return attributeList;
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
    private void addAttribute(ChildList<ADEComponent> childList, String parentAttributeName,
            String addAttributeName, String type, ArrayList<ArrayList<String>> attributeList) {
        String namespaceURI = uroAttributeDocument.getDocumentElement().getAttribute("xmlns:uro");

        if (parentAttributeName == null) {
            // ルート要素を追加する際の処理
            var adeComponent = childList.get(0);
            var adeElement = (ADEGenericElement) adeComponent;
            Node node = adeElement.getContent();
            Element element = (Element) node;
            org.w3c.dom.Document doc = node.getOwnerDocument();

            if (addAttributeName != null) {
                Element newElement = doc.createElementNS(namespaceURI, addAttributeName);
                newElement.setTextContent("NULL");
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
            // 第二階層以下を追加する際の処理
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
                    newElement.setTextContent("NULL");

                    if (type.matches("gml:CodeType")) {
                        inputCodeSpace(newElement);
                    } else if (type.matches("gml:MeasureType") | type.matches("gml:LengthType")
                            | type.matches("gml::MeasureOrNullListType")) {
                        inputUom(newElement);
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
     * CodeSpace
     * CodeSpace属性を入力させ、属性として格納する
     */
    private void inputCodeSpace(Element element) {
        String datasetPath = CityGMLEditorApp.getDatasetPath();
        String codeListPath = datasetPath + "\\codelists";
        Stage pStage = new Stage();
        ListView<String> listView = new ListView<>();
        File folder = new File(codeListPath);

        // フォルダ内のファイル名をリストビューに追加
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            listView.getItems().add(file.getName());
        }

        // リストビューのアイテムがダブルクリックされた場合の処理
        listView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                String selectedFile = listView.getSelectionModel().getSelectedItem();
                element.setAttribute("codeSpace", "../../codelists/" + selectedFile);
                // リストビューを閉じる
                pStage.close();
            }
        });

        // 配置
        VBox vbRoot = new VBox();
        vbRoot.setAlignment(Pos.CENTER);
        vbRoot.setSpacing(20);
        vbRoot.getChildren().addAll(listView);

        pStage.setTitle("codeSpaceの選択");
        pStage.setWidth(500);
        pStage.setHeight(300);
        pStage.setScene(new Scene(vbRoot));
        pStage.show();
    }

    /**
     * inputUom
     * Uom属性を入力させ、属性として格納する
     */
    private void inputUom(Element element) {
        // テキスト入力ダイアログの作成
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("uom属性入力フォーム");
        dialog.setHeaderText("原則：長さの単位は m,面積の単位は m2,時間の単位は hour");
        dialog.setContentText("uom:");

        // ダイアログを表示し、結果を取得
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(uomValue -> {
            element.setAttribute("uom", uomValue);
        });
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

    /**
     * printNode
     * （デバッグ用）ノードリストを可視化する
     * 子ノードがあればprintNode()を呼び出す
     *
     * @param nodeList 表示対象のノードリスト
     */
    private void printNodeList(NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            printNode(node, 0);
        }
    }

    /**
     * printNode
     * （デバッグ用）ノードを可視化する
     *
     * @param node   表示対象のノード
     * @param indent 子を表すためのインデントの数
     */
    private void printNode(Node node, int indent) {
        // インデント文字列の生成
        String indentString = new String(new char[indent]).replace("\0", "    ");

        // ノードの基本情報を表示
        System.out.println(indentString + "Node Name: " + node.getNodeName() + ", Type: " + node.getNodeType());
        // 子ノードがある場合は再帰的に表示
        if (node.hasChildNodes()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                printNode(children.item(i), indent + 1);
            }
        }
    }
}
