package org.plateau.citygmleditor.citygmleditor;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.plateau.citygmleditor.validation.GMLIDCompletenessValidator;
import org.plateau.citygmleditor.validation.IValidator;
import org.plateau.citygmleditor.validation.ValidationResultMessage;
import org.plateau.citygmleditor.validation.ValidationResultMessageType;
import org.plateau.citygmleditor.world.World;

import javafx.event.ActionEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ValidationController implements Initializable {
    @FXML
    VBox resultTextContainer;

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
    }

    public void execute(ActionEvent event) {
        var errorCount = 0;
        var warningCount = 0;

        if (World.getActiveInstance().getCityModels() == null) {
            showMessage(new ValidationResultMessage(ValidationResultMessageType.Error,
                    "CityGMLがインポートされていません"));
            return;
        }
        
        for (var cityModelView : World.getActiveInstance().getCityModels()) {
            var cityModel = cityModelView.getGmlObject();
            if (cityModel == null)
                return;

            List<IValidator> validators = new ArrayList<IValidator>() {
                {
                    add(new GMLIDCompletenessValidator());
                }
            };

            for (var validator : validators) {
                var messages = validator.validate(cityModel);

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
        }
        
        showMessage(new ValidationResultMessage(
                ValidationResultMessageType.Info,
                String.format("品質検査が完了しました。（エラー数:%d, 警告数:%d）", errorCount, warningCount)
        ));
    }
}
