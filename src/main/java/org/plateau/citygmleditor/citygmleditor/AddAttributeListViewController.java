package org.plateau.citygmleditor.citygmleditor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.plateau.citygmleditor.citymodel.AttributeItem;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class AddAttributeListViewController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> attributeListView;

    private Stage stage;

    private ArrayList<ArrayList<String>> attributeList;
    private int selectedIndex;
    private String selectedTagName;

    public void initialize(ArrayList<ArrayList<String>> attributeList, Stage stage, Consumer<AddAttributeListViewController> onAdd) {
        this.attributeList = attributeList;
        this.stage = stage;

        // 要素リスト
        for (ArrayList<String> attribute : attributeList) {
            attributeListView.getItems().add(attribute.get(0));
        }

        // メニュー内の要素をダブルクリックで要素を追加
        attributeListView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                selectedTagName = attributeListView.getSelectionModel().getSelectedItem();
                selectedIndex = attributeListView.getSelectionModel().getSelectedIndex();
                // 要素を追加
                onAdd.accept(this);
                stage.close();
            }
        });

        // 検索欄
        searchField = new TextField();
        searchField.setPromptText("Search");

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
    }

    public static void createModal(
            TreeItem<AttributeItem> parentItem, boolean isRoot, Consumer<AddAttributeListViewController> onAdd) {
        AddAttributeListViewController controller;
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        try {
            FXMLLoader loader = new FXMLLoader(AddAttributeListViewController.class.getResource("fxml/add-attribute-list-view.fxml"));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("要素の追加");
            stage.setWidth(500);
            stage.setHeight(300);

            controller = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<ArrayList<String>> attributeList = getUroList(parentItem, isRoot);

        if (attributeList.isEmpty()) {
            showAndWaitAlert();
            return;
        }

        controller.initialize(attributeList, stage, onAdd);

        stage.showAndWait();
    }

    private static void showAndWaitAlert() {
        // アラートを作成
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(AddAttributeListViewController.class.getResource("viewer.css")).toExternalForm());
        alert.getDialogPane().getStyleClass().add("alert");
        alert.setTitle("追加エラー");
        alert.setHeaderText(null);
        alert.setContentText("追加できる要素がありません。");

        // アラートを表示
        alert.showAndWait();
    }

    /**
     * getUroList
     * 追加メニューの一覧に乗せるuro要素の一覧を返す
     *
     * @param parentItem 検索対象の親属性要素。nullの場合は地物直下のuro要素の一覧を返す。
     * @return メニューに表示させる要素リスト
     */
    private static ArrayList<ArrayList<String>> getUroList(TreeItem<AttributeItem> parentItem, boolean isRoot) {
        if (parentItem == null)
            return null;

        ArrayList<ArrayList<String>> attributeList = new ArrayList<ArrayList<String>>();
        ArrayList<String> treeViewChildItemList = new ArrayList<String>();
        var parentKey = parentItem.getValue().keyProperty().get();
        // xsdでは接頭辞無しで扱われるため"uro:"を削除
        if (parentKey.startsWith("uro:"))
            parentKey = parentKey.substring(4);

        // 追加済みの子要素の名前を取得
        // 子アイテムのリストを取得
        var children = parentItem.getChildren();
        // 子アイテムを処理する
        for (TreeItem<AttributeItem> child : children) {
            treeViewChildItemList.add(child.getValue().keyProperty().get());
        }

        // Root要素の追加
        if (isRoot) {
            // 追加済みのルート要素の名前を取得
            ArrayList<String> treeViewRootItemList = new ArrayList<String>();
            for (TreeItem<AttributeItem> item : parentItem.getChildren()) {
                treeViewRootItemList.add(item.getValue().keyProperty().get());
            }

            // Uro要素の取得
            var uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
            Element rootNode = uroAttributeDocument.getDocumentElement();
            Element targetElement;
            NodeList elementNodeList = rootNode.getChildNodes();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                targetElement = (Element) node;
                // すでに追加済みのアイテムは除く
                if (treeViewRootItemList.contains("uro:" + targetElement.getAttribute("name")))
                    continue;

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
        } else {
            // 第二階層以下の要素の追加
            // 追加対象の基準となる親要素を取得
            var uroAttributeDocument = CityGMLEditorApp.getUroAttributeDocument();
            Element rootNode = uroAttributeDocument.getDocumentElement();
            Element targetElement = rootNode;
            NodeList elementNodeList = rootNode.getChildNodes();

            for (int i = 0; i < elementNodeList.getLength(); i++) {
                Node node = elementNodeList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                Element element = (Element) node;
                if (parentKey.equals(element.getAttribute("name"))) {
                    targetElement = element;
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
                    if (parentKey.toLowerCase().matches(element.getAttribute("name").toLowerCase()))
                        continue;

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
        return attributeList;
    }

    public ArrayList<ArrayList<String>> getAttributeList() {
        return attributeList;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public String getSelectedTagName() {
        return selectedTagName;
    }
}
