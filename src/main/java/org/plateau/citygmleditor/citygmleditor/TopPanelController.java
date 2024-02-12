package org.plateau.citygmleditor.citygmleditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.ade.ADEException;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.exporters.GmlExporter;
import org.plateau.citygmleditor.exporters.TextureExporter;
import org.plateau.citygmleditor.importers.gml.GmlImporter;
import org.plateau.citygmleditor.exporters.CityGmlDatasetExporter;
import org.plateau.citygmleditor.world.World;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import javafx.scene.layout.VBox;

public class TopPanelController {
    public void importGml(ActionEvent actionEvent) {
        var files = FileChooserService.showMultipleOpenDialog("*.gml", SessionManager.GML_FILE_PATH_PROPERTY);

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

    public void openValidationWindow(ActionEvent event) throws IOException {
        Stage newWindow = new Stage();
        newWindow.setTitle("品質検査");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/validation/validation.fxml"));
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
