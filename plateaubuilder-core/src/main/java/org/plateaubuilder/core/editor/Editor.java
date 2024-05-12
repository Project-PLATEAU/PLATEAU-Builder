package org.plateaubuilder.core.editor;

import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.plateaubuilder.core.citymodel.UroAttributeInfo;
import org.plateaubuilder.core.editor.CityModelViewMode;
import org.plateaubuilder.core.editor.FeatureSelection;
import org.plateaubuilder.core.editor.commands.UndoManager;
import org.plateaubuilder.core.editor.surfacetype.SurfaceTypeEditor;
import org.plateaubuilder.core.world.*;

import java.util.Objects;

public class Editor {
    private static Stage stage;
    private static Scene scene;
    private static Camera camera;
    private static CoordinateGrid coordinateGrid;
    private static Light light;
    private static SceneContent sceneContent;
    private static AntiAliasing antiAliasing;
    private static CityModelViewMode cityModelViewMode;
    private static SurfaceTypeEditor surfaceTypeEditor;
    private static UroAttributeInfo uroAttributeInfo;
    private static String datasetPath;

    private static SessionManager sessionManager;

    private static FeatureSelection selection;

    private static UndoManager undoManager;


    public static Camera getCamera() {
        return camera;
    }

    public static CoordinateGrid getCoordinateGrid() {
        return coordinateGrid;
    }

    public static Light getLight() {
        return light;
    }

    public static SceneContent getSceneContent() {
        return sceneContent;
    }

    public static Scene getScene() {
        return scene;
    }

    public static Window getWindow() {
        return scene.getWindow();
    }

    public static AntiAliasing getAntiAliasing() {
        return antiAliasing;
    }

    public static FeatureSelection getFeatureSellection() {
        return selection;
    }

    public static UndoManager getUndoManager() {
        return undoManager;
    }

    public static CityModelViewMode getCityModelViewMode() {
        return cityModelViewMode;
    }

    public static SurfaceTypeEditor getSurfaceTypeEditor() {
        return surfaceTypeEditor;
    }

    public static org.w3c.dom.Document getUroAttributeDocument() {
        return uroAttributeInfo.getUroAttributeDocument();
    }

    public static void settingUroAttributeInfo(String path) {
        uroAttributeInfo.readUroSchemas(path);
    }

    public static void setDatasetPath(String path) {
        datasetPath = path;
    }

    public static String getDatasetPath() {
        return datasetPath;
    }

    public static void initialize(Stage newStage) throws Exception {
        stage = newStage;
        sessionManager = SessionManager.createSessionManager("PLATEAUBuilder");
        sessionManager.loadSession();
        uroAttributeInfo = new UroAttributeInfo();

        World.setActiveInstance(new World(), new Group());
        light = new Light();
        antiAliasing = new AntiAliasing();
        camera = new Camera();
        sceneContent = new SceneContent();

        antiAliasing.setSceneContent();

        coordinateGrid = new CoordinateGrid();

        World.getActiveInstance().setCamera(camera);

        sceneContent.rebuildSubScene();

        cityModelViewMode = new CityModelViewMode();

        surfaceTypeEditor = new SurfaceTypeEditor();

        selection = new FeatureSelection();

        undoManager = new UndoManager(20);

    }

    public static void registerScene(Scene newScene) {
        scene = newScene;
        selection.initialize(sceneContent.getSubScene());
        surfaceTypeEditor.registerClickEvent(sceneContent.getSubScene());
        cityModelViewMode.isSurfaceViewModeProperty().addListener((observable, oldValue, newValue) -> {
            selection.enabledProperty().set(!newValue);
            surfaceTypeEditor.enabledProperty().set(newValue);
        });

        stage.setScene(scene);
        stage.show();

        // TODO: 別クラスに委譲
        stage.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.Z) {
                getUndoManager().undo();
                event.consume();
            }

            if (event.isControlDown() && event.getCode() == KeyCode.Y) {
                getUndoManager().redo();
                event.consume();
            }
        });

        // アプリ終了時にセッションを保存
        stage.setOnCloseRequest(event -> sessionManager.saveSession());
    }
}
