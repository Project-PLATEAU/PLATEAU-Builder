package org.plateau.citygmleditor.citygmleditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.plateau.citygmleditor.exporters.CityGmlDatasetExporter;
import org.plateau.citygmleditor.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TopPanelController {
    public void importGml(ActionEvent actionEvent) {
        var file = FileChooserService.showOpenDialog("*.gml", SessionManager.GML_FILE_PATH_PROPERTY);

        if (file == null)
            return;

        CoordinateDialogController.createCoorinateDialog(file);
    }

    public void exportDataset(ActionEvent event) {
        var cityModel = World.getActiveInstance().getCityModel();
        var datasetExporter = new CityGmlDatasetExporter();
        datasetExporter.export(cityModel);
    }

    public void openValidationWindow(ActionEvent event) throws IOException {
        Stage newWindow = new Stage();
        newWindow.setTitle("品質検査");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/validation.fxml"));
        newWindow.setScene(new Scene(loader.load()));
        newWindow.showAndWait();
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
