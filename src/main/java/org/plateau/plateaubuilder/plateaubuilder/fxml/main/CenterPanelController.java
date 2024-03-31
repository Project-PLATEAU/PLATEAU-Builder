package org.plateau.plateaubuilder.plateaubuilder.fxml.main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.layout.Pane;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;

import java.net.URL;
import java.util.ResourceBundle;

public class CenterPanelController implements Initializable {
    @FXML
    Pane subSceneContainer;

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        // SubScene設定
        var sceneContent = PLATEAUBuilderApp.getSceneContent();
        var subScene = sceneContent.subSceneProperty();

        if (subScene.get() != null) {
            setSubScene(subScene.get());
        }

        subScene.addListener((o,old,newSubScene) -> {
            setSubScene(newSubScene);
        });
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
}
