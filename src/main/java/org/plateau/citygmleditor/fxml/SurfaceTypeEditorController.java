package org.plateau.citygmleditor.fxml;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.citygml4j.model.citygml.CityGMLClass;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.control.BuildingSurfaceTypeView;
import org.plateau.citygmleditor.control.SurfacePolygonSection;
import org.plateau.citygmleditor.utils.ColorUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class SurfaceTypeEditorController implements Initializable {
    public VBox list;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private BuildingSurfaceTypeView view;
    private SurfacePolygonSection section;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // 全てのトグルがオフになる状態は回避する
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }

            if (view == null || section == null)
                return;

            view.updateSurfaceType(section, (CityGMLClass) newToggle.getUserData());
        });

        var viewMode = CityGMLEditorApp.getCityModelViewMode();
        list.setVisible(viewMode.isSurfaceViewModeProperty().get());
        viewMode.isSurfaceViewModeProperty().addListener((observable, oldValue, newValue) -> {
            list.setVisible(newValue);
        });

        try {
            for (var entry : BuildingSurfaceTypeView.buildingSurfaceColors().entrySet()) {
                var listItem = FXMLLoader.<ToggleButton>load(Objects
                        .requireNonNull(CityGMLEditorApp.class.getResource("fxml/surface-type-editor-list-item.fxml")));
                listItem.setStyle("-fx-text-fill: " + ColorUtils.getWebString(entry.getValue()));
                listItem.setText(entry.getKey().name());
                listItem.setToggleGroup(toggleGroup);
                listItem.setUserData(entry.getKey());
                list.getChildren().add(listItem);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var featureSelection = CityGMLEditorApp.getFeatureSellection();
        featureSelection.getActiveFeatureProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {

                if (newValue == null || newValue.getLOD2Solid() == null) {
                    view = null;
                    return;
                }
                view = newValue.getLOD2Solid().getSurfaceTypeView();
            }
        }));

        featureSelection.getSurfacePolygonSectionProperty().addListener(((observable, oldValue, newValue) -> {
            section = newValue;

            if (section == null)
                return;

            for (var toggle : toggleGroup.getToggles()) {
                if ((CityGMLClass) toggle.getUserData() == view.getSurfaceType(section)) {
                    toggleGroup.selectToggle(toggle);
                    break;
                }
            }
            updateToggle();
        }));
    }

    private void updateToggle() {
        if (section == null || view == null)
            return;

        for (var toggle : toggleGroup.getToggles()) {
            if (toggle.getUserData() == view.getSurfaceType(section)) {
                toggleGroup.selectToggle(toggle);
                break;
            }
        }
    }
}
