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
import org.plateau.citygmleditor.citymodel.AttributeItem;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class AttributeEditorController implements Initializable {
    public TreeTableView<AttributeItem> attributeTreeTable;
    public TreeTableColumn<AttributeItem, String> keyColumn;
    public TreeTableColumn<AttributeItem, String> valueColumn;

    @FXML
    private TitledPane titledPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var activeFeatureProperty = CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty();
        attributeTreeTable.rootProperty().bind(new ObjectBinding<>() {
            {
                bind(activeFeatureProperty);
            }

            @Override
            protected TreeItem<AttributeItem> computeValue() {
                var feature = activeFeatureProperty.get();
                if (feature == null)
                    return null;

                // タイトル更新
                titledPane.setText(String.format("属性情報（%s）", feature.getGMLObject().getId()));

                var root = new TreeItem<>(new AttributeItem("", ""));
                var gmlObject = feature.getGMLObject();

                {
                    var attribute = new AttributeItem(
                            "measuredHeight",
                            Double.toString(gmlObject.getMeasuredHeight().getValue())
                    );
                    attribute.valueProperty().addListener((observable, oldValue, newValue) -> {
                        gmlObject.getMeasuredHeight().setValue(Double.parseDouble(newValue));
                    });
                    root.getChildren().add(new TreeItem<>(attribute));
                }

                {
                    addADEPropertyToTree(feature, root);
                }

                root.setExpanded(true);

                return root;
            }
        });

        attributeTreeTable.setShowRoot(false);

        keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("key"));
        valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));

//                attributeTreeTable.setOnKeyPressed(t -> {
//                    if (t.getCode() == KeyCode.SPACE) {
//                        TreeItem<Node> selectedItem = attributeTreeTable.getSelectionModel().getSelectedItem();
//                        if (selectedItem != null) {
//                            Node node = selectedItem.getValue();
//                            node.setVisible(!node.isVisible());
//                        }
//                        t.consume();
//                    }
//                });


        valueColumn.setCellFactory(
                TextFieldTreeTableCell.forTreeTableColumn());
        valueColumn.setOnEditCommit(event -> {
            TreeItem<AttributeItem> editedItem = event.getRowValue();
            AttributeItem attribute = editedItem.getValue();
            attribute.valueProperty().set(event.getNewValue());
        });
    }

    private static void addADEPropertyToTree(BuildingView building, TreeItem<AttributeItem> root) {
        for (var adeComponent : building.getGMLObject().getGenericApplicationPropertyOfAbstractBuilding()) {
            var adeElement = (ADEGenericElement) adeComponent;
            addXMLElementToTree(adeElement.getContent(), root);
        }
    }

    private static void addXMLElementToTree(Node node, TreeItem<AttributeItem> root) {
        // 子が末尾の要素であるかチェック
        var firstChild = node.getFirstChild();
        if (node.getChildNodes().getLength() == 1 && firstChild instanceof CharacterData) {
            // 子の内容を属性値として登録して再帰処理を終了
            var attribute = new AttributeItem(node.getNodeName(), firstChild.getNodeValue());
            attribute.valueProperty().addListener((observable, oldValue, newValue) -> {
                firstChild.setNodeValue(newValue);
            });
            var item = new TreeItem<>(attribute);
            root.getChildren().add(item);
            return;
        }

        var item = new TreeItem<>(
                new AttributeItem(node.getNodeName(), "", false)
        );
        item.setExpanded(true);

        // XMLの子要素を再帰的に追加
        var children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            var xmlElement = children.item(i);

            // ここでの文字列要素はタブ・改行なので飛ばす
            if (xmlElement instanceof CharacterData)
                continue;

            addXMLElementToTree(xmlElement, item);
        }

        root.getChildren().add(item);
    }
}
