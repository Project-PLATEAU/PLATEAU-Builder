package org.plateaubuilder.gui.main;

import javafx.event.ActionEvent;
import org.plateaubuilder.core.io.gml.CityGmlDatasetExporter;
import org.plateaubuilder.gui.FileChooserService;
import org.plateaubuilder.core.editor.SessionManager;
import org.plateaubuilder.gui.io.gml.CoordinateDialogController;
import org.plateaubuilder.gui.io.gml.LoadGMLDialogController;
import org.plateaubuilder.gui.validation.ValidationController;
import org.plateaubuilder.core.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TopPanelController {
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
}
