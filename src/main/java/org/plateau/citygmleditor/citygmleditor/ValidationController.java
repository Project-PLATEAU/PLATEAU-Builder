package org.plateau.citygmleditor.citygmleditor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Shape3D;
import org.plateau.citygmleditor.citymodel.BuildingView;
import org.plateau.citygmleditor.citymodel.geometry.LOD1SolidView;
import org.plateau.citygmleditor.modelstandard.Standard;
import org.plateau.citygmleditor.utils.XmlUtil;
import org.plateau.citygmleditor.validation.*;
import org.plateau.citygmleditor.world.World;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathJsonFile.setText(VALIDATION_CONFIG_PATH_DEFAULT);
    }

    private void showMessage(ValidationResultMessage message) {
        var text = new Button(message.getMessage());
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

        var x = World.getActiveInstance().getCityModel().lookupAll("#" + message.getElementErrors().get(0).getBuildingId());
        x.forEach(n -> {
//            ((BuildingView)n).getLOD1Solid().setMaterial(new PhongMaterial(Color.GREEN));

            var k = ((BuildingView) n).getChildrenUnmodifiable();
            for(var i=0; i<k.size(); i+=2) {
                if (k.get(i) instanceof Shape3D) {
                    ((Shape3D) k.get(i)).setMaterial(new PhongMaterial(Color.LIGHTBLUE));
                }
            }



            ((BuildingView) n).getLOD1Solid().getPolygons().forEach(p -> {
                if (p.getSurfaceData() != null) {
                    p.getSurfaceData().setMaterial(new PhongMaterial(Color.LIGHTBLUE));
                }
            });


            if (n instanceof BuildingView) {
                 ((BuildingView)n).getLOD1Solid().setMaterial(new PhongMaterial(Color.GREEN));
//                ((BuildingView)n).getLOD2Solid().getMeshView().setStyle("-fx-background-color: #0d5db9;");
//                ((BuildingView)n).getLOD2Solid().getMeshView().setMaterial(new PhongMaterial(Color.GREEN));
//                ((BuildingView)n).getLOD2Solid().getSurfaceTypeView().setStyle("-fx-background-color: #1776e7;");
//                ((BuildingView)n).getLOD2Solid().getSurfaceTypeView().setMaterial(new PhongMaterial(Color.GREEN));
            }
        });

        resultTextContainer.getChildren().add(text);
        scrollContentError.setContent(resultTextContainer);
    }

    public void execute(ActionEvent event) throws ParserConfigurationException, IOException, SAXException {
        var cityModelView = World.getActiveInstance().getCityModel();
        if (cityModelView == null || cityModelView.getGmlObject() == null) {
            showMessage(new ValidationResultMessage(
                    ValidationResultMessageType.Error,
                    "CityGMLがインポートされていません"
            ));
            return;
        }

        var cityModel = cityModelView.getGmlObject();
        if (cityModel == null)
            return;

        var errorCount = 0;
        var warningCount = 0;

        if (JSON_PATH_CONFIG.isBlank()) {
            JSON_PATH_CONFIG = VALIDATION_CONFIG_PATH_DEFAULT;
        } else {
            pathJsonFile.setText(JSON_PATH_CONFIG);
        }
        List<IValidator> validators = this.loadValidators(JSON_PATH_CONFIG);

        List<String> errorMessages = new ArrayList<>();
        for (var validator : validators) {
            var messages = validator.validate(cityModelView);

            for (var message : messages) {
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
        }

        String resultMessage = String.format("品質検査が完了しました。（エラー数:%d, 警告数:%d）", errorCount, warningCount);
        errorMessages.add(resultMessage);
        showMessage(new ValidationResultMessage(
                ValidationResultMessageType.Info, resultMessage
        ));

        XmlUtil.writeErrorMessageInFile(errorMessages);
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
}
