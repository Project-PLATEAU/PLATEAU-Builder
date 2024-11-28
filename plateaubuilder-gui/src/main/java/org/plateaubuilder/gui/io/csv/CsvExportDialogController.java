package org.plateaubuilder.gui.io.csv;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.world.World;
import org.plateaubuilder.gui.utils.AlertController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class CsvExportDialogController implements Initializable {
    private Stage root;
    
    private boolean dialogResult;
    private void setRoot(Stage stage) {
        root = stage;
    }
    private String fileUrl;
    
    @FXML
    private TextField textFieldFile;
    
    private String fileName;
    
    private String folderName;
    
    private boolean exportAllFlg;
    
    public final static String DEFAULT_FILE = "_attribute_export.csv";
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }
    
    /**
     * Set default values for folderName and fileName.
     *
     */
    private void setup(){
        if (folderName == null) {
            folderName = getDefaultPathExport();
            File folder = new File(folderName);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    folderName = Editor.getDatasetPath();
                }
            }
        }
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = currentDate.format(formatter);
        fileName = formattedDate + DEFAULT_FILE;
        textFieldFile.setText(folderName + "\\" + fileName);
    }
    
    /**
     * Set the default value of folderName based on the features selected by the user.
     *
     * @return default csv path
     */
    private String getDefaultPathExport() {
        String gmlPath = Editor.getDatasetPath();
        if (exportAllFlg) {
            if (World.getActiveInstance().getCityModelGroup() != null) {
                List<Node> cityModelViews = World.getActiveInstance().getCityModelGroup().getChildren();
                for (Node cityModelView : cityModelViews) {
                    CityModelView city = (CityModelView) cityModelView;
                    gmlPath = city.getGmlPath();
                    break;
                }
            }
        } else {
            List<Node> cityModelViews = World.getActiveInstance().getCityModelGroup().getChildren();
            List<Pair<String, List<IFeatureView>>> listPairGmlPath = new LinkedList<>();
            for (Node cityModelView : cityModelViews) {
                String gmlPathTemp = ((CityModelView) cityModelView).getGmlPath();
                CityModelView city = (CityModelView) cityModelView;
                listPairGmlPath.add(new Pair<>(gmlPathTemp, new LinkedList<>(city.getFeatureViews())));
            }
            List<IFeatureView> featuresExport = new ArrayList<>(Editor.getFeatureSellection().getSelectedFeatures());
            if (!featuresExport.isEmpty()) {
                for (Pair<String, List<IFeatureView>> pairGmlPath : listPairGmlPath) {
                    List<IFeatureView> featuresImport = pairGmlPath.getSecond();
                    gmlPath = pairGmlPath.getFirst();
                    if(featuresImport.stream().anyMatch(e -> Objects.equals(e, featuresExport.get(0)))){
                        break;
                    }
                }
            }
        }
        var datasetPath = Paths.get(gmlPath);
        while (!datasetPath.getFileName().toString().equals("udx")) {
            datasetPath = datasetPath.getParent();
            if(datasetPath.getParent() == null){
                datasetPath = null;
                break;
            }
        }
        if (datasetPath != null) {
            return datasetPath.getParent().toString()+"\\csv";
        }
        return Editor.getDatasetPath()+"\\csv";
    }
    
    
    /**
     * エクスポートボタン選択時イベント
     * @param actionEvent
     */
    public void onSubmit(ActionEvent actionEvent) {
        this.fileUrl = textFieldFile.getText();
        if (StringUtils.isEmpty(this.fileUrl)) {
            return;
        }
        this.dialogResult = true;
        
        root.close();
    }
    
    /**
     * 出力先選択時イベント
     * @param actionEvent
     */
    public void onSelectFile(ActionEvent actionEvent) {
        var type = "csv";
        var extensions = new String[] {"*.csv"};
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(type, extensions)
        );
        chooser.setTitle(String.format("Export %s", type));
        chooser.setInitialFileName(fileName);
        File folder = new File(folderName);
        if (folder.exists()) {
            chooser.setInitialDirectory(new File(folderName));
        }
        File file = chooser.showSaveDialog(this.root.getOwner());
        if (file == null) {
            return;
        }
        fileName = file.getName();
        folderName = file.getParent();
        textFieldFile.setText(file.getAbsolutePath());
    }
    
    /**
     * ダイアログ結果を取得
     * @return
     */
    public boolean getDialogResult() {
        return dialogResult;
    }

    public static CsvExportDialogController create(boolean exportAllFlg) {
        try {
            String title = "選択されたモデルの属性情報をエクスポートする";
            if(exportAllFlg) {
                title = "すべてのモデルの属性情報をエクスポートする";
            }
            if (World.getActiveInstance().getCityModelGroup() == null) {
                AlertController.showExportAlert();
                return new CsvExportDialogController();
            }
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(CsvExportDialogController.class.getResource("csv-export-dialog.fxml")));
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);
            var controller = (CsvExportDialogController) loader.getController();
            controller.setRoot(stage);
            controller.setExportAllFlg(exportAllFlg);
            controller.setup();
            stage.showAndWait();
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    private void setExportAllFlg(boolean exportAllFlg){
        this.exportAllFlg = exportAllFlg;
    }
}
