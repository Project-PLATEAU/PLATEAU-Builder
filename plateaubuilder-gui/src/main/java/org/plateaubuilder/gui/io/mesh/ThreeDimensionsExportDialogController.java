package org.plateaubuilder.gui.io.mesh;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.geometry.ILODView;
import org.plateaubuilder.core.io.mesh.AxisEnum;
import org.plateaubuilder.core.io.mesh.ThreeDimensionsModelEnum;
import org.plateaubuilder.core.io.mesh.exporters.ExportOption;
import org.plateaubuilder.core.io.mesh.exporters.ExportOptionBuilder;
import org.plateaubuilder.core.utils3d.geom.Vec3d;
import org.plateaubuilder.core.world.World;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ThreeDimensionsExportDialogController implements Initializable {
    private Stage root;

    private IFeatureView featureView;

    private String fileUrl;

    private ILODView lodView;

    private ExportOption exportOption;
    
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
    private Label labelGeoReference;

    @FXML
    private TextField textFieldEast;

    @FXML
    private TextField textFieldNorth;

    @FXML
    private TextField textFieldTop;

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
     * 3Dエクスポート対象のIFeatureViewを設定
     * 
     * @param featureView
     */
    public void setFeatureView(IFeatureView featureView) {
        this.featureView = featureView;

        var list = new ArrayList<String>();
        if (this.featureView.getLODView(1) != null) {
            list.add("LOD1");
        }
        if (this.featureView.getLODView(2) != null) {
            list.add("LOD2");
        }
        if (this.featureView.getLODView(3) != null) {
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
     * 3Dエクスポート対象のILODViewを取得
     * 
     * @return
     */
    public ILODView getLodView() {
        return lodView;
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
                this.lodView = this.featureView.getLODView(1);
                break;
            case "LOD2":
                this.lodView = this.featureView.getLODView(2);
                break;
            case "LOD3":
                this.lodView = this.featureView.getLODView(3);
                break;
            default:
                return;
        }
        this.fileUrl = textFieldFile.getText();
        if (StringUtils.isEmpty(this.fileUrl)) {
            return;
        }

        var origin = World.getActiveInstance().getGeoReference().getOrigin();
        this.exportOption = new ExportOptionBuilder()
                .axisEast(comboBoxAxisEast.getValue())
                .axisTop(comboBoxAxisUp.getValue())
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
     * @param featureView
     */
    public static ThreeDimensionsExportDialogController create(IFeatureView featureView, ThreeDimensionsModelEnum modelType) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(ThreeDimensionsExportDialogController.class.getResource("3d-export-dialog.fxml"));
            stage.setScene(new Scene(loader.load()));
            var controller = (ThreeDimensionsExportDialogController) loader.getController();
            controller.setRoot(stage);
            controller.setFeatureView(featureView);
            controller.setModelType(modelType);
            stage.showAndWait();

            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
