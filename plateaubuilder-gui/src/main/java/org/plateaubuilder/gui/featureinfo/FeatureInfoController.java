package org.plateaubuilder.gui.featureinfo;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.plateaubuilder.core.citymodel.citygml.ADEGenericComponent;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.gui.UIConstants;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class FeatureInfoController implements Initializable {
    @FXML
    private ToggleButton lod1Toggle;
    @FXML
    private ToggleButton lod2Toggle;
    @FXML
    private ToggleButton lod3Toggle;
    @FXML
    private Text featureIDText;
    @FXML
    private Text featureTypeText;
    @FXML
    private VBox featureInfoContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        featureInfoContainer.setVisible(false);

        Editor.getFeatureSellection().getActiveFeatureProperty()
                .addListener((observable, oldFeature, feature) -> {
                    var toggles = new ArrayList<ToggleButton>();
                    toggles.add(lod1Toggle);
                    toggles.add(lod2Toggle);
                    toggles.add(lod3Toggle);

                    for (int lod = 1; lod <= 3; ++lod) {
                        var toggle = toggles.get(lod - 1);
                        if (oldFeature == null || oldFeature.getLODView(lod) == null)
                            continue;

                        toggle.selectedProperty().unbindBidirectional(((Node) oldFeature.getLODView(lod)).visibleProperty());
                    }

                    if (feature == null) {
                        featureInfoContainer.setVisible(false);
                        return;
                    }

                    featureInfoContainer.setVisible(true);

                    for (int lod = 1; lod <= 3; ++lod) {
                        var toggle = toggles.get(lod - 1);
                        if (feature.getLODView(lod) == null) {
                            toggle.setVisible(false);
                            continue;
                        }

                        toggle.setVisible(true);
                        if (oldFeature != null && oldFeature.getLODView(lod) != null)
                            toggle.selectedProperty().unbindBidirectional(((Node) oldFeature.getLODView(lod)).visibleProperty());
                        toggle.selectedProperty().bindBidirectional(((Node) feature.getLODView(lod)).visibleProperty());
                    }
                });


        Editor.getFeatureSellection().activeCityObjectProperty()
                .addListener((observable, oldFeature, feature) -> {
                    if (feature == null)
                        return;

                    featureIDText.setText("地物ID：" + feature.getId());
                    if (feature instanceof AbstractBuilding) {
                        featureTypeText.setText("地物型：" + UIConstants.buildingTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof Road) {
                        featureTypeText.setText("地物型：" + UIConstants.roadTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof LandUse) {
                        featureTypeText.setText("地物型：" + UIConstants.landUseTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof WaterBody) {
                        featureTypeText.setText("地物型：" + UIConstants.waterBodyTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof SolitaryVegetationObject) {
                        featureTypeText.setText("地物型：" + UIConstants.solitaryVegetationObjectTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof PlantCover) {
                        featureTypeText.setText("地物型：" + UIConstants.plantCoverTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof CityFurniture) {
                        featureTypeText.setText("地物型：" + UIConstants.cityFurnitureTypeDescription(feature.getCityGMLClass()));
                    } else if (feature instanceof ADEGenericComponent) {
                        featureTypeText.setText("地物型：" + UIConstants.genericComponentTypeDescription(((ADEGenericComponent) feature).getNodeName()));
                    }
                });
    }
}