package org.plateau.plateaubuilder.plateaubuilder.fxml.io.mesh;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.io.mesh.converters.ConvertOption;
import org.plateau.plateaubuilder.io.mesh.converters.ConvertOptionBuilder;
import org.plateau.plateaubuilder.io.mesh.AxisEnum;
import org.plateau.plateaubuilder.plateaubuilder.FileChooserService;
import org.plateau.plateaubuilder.plateaubuilder.SessionManager;
import org.plateau.plateaubuilder.io.mesh.ThreeDimensionsModelEnum;
import org.plateau.plateaubuilder.utils3d.geom.Vec3d;
import org.plateau.plateaubuilder.world.World;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ThreeDimensionsImportDialogController implements Initializable {
    private Stage root;

    private BuildingView buildingView;

    private String fileUrl;

    private int lod;

    private ConvertOption convertOption;

    private boolean dialogResult;

    private ThreeDimensionsModelEnum modelType;

    @FXML
    private TextField textFieldFile;

    @FXML
    private ComboBox<String> comboBoxLod;

    @FXML
    private ComboBox<AxisEnum> comboBoxAxisEast;

    @FXML
    private ComboBox<AxisEnum> comboBoxAxisUp;

    @FXML
    private TextField textFieldWallThreshold;

    @FXML
    private Label labelGeoReference;

    @FXML
    private TextField textFieldEast;

    @FXML
    private TextField textFieldNorth;

    @FXML
    private TextField textFieldTop;

    @FXML
    private Pane paneUseGeoReference;

    @FXML
    private Pane paneAuto;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Callback<ListView<AxisEnum>, ListCell<AxisEnum>> cellFactory
                = (ListView<AxisEnum> param) -> new ListCell<AxisEnum>() {
            @Override
            protected void updateItem(AxisEnum item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getDisplayName());
                }
            }
        };
        ObservableList<AxisEnum> list = FXCollections.observableArrayList(AxisEnum.values());
        comboBoxAxisEast.getItems().addAll(list);
        comboBoxAxisEast.setButtonCell(cellFactory.call(null));
        comboBoxAxisEast.setCellFactory(cellFactory);
        comboBoxAxisEast.setValue(AxisEnum.X);
        comboBoxAxisUp.getItems().addAll(list);
        comboBoxAxisUp.setButtonCell(cellFactory.call(null));
        comboBoxAxisUp.setCellFactory(cellFactory);
        comboBoxAxisUp.setValue(AxisEnum.Z);

        textFieldWallThreshold.setText(String.valueOf(ConvertOption.DEFAULT_WALL_THRESHOLD));
        var geoReference = World.getActiveInstance().getGeoReference();
        var origin = geoReference.getOrigin();
        labelGeoReference.setText(geoReference.getEPSGCode());

        textFieldEast.setText(String.valueOf(origin.x));
        textFieldNorth.setText(String.valueOf(origin.y));
        textFieldTop.setText(String.valueOf(origin.z));
    }

    /**
     * FXMLのStageを設定
     * @param stage
     */
    public void setRoot(Stage stage) {
        root = stage;
    }

    /**
     * 3Dエクスポート対象のBuildingViewを設定
     * @param buildingView
     */
    public void setBuildingView(BuildingView buildingView) {
        this.buildingView = buildingView;
        comboBoxLod.getItems().addAll(FXCollections.observableArrayList(Arrays.asList("LOD1", "LOD2", "LOD3")));
    }

    /*
     * ThreeDimensionsModelEnumを設定
     */
    public void setModelType(ThreeDimensionsModelEnum modelType) {
        this.modelType = modelType;
    }

    /*
     * 出力先を取得
     */
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * 3Dエクスポート対象のLODを取得
     * @return
     */
    public int getLod() {
        return lod;
    }

    /**
     * ConvertOptionを取得
     * @return
     */
    public ConvertOption getConvertOption() {
        return convertOption;
    }

    /**
     * ダイアログ結果を取得
     * @return
     */
    public boolean getDialogResult() {
        return dialogResult;
    }

    /**
     * 出力先選択時イベント
     * @param actionEvent
     */
    public void onSelectFile(ActionEvent actionEvent) {
        var extensions = modelType == ThreeDimensionsModelEnum.OBJ ? new String[] {"*.obj"} : new String[] {"*.gltf", "*.glb"};
        var file = FileChooserService.showOpenDialog(SessionManager.GLTF_FILE_PATH_PROPERTY, extensions);
        if (file == null) {
            return;
        }

        textFieldFile.setText(file.getAbsolutePath());
    }

    /**
     * エクスポートボタン選択時イベント
     * @param actionEvent
     */
    public void onSubmit(ActionEvent actionEvent) {
        switch (comboBoxLod.getValue()) {
            case "LOD1":
                lod = 1;
                break;
            case "LOD2":
                lod = 2;
                break;
            case "LOD3":
                lod = 3;
                break;
            default:
                return;
        }
        this.fileUrl = textFieldFile.getText();
        if (StringUtils.isEmpty(this.fileUrl)) {
            return;
        }

        var optionBuilder = new ConvertOptionBuilder().wallThreshold(Double.parseDouble(textFieldWallThreshold.getText()));
        var useGeoReference = paneUseGeoReference.isVisible();
        optionBuilder
                .axisEast(comboBoxAxisEast.getValue())
                .axisUp(comboBoxAxisUp.getValue())
                .useGeoReference(useGeoReference);
        if (useGeoReference) {
            var origin = World.getActiveInstance().getGeoReference().getOrigin();
            optionBuilder
                .offset(new Vec3d(
                    origin.x - Double.parseDouble(textFieldEast.getText()),
                    origin.y - Double.parseDouble(textFieldNorth.getText()),
                    origin.z - Double.parseDouble(textFieldTop.getText())));
        }

        this.convertOption = optionBuilder.build();
        this.dialogResult = true;

        root.close();
    }

    public void onUseGeoReference(ActionEvent actionEvent) {
        paneUseGeoReference.visibleProperty().set(true);
        paneAuto.visibleProperty().set(false);
    }

    public void onAuto(ActionEvent actionEvent) {
        paneUseGeoReference.visibleProperty().set(false);
        paneAuto.visibleProperty().set(true);
    }

    /**
     * 3Dエクスポートダイアログ表示
     * 
     * @param buildingView
     */
    public static ThreeDimensionsImportDialogController create(BuildingView buildingView, ThreeDimensionsModelEnum modelType) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(ThreeDimensionsImportDialogController.class.getResource("3d-import-dialog.fxml"));
            stage.setScene(new Scene(loader.load()));
            var controller = (ThreeDimensionsImportDialogController) loader.getController();
            controller.setRoot(stage);
            controller.setBuildingView(buildingView);
            controller.setModelType(modelType);
            stage.showAndWait();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
