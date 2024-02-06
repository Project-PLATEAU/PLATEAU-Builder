package org.plateau.citygmleditor.fxml.featureinfo;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.text.Text;
import org.plateau.citygmleditor.citygmleditor.CityGMLEditorApp;
import org.plateau.citygmleditor.fxml.UIConstants;

import java.net.URL;
import java.util.ResourceBundle;

public class FeatureInfoController implements Initializable {
    @FXML
    private Text featureIDText;

    @FXML
    private Text featureTypeText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty()
                .addListener((observable, oldFeature, feature) -> {
                    if (feature == null)
                        return;

                    featureIDText.setText("地物ID：" + feature.getGMLObject().getId());
                    featureTypeText.setText("地物型：" + UIConstants.buildingTypeDescription(feature.getGMLObject().getCityGMLClass()));
                });
    }
}
