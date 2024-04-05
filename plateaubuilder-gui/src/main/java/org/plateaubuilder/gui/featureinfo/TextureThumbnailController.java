package org.plateaubuilder.gui.featureinfo;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import org.plateaubuilder.core.editor.Editor;

import java.awt.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class TextureThumbnailController implements Initializable {
    @FXML
    private TitledPane titledPane;

    @FXML
    private TilePane tilePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var activeFeatureProperty = Editor.getFeatureSellection().getActiveFeatureProperty();
        activeFeatureProperty.addListener((observable, oldValue, newValue) -> {
            clearImage();

            if (newValue == null)
                return;

            for (var image : newValue.getTexturePaths()) {
                var imageUri = Paths.get(image).toUri().toString();
                addImage(imageUri);
            }
        });
    }

    private void addImage(String imageUrl) {
        ImageView imageView = new ImageView(imageUrl);
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        imageView.setOnMouseClicked(event -> {
            openExplorer(imageUrl);
        });
        tilePane.getChildren().add(imageView);
    }

    private void clearImage() {
        tilePane.getChildren().clear();
    }

    private void openExplorer(String path) {
        try {
            if (Desktop.isDesktopSupported()) {
                // WindowsではDesktopAPIでは開けない？
                // Desktop.getDesktop().open(new java.io.File(path));
                Desktop.getDesktop().browseFileDirectory(new java.io.File(path));
            }
        } catch (UnsupportedOperationException uoe) {
            try {
                // Windows Only
                String explorerCommand = "explorer.exe /select," + path;
                Runtime.getRuntime().exec(explorerCommand);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
