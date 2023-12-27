package org.plateau.citygmleditor.citygmleditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.plateau.citygmleditor.validation.*;
import org.plateau.citygmleditor.world.World;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ValidationController implements Initializable {
    @FXML
    VBox resultTextContainer;

    @FXML
    ScrollPane scrollContentError;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

        List<IValidator> validators = new ArrayList<>() {
            {
                add(new GMLIDCompletenessValidator());
                add(new L05LogicalConsistencyValidator());
                add(new L06LogicalConsistencyValidator());
                add(new L07LogicalConsistencyValidator());
                add(new L08LogicalConsistencyValidator());
                add(new L09LogicalConsistencyValidator());
                add(new L11LogicalConsistencyValidator());
                add(new L12LogicalConsistencyValidator());
                add(new C04CompletenessValidator());
                add(new L18LogicalConsistencyValidator());
            }
        };

        var errorCount = 0;
        var warningCount = 0;

        for (var validator : validators) {
            var messages = validator.validate(cityModelView);

            for (var message : messages) {
                showMessage(message);
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

        showMessage(new ValidationResultMessage(
                ValidationResultMessageType.Info,
                String.format("品質検査が完了しました。（エラー数:%d, 警告数:%d）", errorCount, warningCount)
        ));
    }
}
