package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.geometry.ILODSolidView;
import org.plateau.citygmleditor.exporters.ExportOption;
import org.plateau.citygmleditor.exporters.ExportOptionBuilder;
import org.plateau.citygmleditor.utils3d.geom.Vec3d;
import org.plateau.citygmleditor.world.World;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ThreeDimensionsExportDialogController implements Initializable {
    private Stage root;

    private BuildingView buildingView;

    private String fileUrl;

    private ILODSolidView lodSolidView;

    private ExportOption exportOption;
    
    private boolean dialogResult;

    private ThreeDimensionsModelEnum modelType;

    @FXML
    private TextField textFieldFile;

    @FXML
    private ComboBox<String> comboBoxLod;

    @FXML
    private Label labelGeoReference;

    @FXML
    private TextField textFieldEast;

    @FXML
    private TextField textFieldNorth;

    @FXML
    private TextField textFieldTop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        var list = new ArrayList<String>();
        if (this.buildingView.getLOD1Solid() != null) {
            list.add("LOD1");
        }
        if (this.buildingView.getLOD2Solid() != null) {
            list.add("LOD2");
        }
        if (this.buildingView.getLOD3Solid() != null) {
            list.add("LOD3");
        }
        comboBoxLod.getItems().addAll(FXCollections.observableArrayList(list));
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
     * 3Dエクスポート対象のLodSolidViewを取得
     * @return
     */
    public ILODSolidView getLodSolidView() {
        return lodSolidView;
    }

    /**
     * ExportOptionを取得
     * @return
     */
    public ExportOption getExportOption() {
        return exportOption;
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
        var type = modelType == ThreeDimensionsModelEnum.OBJ ? "OBJ" : "gLTF";
        var extensions = modelType == ThreeDimensionsModelEnum.OBJ ? new String[] {"*.obj"} : new String[] {"*.gltf", "*.glb"};
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(type, extensions)
        );
        chooser.setTitle(String.format("Export %s", type));
        File file = chooser.showSaveDialog(this.root.getOwner());
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
                this.lodSolidView = buildingView.getLOD1Solid();
                break;
            case "LOD2":
                this.lodSolidView = buildingView.getLOD2Solid();
                break;
            case "LOD3":
                this.lodSolidView = buildingView.getLOD3Solid();
                break;
            default:
                break;
        }
        this.fileUrl = textFieldFile.getText();

        var origin = World.getActiveInstance().getGeoReference().getOrigin();
        this.exportOption = new ExportOptionBuilder()
                .offset(new Vec3d(
                        origin.x - Double.parseDouble(textFieldEast.getText()),
                        origin.y - Double.parseDouble(textFieldNorth.getText()),
                        origin.z - Double.parseDouble(textFieldTop.getText())))
                .build();
        this.dialogResult = true;

        root.close();
    }

    /**
     * 3Dエクスポートダイアログ表示
     * 
     * @param buildingView
     */
    public static ThreeDimensionsExportDialogController create(BuildingView buildingView, ThreeDimensionsModelEnum modelType) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(ThreeDimensionsExportDialogController.class.getResource("fxml/3d-export-dialog.fxml"));
            stage.setScene(new Scene(loader.load()));
            var controller = (ThreeDimensionsExportDialogController) loader.getController();
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