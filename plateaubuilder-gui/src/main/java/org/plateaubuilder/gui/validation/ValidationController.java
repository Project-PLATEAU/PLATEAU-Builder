package org.plateaubuilder.gui.validation;

import static org.plateaubuilder.validation.AppConst.DATE_TIME_FORMAT;
import static org.plateaubuilder.validation.constant.StandardID.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.citygml4j.builder.copy.DeepCopyBuilder;
import org.citygml4j.model.gml.geometry.primitives.Polygon;
import org.plateaubuilder.core.citymodel.CityModelView;
import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.citymodel.factory.CustomGeometryFactory;
import org.plateaubuilder.core.citymodel.geometry.PolygonView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.world.World;
import org.plateaubuilder.gui.FileChooserService;
import org.plateaubuilder.validation.AppConst;
import org.plateaubuilder.validation.C04CompletenessValidator;
import org.plateaubuilder.validation.GMLIDCompletenessValidator;
import org.plateaubuilder.validation.GmlElementError;
import org.plateaubuilder.validation.IValidator;
import org.plateaubuilder.validation.L04LogicalConsistencyValidator;
import org.plateaubuilder.validation.L05LogicalConsistencyValidator;
import org.plateaubuilder.validation.L06LogicalConsistencyValidator;
import org.plateaubuilder.validation.L07LogicalConsistencyValidator;
import org.plateaubuilder.validation.L08LogicalConsistencyValidator;
import org.plateaubuilder.validation.L09LogicalConsistencyValidator;
import org.plateaubuilder.validation.L10LogicalConsistencyValidator;
import org.plateaubuilder.validation.L11LogicalConsistencyValidator;
import org.plateaubuilder.validation.L12LogicalConsistencyValidator;
import org.plateaubuilder.validation.L13LogicalConsistencyValidator;
import org.plateaubuilder.validation.Lbldg01LogicalAccuracyValidator;
import org.plateaubuilder.validation.Lbldg02LogicalConsistencyValidator;
import org.plateaubuilder.validation.Standard;
import org.plateaubuilder.validation.T03ThematicAccuaracyValidator;
import org.plateaubuilder.validation.Tbldg02ThematicAccuaracyValidator;
import org.plateaubuilder.validation.ValidationResultMessage;
import org.plateaubuilder.validation.ValidationResultMessageType;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;

public class ValidationController implements Initializable {
    @FXML
    VBox rootContainer;
    @FXML
    TextField parameterFilePathText;
    @FXML
    TextField logDestinationPathText;
    @FXML
    VBox resultTextContainer;
    @FXML
    TextArea messageTextArea;

    private final ToggleGroup toggleGroup = new ToggleGroup();

    private static final StringProperty parameterFilePath = new SimpleStringProperty();
    private static final StringProperty logDestinationDirectoryPath = new SimpleStringProperty();
    private static Logger LOGGER = Logger.getLogger(ValidationController.class.getName());

    private List<ValidationResultMessage> validationResultMessages;
    private final Group highLightGroup = new Group();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        World.getRoot3D().getChildren().add(highLightGroup);

        // パラメータファイル選択UI
        var defaultParameterFilePath = getDefaultParameterFilePath();
        if (!new File(defaultParameterFilePath).exists())
            writeTemplateParameterResource(defaultParameterFilePath);
        parameterFilePathText.setText(getDefaultParameterFilePath());
        parameterFilePath.bind(parameterFilePathText.textProperty());

        // ログ出力先選択UI
        logDestinationPathText.setText(getDefaultLogDestinationPath());
        logDestinationDirectoryPath.bind(logDestinationPathText.textProperty());

        messageTextArea.prefWidthProperty().bind(rootContainer.widthProperty());

        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            var message = (ValidationResultMessage)newValue.getUserData();
            messageTextArea.setText(message.getMessage());
            switch (message.getType()) {
                case Info:
                    break;
                case Warning:
                    messageTextArea.setStyle("-fx-text-fill: #be762d;");
                    break;
                case Error:
                    messageTextArea.setStyle("-fx-text-fill: red;");
                    break;
            }

            if (message.getElementErrors().isEmpty())
                return;

            activeBuildingElement(message.getElementErrors().get(0));
        });
    }

    public static void openWindow() {
        Stage newWindow = new Stage();
        newWindow.setTitle("品質検査");
        FXMLLoader loader = new FXMLLoader(ValidationController.class.getResource("validation.fxml"));
        try {
            newWindow.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        var controller = (ValidationController)loader.getController();
        newWindow.setOnCloseRequest(event -> {
            controller.clearHighLight();
        });

        newWindow.showAndWait();
    }

    private void showMessage(ValidationResultMessage message) {
        var item = ValidationLogListItemController.createUI(toggleGroup, message);
        item.setDisable(message.getType() == ValidationResultMessageType.Info);
        item.prefWidthProperty().bind(rootContainer.widthProperty());
        resultTextContainer.getChildren().add(item);

        if (!isEmpty(message.getElementErrors())) {
            highlightErrors(message.getElementErrors());
        }
    }

    private void showMessageInMainThread(ValidationResultMessage message) {
        Platform.runLater(() -> showMessage(message));
    }

    private Task<Void> createValidationTask(List<CityModelView> cityModelViews) {
        return new Task<>() {
          @Override
          protected Void call() throws Exception {
            validationResultMessages = new ArrayList<>();

            for (var cityModelView : cityModelViews) {
                var cityModel = cityModelView.getGML();
                if (cityModel == null) {
                    return null;
                }

                List<IValidator> validators = loadValidators(parameterFilePath.get());
                int nextValidationIndex = 0;
                int totalValidationCount = validators.size();
                for (var validator : validators) {
                    LOGGER.info("------ Start validate: " + validator.getClass().getSimpleName());

                    var progressMessage = new ValidationResultMessage(
                            ValidationResultMessageType.Info,
                            "品質項目の検査中...(" + (++nextValidationIndex) + "/" + totalValidationCount + ")");
                    showMessageInMainThread(progressMessage);

                    var messages = validator.validate(cityModelView);
                    for (var message : messages) {
                        showMessageInMainThread(message);
                    }

                    validationResultMessages.addAll(messages);

                    LOGGER.info("------ Finished validate: " + validator.getClass().getSimpleName());
                }
            }

            return null;
          }
        };
    }

    private EventHandler<WorkerStateEvent> validateFinishedHandler() {
        return event -> {
            var errorCount = 0;
            var warningCount = 0;
            for (var message : validationResultMessages) {
                switch (message.getType()) {
                    case Error:
                        errorCount++;
                        break;
                    case Warning:
                        warningCount++;
                        break;
                }
            }

            String countResultMessage = String.format("品質検査が完了しました。（エラー数:%d, 警告数:%d）", errorCount, warningCount);
            showMessage(new ValidationResultMessage(
                ValidationResultMessageType.Info, countResultMessage
            ));
            try {
                writeValidationLogToFile(logDestinationDirectoryPath.get(), validationResultMessages);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public void execute() {
        var cityModelViews = World.getActiveInstance().getCityModels();
        if (cityModelViews == null || cityModelViews.isEmpty()) {
            showMessage(new ValidationResultMessage(
                    ValidationResultMessageType.Error,
                    "CityGMLがインポートされていません"
            ));
            return;
        }

        Task<Void> validationTask = createValidationTask(cityModelViews);
        validationTask.setOnSucceeded(validateFinishedHandler());

        Thread validationThread = new Thread(validationTask);
        validationThread.start();
    }

    public void onClickParameterFileSelectButton() {
        var defaultParameterFileFolder = Paths.get(getDefaultParameterFilePath()).getParent().toString();
        var file = FileChooserService.showOpenDialogWithoutSession(defaultParameterFileFolder, "*.json");

        if (file == null)
            return;

        parameterFilePathText.setText(file.getAbsolutePath());
    }

    public void onClickLogDestinationSelectButton() {
        var file = FileChooserService.showDirectoryDialog(getDefaultLogDestinationPath());

        if (file == null)
            return;

        logDestinationPathText.setText(file.getAbsolutePath());
    }

    private List<IValidator> loadValidators(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Standard> standards = mapper.readValue(new File(path), new TypeReference<>() {});
        List<IValidator> result = new ArrayList<>();

        for (Standard standard : standards) {
            if (!standard.isEnabled()) continue;
            switch (standard.getId()) {
                case C01:
                    result.add(new GMLIDCompletenessValidator());
                    break;
                case C04:
                    result.add(new C04CompletenessValidator());
                    break;
                case L04:
                    result.add(new L04LogicalConsistencyValidator());
                    break;
                case L05:
                    result.add(new L05LogicalConsistencyValidator());
                    break;
                case L06:
                    result.add(new L06LogicalConsistencyValidator());
                    break;
                case L07:
                    result.add(new L07LogicalConsistencyValidator());
                    break;
                case L08:
                    result.add(new L08LogicalConsistencyValidator());
                    break;
                case L09:
                    result.add(new L09LogicalConsistencyValidator());
                    break;
                case L10:
                    result.add(new L10LogicalConsistencyValidator());
                    break;
                case L11:
                    result.add(new L11LogicalConsistencyValidator());
                    break;
                case L12:
                    result.add(new L12LogicalConsistencyValidator());
                    break;
                case L13:
                    result.add(new L13LogicalConsistencyValidator());
                    break;
                case L18:
                    //result.add(new L18LogicalConsistencyValidator());
                    break;
                case T03:
                    result.add(new T03ThematicAccuaracyValidator());
                    break;
                case L_BLDG_01:
                    result.add(new Lbldg01LogicalAccuracyValidator());
                    break;
                case L_BLDG_02:
                    result.add(new Lbldg02LogicalConsistencyValidator());
                    break;
                case L_BLDG_03:
                    //result.add(new Lbldg03LogicalAccuaracyValidator());
                    break;
                case T_BLDG_02:
                    result.add(new Tbldg02ThematicAccuaracyValidator());
                    break;
                case L14:
                    //result.add(new L14LogicalAccuaracyValidator());
                    break;
            }
        }
        return result;
    }

    private void highlightErrors(List<GmlElementError> elementErrors) {
        elementErrors.forEach(this::highlightError);
    }

    public void clearHighLight() {
        highLightGroup.getChildren().clear();
    }

    private void highlightError(GmlElementError elementError) {
        var material = new PhongMaterial();
        WritableImage image = new WritableImage(1, 1);
        PixelWriter writer = image.getPixelWriter();
        writer.setColor(0, 0, Color.RED);
        material.setSelfIlluminationMap(image);

        for (var cityModelView : World.getActiveInstance().getCityModels()) {
            var geometryFactory = new CustomGeometryFactory(cityModelView);
            cityModelView
                    .lookupAll("#" + elementError.getBuildingId())
                    .forEach(node -> {
                        List<PolygonView> polygonViews = new ArrayList<>();
                        if (((IFeatureView) node).getLODView(1) != null) {
                            polygonViews.addAll(((IFeatureView) node).getLODView(1).getPolygons());
                        }
                        if (((IFeatureView) node).getLODView(2) != null) {
                            polygonViews.addAll(((IFeatureView) node).getLODView(2).getPolygons());
                        }

                        polygonViews
                                .stream().filter(p -> Objects.equals(elementError.getPolygonId(), p.getGMLID()))
                                .forEach(polygon -> {
                                    var copiedPolygon = (Polygon) polygon.getGML().copy(new DeepCopyBuilder());
                                    var mesh = geometryFactory.createTriangleMesh(List.of(geometryFactory.createPolygon(copiedPolygon)));
                                    var meshView = new MeshView(mesh);
                                    meshView.setMesh(mesh);
                                    meshView.setMaterial(material);
                                    meshView.setDrawMode(DrawMode.LINE);
                                    meshView.setCullFace(CullFace.NONE);
                                    meshView.setDepthTest(DepthTest.DISABLE);
                                    highLightGroup.getChildren().add(meshView);
                                });
                    });
        }
    }

    private void activeBuildingElement(GmlElementError elementError) {
        for (var cityModelView : World.getActiveInstance().getCityModels()) {
            var nodes = cityModelView.lookupAll("#" + elementError.getBuildingId());
            if (nodes.isEmpty()) {
                continue;
            }
            var node = nodes.iterator().next();
            var featureView = (IFeatureView) node;
            Editor.getFeatureSellection().select(featureView);
            return;
        }
    }

    private static void writeTemplateParameterResource(String path) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            try {
                var fileName = AppConst.VALIDATION_PARAM_FILE_NAME;
                inputStream = Editor.class.getResourceAsStream(fileName);
                if (inputStream == null)
                    throw new Exception("Cannot get resource \"" + fileName + "\" from Jar file.");

                int readBytes;
                byte[] buffer = new byte[4096];
                outputStream = new FileOutputStream(path);
                while ((readBytes = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readBytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                inputStream.close();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getDefaultParameterFilePath() {
        return Paths.get(AppConst.VALIDATION_PARAM_FILE_NAME).toAbsolutePath().toString();
    }

    private static String getDefaultLogDestinationPath() {
        return Paths.get(AppConst.VALIDATION_LOG_DESTINATION_DIRECTORY).toAbsolutePath().toString();
    }

    private static void writeValidationLogToFile(String directoryPath, List<ValidationResultMessage> messages) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
        LocalDateTime now = LocalDateTime.now();

        Path directory = Path.of(directoryPath);
        if (!Files.exists(directory)) {
            Files.createDirectory(directory);
        }

        var fileName = String.format("validation-%s.txt", dtf.format(now));
        var filePath = Paths.get(directoryPath, fileName).toString();

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
        for (var message : messages) {
            bw.write(message.getMessage() + "\n");
        }
        bw.close();
    }


    private static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
