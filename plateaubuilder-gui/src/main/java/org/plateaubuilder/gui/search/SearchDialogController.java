package org.plateaubuilder.gui.search;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.plateaubuilder.core.citymodel.IFeatureView;
import org.plateaubuilder.core.editor.Editor;
import org.plateaubuilder.core.editor.filters.AndFilter;
import org.plateaubuilder.core.editor.filters.IFeatureFilter;
import org.plateaubuilder.core.editor.filters.OrFilter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 地物検索機能のコントローラクラスです。
 * このクラスはFXMLファイルに定義されたGUI要素にアクセスし、それらの要素のイベントやプロパティを処理します。
 */
public class SearchDialogController implements Initializable {
    private static List<String> typeListInstance;
    private static List<IFeatureView> featureListInstance;
    private Stage root;
    private boolean dialogResult = false;
    private boolean isAndLogic = true;
    private List<SearchConditionController> searchConditions = new ArrayList<>();
    private IFeatureFilter filter;

    @FXML
    VBox subSceneContainer;

    private EventHandler<javafx.event.ActionEvent> onCloseAction;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addCondition();
    }

    /**
     * FXMLのStageを設定
     * 
     * @param stage
     */
    public void setRoot(Stage stage) {
        root = stage;
        stage.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                if (onCloseAction != null) {
                    onCloseAction.handle(new ActionEvent());
                }
            }
        });
    }

    public void show() {
        root.show();
    }

    public void showAndWait() {
        root.showAndWait();
    }

    public void close() {
        root.close();
    }

    public void onAddCondition() {
        addCondition();
    }

    public void onAndCondition() {
        isAndLogic = true;
    }

    public void onOrCondition() {
        isAndLogic = false;
    }

    public void onCancel() {
        dialogResult = false;
        this.root.close();
    }

    public void onOK() {
        filter = createFilter();
        dialogResult = true;
        this.root.close();
    }

    public boolean getDialogResult() {
        return dialogResult;
    }

    public IFeatureFilter getFilter() {
        return filter;
    }

    public void setOnCloseAction(EventHandler<javafx.event.ActionEvent> onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    /**
     * 検索ダイアログを生成します。
     */
    public static SearchDialogController createSearchDialog(List<String> typeList, List<IFeatureView> featureList) {
        try {
            typeListInstance = typeList;
            featureListInstance = featureList;
            Stage stage = new Stage();
            stage.initModality(Modality.NONE);
            stage.initOwner(Editor.getWindow());
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(SearchDialogController.class.getResource("search-dialog.fxml")));
            stage.setScene(new Scene(loader.load()));
            var controller = (SearchDialogController) loader.getController();
            controller.setRoot(stage);
            return controller;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void addCondition() {
        var controller = new SearchConditionController(typeListInstance, featureListInstance);
        searchConditions.add(controller);
        subSceneContainer.getChildren().add(controller.getRoot());
        controller.buttonDelete.setOnAction(event -> {
            searchConditions.remove(controller);
            subSceneContainer.getChildren().remove(controller.getRoot());
        });
    }

    private IFeatureFilter createFilter() {
        if (searchConditions.isEmpty()) {
            return null;
        }
        if (searchConditions.size() == 1) {
            return searchConditions.get(0).createFilter();
        }

        var logicFilter = isAndLogic ? new AndFilter() : new OrFilter();
        var filters = new ArrayList<IFeatureFilter>();
        for (var condition : searchConditions) {
            var filter = condition.createFilter();
            if (this.filter == null) {
                filters.add(filter);
            }
        }
        return isAndLogic ? AndFilter.create(filters.toArray(IFeatureFilter[]::new)) : OrFilter.create(filters.toArray(IFeatureFilter[]::new));
    }
}
