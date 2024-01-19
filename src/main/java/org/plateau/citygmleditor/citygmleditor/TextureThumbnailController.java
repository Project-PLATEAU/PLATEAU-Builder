package org.plateau.citygmleditor.citygmleditor;

import java.awt.Desktop;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

public class TextureThumbnailController implements Initializable {
    @FXML
    private TitledPane titledPane;

    @FXML
    private TilePane tilePane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var activeFeatureProperty = CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty();
        activeFeatureProperty.addListener((observable, oldValue, newValue) -> {
            clearImage();
            for (var image : newValue.getTexturePaths()) {
                addImage(image);
            }
        });
    }

    private void addImage(String imageUrl) {
        ImageView imageView = new ImageView(new Image(imageUrl));
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
