package org.plateau.citygmleditor.citygmleditor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.plateau.citygmleditor.citygmleditor.CoordinateDialogController.CoordinateCodesEnum;
import org.plateau.citygmleditor.importers.gml.GmlImporter;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoadGMLDialogController {
    private Stage root;

    private List<File> gmlFiles;
    
    /**
     * FXMLのStageを設定
     * 
     * @param stage
     */
    public void setRoot(Stage stage) {
        root = stage;
    }

    private void setFile(List<File> files) {
        gmlFiles = files;
    }

    /**
     * 新規ボタン選択時イベント
     * @param actionEvent
     */
    public void onNewGML(ActionEvent actionEvent) {
        CoordinateDialogController.createCoorinateDialog(gmlFiles);
        root.close();
    }

    /**
     * 追加ボタン選択時イベント
     * 
     * @param actionEvent
     */
    public void onAddGML(ActionEvent actionEvent) {
        try {
            for (var gmlFile : gmlFiles) {
                GmlImporter.loadGmlAdd(gmlFile.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        root.close();
    }

    /**
     * 座標系選択ダイアログ表示
     * 
     * @param file
     */
    public static void createLoadGMLDialog(List<File> files) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(CoordinateDialogController.class.getResource("fxml/loadgml-dialog.fxml"));
            stage.setScene(new Scene(loader.load()));
            var controller = (LoadGMLDialogController) loader.getController();
            controller.setFile(files);
            controller.setRoot(stage);
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
