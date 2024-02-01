package org.plateau.citygmleditor.citygmleditor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.modelstandard.Standard;
import org.plateau.citygmleditor.utils.CollectionUtil;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.validation.*;
import org.plateau.citygmleditor.world.World;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.plateau.citygmleditor.constant.StandardID.*;
import static org.plateau.citygmleditor.validation.AppConst.VALIDATION_CONFIG_PATH_DEFAULT;

public class ValidationController implements Initializable {
    @FXML
    VBox resultTextContainer;

    @FXML
    ScrollPane scrollContentError;

    @FXML
    TextField pathJsonFile;

    private static String JSON_PATH_CONFIG = "";

    private List<ValidationResultMessage> validationResultMessages;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathJsonFile.setText(VALIDATION_CONFIG_PATH_DEFAULT);
    }

    private void showMessage(ValidationResultMessage message) {
        var text = new TextField(message.getMessage());
        switch (message.getType()) {
            case Info:
                text.setStyle("-fx-font-size: 16px;");
                break;
            case Warning:
                text.setStyle("-fx-text-fill: yellow; -fx-font-size: 16px;");
                break;
            case Error:
                text.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                break;
        }
        resultTextContainer.getChildren().add(text);
        scrollContentError.setContent(resultTextContainer);
        if (!CollectionUtil.isEmpty(message.getElementErrors())) {
            highlightErrors(message.getElementErrors());
        }
    }

    private void hiddenMessageLoading(int index) {
        resultTextContainer.getChildren().remove(index - 1);
    }

    private void showLoadingMessage() {
        showMessage(new ValidationResultMessage(
                ValidationResultMessageType.Info, "検証中..."
        ));
    }

    private Task<Void> createValidationTask(CityModelView cityModelView) {
        return new Task<>() {
          @Override
          protected Void call() throws Exception {
            validationResultMessages = new ArrayList<>();
            var cityModel = cityModelView.getGmlObject();
            if (cityModel == null) {
              return null;
            }

            if (JSON_PATH_CONFIG.isBlank()) {
              JSON_PATH_CONFIG = VALIDATION_CONFIG_PATH_DEFAULT;
            } else {
              pathJsonFile.setText(JSON_PATH_CONFIG);
            }

            List<IValidator> validators = loadValidators(JSON_PATH_CONFIG);
            for (var validator : validators) {
              validationResultMessages.addAll(validator.validate(cityModelView));
            }

            return null;
          }
        };
    }

    private int getIndexLoadingMessage() {
        return resultTextContainer.getChildren().size();
    }

    private EventHandler<WorkerStateEvent> validateFinishedHandler() {
        return event -> {
            int indexValidating = getIndexLoadingMessage();
            var errorCount = 0;
            var warningCount = 0;
            List<String> errorMessages = new ArrayList<>();
            for (var message : validationResultMessages) {
                showMessage(message);
                errorMessages.add(message.getMessage());
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
            hiddenMessageLoading(indexValidating);
            showMessage(new ValidationResultMessage(
                ValidationResultMessageType.Info, countResultMessage
            ));
            errorMessages.add(countResultMessage);
            try {
                XmlUtil.writeErrorMessageInFile(errorMessages);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public void execute() {
        var cityModelView = World.getActiveInstance().getCityModel();
        if (cityModelView == null || cityModelView.getGmlObject() == null) {
            showMessage(new ValidationResultMessage(
                    ValidationResultMessageType.Error,
                    "CityGMLがインポートされていません"
            ));
            return;
        }

        showLoadingMessage();

        Task<Void> validationTask = createValidationTask(cityModelView);
        validationTask.setOnSucceeded(validateFinishedHandler());

        Thread validationThread = new Thread(validationTask);
        validationThread.start();
    }

    public void setPathFileJson() {
        var file = FileChooserService.showOpenDialog("*.json", SessionManager.JSON_FILE_PATH_CONFIG);

        if (file == null)
            return;

        JSON_PATH_CONFIG = file.getPath();
    }

    private List<IValidator> loadValidators(String path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<Standard> standards = mapper.readValue(new File(path), new TypeReference<>() {
        });
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
                    result.add(new L18LogicalConsistencyValidator());
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
                    result.add(new Lbldg03LogicalAccuaracyValidator());
                    break;
                case T_BLDG_02:
                    result.add(new Tbldg02ThematicAccuaracyValidator());
                    break;
                case L14:
                    result.add(new L14LogicalAccuaracyValidator());
                    break;
            }
        }
        return result;
    }

    private void highlightErrors(List<GmlElementError> elementErrors) {
        elementErrors.forEach(this::highlightError);
    }

    private void highlightError(GmlElementError elementError) {
        World.getActiveInstance().getCityModel()
            .lookupAll("#" + elementError.getBuildingId())
            .forEach(node -> {
                ((BuildingView) node).getLOD1Solid().setMaterial(new PhongMaterial(Color.RED));
                ((BuildingView) node).getLOD2Solid().getSurfaceTypeView().setMaterial(new PhongMaterial(Color.RED));
                ((BuildingView) node).getLOD2Solid().getMeshView().setMaterial(new PhongMaterial(Color.RED));

                // TODO check LOD2SolidView highlight, now only highlight bottom surface
                // TODO highlight solid and polygon
            });
    }
}
