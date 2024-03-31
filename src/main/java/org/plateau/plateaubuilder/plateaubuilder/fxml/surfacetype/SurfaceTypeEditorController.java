package org.plateau.plateaubuilder.plateaubuilder.fxml.surfacetype;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.citygml4j.model.citygml.CityGMLClass;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.control.surfacetype.PolygonSection;
import org.plateau.plateaubuilder.control.surfacetype.*;
import org.plateau.plateaubuilder.plateaubuilder.fxml.UIConstants;
import org.plateau.plateaubuilder.utils.ColorUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class SurfaceTypeEditorController implements Initializable {
    public TabPane modeTabPane;
    public Tab polygonEditTab;
    public VBox polygonEditTypeList;
    public Tab componentEditTab;
    public VBox componentEditTypeList;
    public AnchorPane root;
    public VBox componentPolygonEditNode;
    public Button componentPolygonEditButton;
    public Text componentPolygonSelectTutorialText;
    public Text componentOpeningSelectTutorialText;
    public Button componentOpeningEditButton;
    public VBox componentOpeningEditNode;
    @FXML
    private TabPane componentEditTabPane;
    @FXML
    private Tab componentTypeEditTab;
    @FXML
    private Tab componentPolygonEditTab;
    @FXML
    private Tab componentOpeningEditTab;

    private final ToggleGroup polygonClazzToggleGroup = new ToggleGroup();
    private final ToggleGroup componentClazzToggleGroup = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        polygonEditTypeList.getChildren().clear();
        componentEditTypeList.getChildren().clear();

        SurfaceTypeEditor surfaceTypeEditor = PLATEAUBuilderApp.getSurfaceTypeEditor();

        surfaceTypeEditor.modeProperty().set(SurfaceTypeEditMode.POLYGON_TYPE_EDIT);
        modeTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == componentEditTab)
                surfaceTypeEditor.modeProperty().set(SurfaceTypeEditMode.COMPONENT_TYPE_EDIT);
            else
                surfaceTypeEditor.modeProperty().set(SurfaceTypeEditMode.POLYGON_TYPE_EDIT);
        });

        componentEditTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == componentTypeEditTab)
                surfaceTypeEditor.modeProperty().set(SurfaceTypeEditMode.COMPONENT_TYPE_EDIT);
            else if (newValue == componentPolygonEditTab) {
                surfaceTypeEditor.modeProperty().set(SurfaceTypeEditMode.COMPONENT_POLYGON_EDIT);
                surfaceTypeEditor.getComponentPolygonEditor().reset();
                showComponentSelectUI();
            } else if (newValue == componentOpeningEditTab) {
                surfaceTypeEditor.modeProperty().set(SurfaceTypeEditMode.COMPONENT_OPENING_EDIT);
                surfaceTypeEditor.getComponentPolygonEditor().reset();
                showComponentOpeningSelectUI();
            }
        });

        polygonClazzToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // 全てのトグルがオフになる状態は回避する
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }

            var editor = surfaceTypeEditor.getPolygonTypeEditor();
            var newClass = (CityGMLClass)newToggle.getUserData();
            if (editor.getSurfaceTypeOfSelectedSection() == newClass)
                return;

            editor.setSurfaceTypeOfSelectedSection(newClass);
        });

        componentClazzToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            // 全てのトグルがオフになる状態は回避する
            if (newToggle == null) {
                oldToggle.setSelected(true);
                return;
            }

            surfaceTypeEditor.getComponentTypeEditor().setSurfaceTypeOfSelectedSections((CityGMLClass)newToggle.getUserData());
        });

        var viewMode = PLATEAUBuilderApp.getCityModelViewMode();
        root.setVisible(viewMode.isSurfaceViewModeProperty().get());
        viewMode.isSurfaceViewModeProperty().addListener((observable, oldValue, newValue) -> {
            root.setVisible(newValue);
        });

        surfaceTypeEditor.getPolygonTypeEditor().selectedSectionProperty().addListener(((observable, oldValue, newValue) -> {
            refreshTypeList();
            if (newValue != null)
                setToggle(newValue.getCityGMLClass(), polygonClazzToggleGroup);
        }));

        surfaceTypeEditor.getComponentTypeEditor().selectedSectionsProperty().addListener(((observable, oldValue, newValue) -> {
            refreshTypeList();

            if (newValue == null || newValue.isEmpty())
                return;

            setToggle(newValue.get(0).getCityGMLClass(), componentClazzToggleGroup);
        }));

        // 地物ポリゴン編集
        surfaceTypeEditor.getComponentPolygonEditor().selectedSectionsProperty().addListener((observable, oldValue, newValue) -> {
            var editor = surfaceTypeEditor.getComponentPolygonEditor();
            switch (editor.getMode()) {
                case SELECT_COMPONENT:
                    showComponentSelectUI();
                    break;
                case EDIT_POLYGONS:
                    // Do nothing
                    break;
            }
        });

        surfaceTypeEditor.getComponentPolygonEditor().modeProperty().addListener(((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case SELECT_COMPONENT:
                    showComponentSelectUI();
                    break;
                case EDIT_POLYGONS:
                    showComponentEditUI();
                    break;
            }
        }));

        // 地物開口部編集
        surfaceTypeEditor.getComponentOpeningEditor().selectedTargetProperty().addListener((observable, oldValue, newValue) -> {
            var editor = surfaceTypeEditor.getComponentOpeningEditor();
            switch (editor.getMode()) {
                case SELECT_COMPONENT:
                    showComponentOpeningSelectUI();
                    break;
                case EDIT_OPENINGS:
                    // Do nothing
                    break;
            }
        });

        surfaceTypeEditor.getComponentOpeningEditor().modeProperty().addListener(((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case SELECT_COMPONENT:
                    showComponentOpeningSelectUI();
                    break;
                case EDIT_OPENINGS:
                    showComponentOpeningEditUI();
                    break;
            }
        }));
    }

    private void hideAllComponentPolygonEditUIs() {
        componentPolygonEditButton.setVisible(false);
        componentPolygonEditNode.setVisible(false);
        componentPolygonSelectTutorialText.setVisible(false);
    }

    private void showComponentSelectUI() {
        hideAllComponentPolygonEditUIs();
        if (PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentPolygonEditor().getSelectedSections().isEmpty()) {
            // チュートリアルテキストを表示
            componentPolygonSelectTutorialText.setVisible(true);
        } else {
            componentPolygonEditButton.setVisible(true);
        }
    }

    private void showComponentEditUI() {
        hideAllComponentPolygonEditUIs();
        componentPolygonEditNode.setVisible(true);
    }


    private void hideAllComponentOpeningEditUIs() {
        componentOpeningEditButton.setVisible(false);
        componentOpeningEditNode.setVisible(false);
        componentOpeningSelectTutorialText.setVisible(false);
    }

    private void showComponentOpeningSelectUI() {
        hideAllComponentOpeningEditUIs();
        if (!PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentOpeningEditor().selectedTargetProperty().get()) {
            // チュートリアルテキストを表示
            componentOpeningSelectTutorialText.setVisible(true);
        } else {
            componentOpeningEditButton.setVisible(true);
        }
    }

    private void showComponentOpeningEditUI() {
        hideAllComponentOpeningEditUIs();
        componentOpeningEditNode.setVisible(true);
    }

    private void refreshTypeList() {
        var editor = PLATEAUBuilderApp.getSurfaceTypeEditor();
        var mode = editor.getMode();
        if (mode == SurfaceTypeEditMode.POLYGON_TYPE_EDIT) {
            var selectedSection = editor.getPolygonTypeEditor().getSelectedSection();
            var sectionType = editor.getPolygonTypeEditor().getSurfaceTypeOfSelectedSection();
            refreshTypeList(polygonEditTypeList, polygonClazzToggleGroup, selectedSection, sectionType);
        } else if (mode == SurfaceTypeEditMode.COMPONENT_TYPE_EDIT) {
            var selectedSections = editor.getComponentTypeEditor().getSelectedSections();
            if (selectedSections == null || selectedSections.isEmpty())
                return;

            refreshTypeList(componentEditTypeList, componentClazzToggleGroup, selectedSections.get(0), selectedSections.get(0).getCityGMLClass());
        }
    }

    private void refreshTypeList(VBox list, ToggleGroup toggleGroup, PolygonSection section, CityGMLClass defaultClazz) {
        try {
            for (var toggleButton : list.getChildren()) {
                ((ToggleButton)toggleButton).setToggleGroup(null);
            }
            list.getChildren().clear();

            if (section == null)
                return;

            var clazzes = section.isOpening()
                    ? BuildingSurfaceTypeView.getOpeningComponentTypes()
                    : BuildingSurfaceTypeView.getLOD2BuildingComponentTypes();
            for (var clazz : clazzes) {
                var listItem = FXMLLoader.<ToggleButton>load(Objects.requireNonNull(getClass().getResource("surface-type-editor-list-item.fxml")));
                listItem.setStyle("-fx-text-fill: " + ColorUtils.getWebString(BuildingSurfaceTypeView.getBuildingSurfaceColor(clazz)));
                listItem.setText(UIConstants.buildingTypeDescriptionShort(clazz));
                listItem.setToggleGroup(toggleGroup);
                listItem.setUserData(clazz);
                list.getChildren().add(listItem);

                if (clazz == defaultClazz)
                    listItem.setSelected(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setToggle(CityGMLClass clazz, ToggleGroup toggleGroup) {
        for (var toggle : toggleGroup.getToggles()) {
            if (toggle.getUserData() == clazz) {
                toggleGroup.selectToggle(toggle);
                break;
            }
        }
    }

    public void onClickComponentPolygonEditButton() {
        PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentPolygonEditor().modeProperty().set(ComponentPolygonEditor.EditorMode.EDIT_POLYGONS);
    }

    public void onClickComponentPolygonCancelButton() {
        var editor = PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentPolygonEditor();
        editor.reset();
        PLATEAUBuilderApp.getSurfaceTypeEditor().clear();
    }

    public void onClickComponentPolygonApplyButton() {
        var editor = PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentPolygonEditor();
        editor.applyEdits();
        editor.reset();
        PLATEAUBuilderApp.getSurfaceTypeEditor().clear();
    }

    public void onClickComponentOpeningCancelButton() {
        var editor = PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentOpeningEditor();
        editor.reset();
        PLATEAUBuilderApp.getSurfaceTypeEditor().clear();
    }

    public void onClickComponentOpeningApplyButton() {
        var editor = PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentOpeningEditor();
        editor.applyEdits();
        editor.reset();
        PLATEAUBuilderApp.getSurfaceTypeEditor().clear();
    }

    public void onClickComponentOpeningEditButton() {
        PLATEAUBuilderApp.getSurfaceTypeEditor().getComponentOpeningEditor().modeProperty().set(ComponentOpeningEditor.EditorMode.EDIT_OPENINGS);
    }
}
