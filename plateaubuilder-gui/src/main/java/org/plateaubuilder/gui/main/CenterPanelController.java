package org.plateaubuilder.gui.main;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.ObjectUtils;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.io.csv.exporters.CSVExporter;
import org.plateaubuilder.gui.io.csv.CsvExportDialogController;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CenterPanelController implements Initializable {
    @FXML
    Pane subSceneContainer;

    @FXML
    private StackPane adjustPerspective;

    @FXML
    private ContextMenu hierarchyContextMenu;

    @FXML
    private MenuItem exportCsvMenu;

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        // SubScene設定
        var sceneContent = Editor.getSceneContent();
        var subScene = sceneContent.subSceneProperty();

        if (subScene.get() != null) {
            setSubScene(subScene.get());
        }

        subScene.addListener((o,old,newSubScene) -> {
            setSubScene(newSubScene);
        });

        // subSceneContainerの幅が変更されたときにadjustを右寄せにする
        subSceneContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            setRightAdjustPerspective(newValue.doubleValue());
        });

        // レイアウトが完了した後に幅を取得し、右寄せにする
        Platform.runLater(() -> {
            setRightAdjustPerspective(subSceneContainer.getWidth());
        });
        Editor.getSceneContent().getSubScene().addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
    }

    private void setSubScene(SubScene subScene) {
        // SubSceneの大きさをPaneに合わせる
        subScene.heightProperty().bind(subSceneContainer.heightProperty());
        subScene.widthProperty().bind(subSceneContainer.widthProperty());

        // 既にあれば上書き、無ければ追加
        if (!subSceneContainer.getChildren().isEmpty()
                && subSceneContainer.getChildren().get(0) instanceof SubScene) {
            subSceneContainer.getChildren().set(0, subScene);
        } else {
            subSceneContainer.getChildren().add(0, subScene);
        }
    }

    private void setRightAdjustPerspective(double containerWidth) {
        // StackPaneの幅を取得し、subSceneContainerの幅から引いてオフセットとする
        double adjustWidth = adjustPerspective.getWidth();
        double xOffset = containerWidth - adjustWidth;

        // StackPaneの位置をオフセットに設定
        adjustPerspective.layoutXProperty().unbind();
        adjustPerspective.setLayoutX(xOffset);
    }
    private final EventHandler<MouseEvent> mouseEventHandler = event -> {
        if (event.isSecondaryButtonDown() && !ObjectUtils.isEmpty(Editor.getFeatureSellection().getSelectedFeatures())) {
            hierarchyContextMenu.show(Editor.getSceneContent().getSubScene(),event.getScreenX(), event.getScreenY());
        }
    };

    public void exportCsv(ActionEvent actionEvent) {
        try {
            CsvExportDialogController controller = CsvExportDialogController.create(false);
            if (!controller.getDialogResult())
                return;
            String fileUrl = controller.getFileUrl();
            CSVExporter exporter = new CSVExporter(false);
            exporter.export(fileUrl);
            java.awt.Desktop.getDesktop().open(new File(fileUrl).getParentFile());
        } catch (Exception ex) {
            Logger.getLogger(TopPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
