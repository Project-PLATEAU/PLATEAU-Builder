package org.plateaubuilder.gui.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.plateaubuilder.core.editor.SessionManager;
import org.plateaubuilder.core.io.csv.exporters.CSVExporter;
import org.plateaubuilder.core.io.gml.CityGmlDatasetExporter;
import org.plateaubuilder.core.world.World;
import org.plateaubuilder.gui.FileChooserService;
import org.plateaubuilder.gui.io.csv.CsvExportDialogController;
import org.plateaubuilder.gui.io.csv.CsvImportDialogController;
import org.plateaubuilder.gui.io.gml.CoordinateDialogController;
import org.plateaubuilder.gui.io.gml.LoadGMLDialogController;
import org.plateaubuilder.gui.validation.ValidationController;

import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

public class TopPanelController {
    ContextMenu fileMenu;
    ContextMenu csvMenu;

    // 「ファイル」メニューを表示するメソッド
    public void showFileMenu(MouseEvent event) {
        fileMenu = new ContextMenu();
        MenuItem importItem = new MenuItem("インポート");
        importItem.setOnAction(e -> importGml(null)); // importGmlメソッドを呼び出し

        MenuItem exportItem = new MenuItem("エクスポート");
        exportItem.setOnAction(e -> exportDataset(null)); // exportDatasetメソッドを呼び出し

        fileMenu.getItems().addAll(importItem, exportItem);
        fileMenu.show((Node) event.getSource(), Side.BOTTOM, 0, 0);
    }

    // マウスがボタンから離れたときにファイルメニューを非表示にするメソッド
    public void hideFileMenu(MouseEvent event) {
        fileMenu.hide();
    }

    // 「ファイル」メニューを表示するメソッド
    public void showCsvMenu(MouseEvent event) {
        csvMenu = new ContextMenu();
        MenuItem importItem = new MenuItem("インポート");
        importItem.setOnAction(e -> importAttribute(null)); // importGmlメソッドを呼び出し

        MenuItem exportItem = new MenuItem("エクスポート");
        exportItem.setOnAction(e -> exportAttribute(null)); // exportDatasetメソッドを呼び出し

        csvMenu.getItems().addAll(importItem, exportItem);
        csvMenu.show((Node) event.getSource(), Side.BOTTOM, 0, 0);
    }

    // マウスがボタンから離れたときにファイルメニューを非表示にするメソッド
    public void hideCsvMenu(MouseEvent event) {
        csvMenu.hide();
    }

    public void importGml(ActionEvent actionEvent) {
        var files = FileChooserService.showMultipleOpenDialog(SessionManager.GML_FILE_PATH_PROPERTY, "*.gml");

        if (files == null)
            return;

        if (World.getActiveInstance().getGeoReference() == null || World.getActiveInstance()
                .getGeoReference().getEPSGCode() == null || World.getActiveInstance()
                        .getGeoReference().getEPSGCode().isEmpty()) {
            CoordinateDialogController.createCoorinateDialog(files);
        } else {
            LoadGMLDialogController.createLoadGMLDialog(files);
        }
    }

    public void exportDataset(ActionEvent event) {
        var cityModels = World.getActiveInstance().getCityModels();
        var datasetExporter = new CityGmlDatasetExporter();
        datasetExporter.export(cityModels);
    }

    public void openValidationWindow() {
        ValidationController.openWindow();
    }

    // フォルダコピーメソッド(udx以下は無視)
    public void folderCopy(Path sourcePath, Path destinationPath) throws IOException {
        String skipPattern = sourcePath.toString().replace("\\", "\\\\") + "\\\\udx\\\\.*";
        Files.walk(sourcePath).forEach(path -> {
            if (!path.toString().matches(skipPattern)) {
                try {
                    Files.copy(path, destinationPath.resolve(sourcePath.relativize(path)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 属性エクスポート
    public void exportAttribute(ActionEvent event) {
        try {
            CsvExportDialogController controller = CsvExportDialogController.create(true);
            if (!controller.getDialogResult())
                return;
            String fileUrl = controller.getFileUrl();
            CSVExporter exporter = new CSVExporter(true);
            exporter.export(fileUrl);
            java.awt.Desktop.getDesktop().open(new File(fileUrl).getParentFile());
        } catch (Exception ex) {
            Logger.getLogger(TopPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void importAttribute(ActionEvent event) {
        try {
            CsvImportDialogController.create();
        } catch (Exception ex) {
            Logger.getLogger(TopPanelController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
