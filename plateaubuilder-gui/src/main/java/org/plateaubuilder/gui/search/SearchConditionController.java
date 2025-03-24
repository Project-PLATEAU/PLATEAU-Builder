package org.plateaubuilder.gui.search;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.filters.FeatureFilterBuilder;
import org.plateaubuilder.core.editor.filters.IFeatureFilter;
import org.plateaubuilder.core.editor.filters.expressions.Operator;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class SearchConditionController {
    private List<String> typeList;

    private List<IFeatureView> featureList;

    Node root;

    @FXML
    ComboBox<String> comboBoxGMLType;

    @FXML
    ComboBox<String> comboBoxAttributeName;

    @FXML
    ComboBox<Operator> comboBoxOperator;

    @FXML
    TextField attributeValue;

    @FXML
    Button buttonDelete;

    public SearchConditionController(List<String> typeList, List<IFeatureView> featureList) {
        try {
            this.typeList = typeList;
            this.featureList = featureList;
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SearchConditionController.class.getResource("search-condition.fxml")));
            loader.setController(this);
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Node getRoot() {
        return root;
    }

    public void initialize() {
        comboBoxGMLType.getItems().addAll(typeList);
        comboBoxGMLType.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                comboBoxAttributeName.getItems().clear();
                for (var name : Editor.getAttributeSchema().getAttributeNames(newValue)) {
                    var isSetAttribute = false;
                    for (var feature : featureList) {
                        if (!feature.getFeatureType().equals(newValue)) {
                            continue;
                        }
                        if (feature.isSetAttribute(name)) {
                            isSetAttribute = true;
                            break;
                        }
                    }
                    if (isSetAttribute) {
                        comboBoxAttributeName.getItems().add(name);
                    }
                }
            }
        });

        Callback<ListView<Operator>, ListCell<Operator>> cellFactoryOperator = (ListView<Operator> param) -> new ListCell<Operator>() {
            @Override
            protected void updateItem(Operator item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getOperator());
                }
            }
        };
        comboBoxOperator.getItems().addAll(FXCollections.observableArrayList(Operator.values()));
        comboBoxOperator.setButtonCell(cellFactoryOperator.call(null));
        comboBoxOperator.setCellFactory(cellFactoryOperator);
        comboBoxOperator.setValue(Operator.NONE);
    }

    public IFeatureFilter createFilter() {
        return createFeatureFilterBuilder().build();
    }

    public FeatureFilterBuilder createFeatureFilterBuilder() {
        return new FeatureFilterBuilder().featureType(comboBoxGMLType.getValue()).attributeName(comboBoxAttributeName.getValue())
                .operator(comboBoxOperator.getValue()).value(attributeValue.getText());
    }

    public void setFeatureType(String featureType) {
        comboBoxGMLType.setValue(featureType);
    }

    public void setAttributeName(String attributeName) {
        comboBoxAttributeName.setValue(attributeName);
    }

    public void setOperator(Operator operator) {
        comboBoxOperator.setValue(operator);
    }

    public void setValue(String value) {
        attributeValue.setText(value);
    }
}
