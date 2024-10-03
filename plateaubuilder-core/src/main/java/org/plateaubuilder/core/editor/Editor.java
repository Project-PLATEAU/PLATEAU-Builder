package org.plateaubuilder.core.editor;

import org.plateaubuilder.core.citymodel.attribute.reader.XSDSchemaDocument;
import org.plateaubuilder.core.editor.commands.UndoManager;
import org.plateaubuilder.core.editor.surfacetype.SurfaceTypeEditor;
import org.plateaubuilder.core.world.AntiAliasing;
import org.plateaubuilder.core.world.Camera;
import org.plateaubuilder.core.world.CoordinateGrid;
import org.plateaubuilder.core.world.Light;
import org.plateaubuilder.core.world.SceneContent;
import org.plateaubuilder.core.world.World;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.plateaubuilder.core.basemap.XyzTile;

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
    private static XSDSchemaDocument uroSchemaDocument;
    private static String datasetPath;

    private static SessionManager sessionManager;

    private static FeatureSelection selection;

    private static UndoManager undoManager;

    private static XyzTile xyzTile;

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

    public static void settingUroSchemaDocument(String path) {
        uroSchemaDocument.initialize(path, "uro");
    }

    public static XSDSchemaDocument getUroSchemaDocument() {
        return uroSchemaDocument;
    }

    public static void setDatasetPath(String path) {
        datasetPath = path;
    }

    public static String getDatasetPath() {
        return datasetPath;
    }

    public static XyzTile getXyzTile() {
        return xyzTile;
    }

    public static void initialize(Stage newStage) throws Exception {
        stage = newStage;
        sessionManager = SessionManager.createSessionManager("PLATEAUBuilder");
        sessionManager.loadSession();
        uroSchemaDocument = new XSDSchemaDocument();

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

        xyzTile = new XyzTile();

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
