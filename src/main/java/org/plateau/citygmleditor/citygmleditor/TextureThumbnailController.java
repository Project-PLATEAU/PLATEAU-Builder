package org.plateau.citygmleditor.citygmleditor;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence.Double;
import org.plateau.citygmleditor.citymodel.AttributeItem;
import org.plateau.citygmleditor.citymodel.BuildingView;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

public class TextureThumbnailController implements Initializable {
    
    @FXML
    private TitledPane titledPane;

    @FXML
    private TilePane tilePane;

    private ObjectProperty<BuildingView> active = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        var activeFeatureProperty = CityGMLEditorApp.getFeatureSellection().getActiveFeatureProperty();
        activeFeatureProperty.addListener((observable, oldValue, newValue) -> {
            // activeFeaturePropertyが変更されたときの処理をここに記述
            System.out.println("activeFeaturePropertyが変更されました");
            System.out.println("新しい値: " + newValue);
            System.out.println("古い値: " + oldValue);
            clearImage();
            for (var image : newValue.getTextures()) {
                addImage(image);
            }
        });
        // addImage("images/icon_info.png");
        // addImage("images/icon_info.png");
        // addImage("images/icon_info.png");
        // addImage("images/icon_info.png");
    }

    private void addImage(String imageUrl) {
        // ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imageUrl)));
        ImageView imageView = new ImageView(new Image(imageUrl));
        imageView.setFitWidth(60);
        imageView.setFitHeight(60);
        imageView.setOnMouseClicked(event -> {
            openExplorer(imageUrl);
            // String imagePath = getImagePath(imageUrl);
            // if (imagePath != null) {
            //     openExplorer(imagePath);
            // }
        });
        tilePane.getChildren().add(imageView);
    }

    private void clearImage() {
        tilePane.getChildren().clear();
    }

    private String getImagePath(String imageUrl) {
        URL resource = getClass().getResource(imageUrl);
        if (resource != null) {
            try {
                URI uri = resource.toURI();
                java.nio.file.Path path = java.nio.file.Paths.get(uri);
                // return path.toString();
                return path.getParent().toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void openExplorer(String path) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new java.io.File(path));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
