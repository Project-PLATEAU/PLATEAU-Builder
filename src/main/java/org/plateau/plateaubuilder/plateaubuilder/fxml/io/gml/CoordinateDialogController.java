package org.plateau.plateaubuilder.plateaubuilder.fxml.io.gml;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.plateau.plateaubuilder.citymodel.CityModelGroup;
import org.plateau.plateaubuilder.citymodel.helpers.SchemaHelper;
import org.plateau.plateaubuilder.io.gml.GmlImporter;
import org.plateau.plateaubuilder.plateaubuilder.PLATEAUBuilderApp;
import org.plateau.plateaubuilder.world.World;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class CoordinateDialogController implements Initializable {
    private Stage root;

    private List<File> gmlFiles;

    @FXML
    private ComboBox<CoordinateCodesEnum> comboboxCoordinateCodes;
    
    /**
     * CoordinateCodesEnumは、座標系のコードを表す列挙型です。
     */
    public enum CoordinateCodesEnum {
        EPSG2443("EPSG:2443(平面直角Ⅰ(1)系, 長崎県, 鹿児島県(南西部))"),
        EPSG2444("EPSG:2444(平面直角Ⅱ(2)系, 福岡県, 佐賀県, 熊本県, 大分県, 宮崎県, 鹿児島県(北東部))"),
        EPSG2445("EPSG:2445(平面直角Ⅲ(3)系, 山口県, 島根県, 広島県)"),
        EPSG2446("EPSG:2446(平面直角Ⅳ(4)系, 香川県, 愛媛県, 徳島県, 高知県)"),
        EPSG2447("EPSG:2447(平面直角Ⅴ(5)系, 兵庫県, 鳥取県, 岡山県)"),
        EPSG2448("EPSG:2448(平面直角Ⅵ(6)系, 京都府, 大阪府, 福井県, 滋賀県, 三重県, 奈良県, 和歌山県)"),
        EPSG2449("EPSG:2449(平面直角Ⅶ(7)系, 石川県, 富山県, 岐阜県, 愛知県)"),
        EPSG2450("EPSG:2450(平面直角Ⅷ(8)系, 新潟県, 長野県, 山梨県, 静岡県)"),
        EPSG2451("EPSG:2451(平面直角Ⅸ(9)系, 東京都, 福島県, 栃木県, 茨城県, 埼玉県, 千葉県, 群馬県, 神奈川県)"),
        EPSG2452("EPSG:2452(平面直角Ⅹ(10)系, 青森県, 秋田県, 山形県, 岩手県, 宮城県)"),
        EPSG2453("EPSG:2453(平面直角Ⅺ(11)系, 北海道(西部))"),
        EPSG2454("EPSG:2454(平面直角Ⅻ(12)系, 北海道(中央部))"),
        EPSG2455("EPSG:2455(平面直角ⅩⅢ(13)系, 北海道(東部))"),
        EPSG2456("EPSG:2456(平面直角ⅩⅣ(14)系, 東京都(小笠原諸島))"),
        EPSG2457("EPSG:2457(平面直角ⅩⅤ(15)系, 沖縄県)"),
        EPSG2458("EPSG:2458(平面直角ⅩⅥ(16)系, 沖縄県(先島諸島))"),
        EPSG2459("EPSG:2459(平面直角ⅩⅦ(17)系, 沖縄県(大東諸島))"),
        EPSG2460("EPSG:2460(平面直角ⅩⅧ(18)系, 東京都(沖ノ鳥島))"),
        EPSG2461("EPSG:2461(平面直角ⅩⅨ(19)系, 東京都(南鳥島))");

        /**
         * 座標系の名称。
         */
        final private String name;

        /**
         * 指定された座標系の名称を持つCoordinateCodesEnumを構築します。
         *
         * @param name 座標系の名称
         */
        private CoordinateCodesEnum(String name) {
            this.name = name;
        }

        /**
         * この座標系の名称を返します。
         *
         * @return 座標系の名称
         */
        public String getName() {
            return name;
        }
    }

    /**
     * FXMLのStageを設定
     * 
     * @param stage ルートとして設定するステージ
     */
    public void setRoot(Stage stage) {
        root = stage;
    }

    /**
     * 読み込むGMLファイルを設定
     * 
     * @param files GMLファイルリスト
     */
    public void setFiles(List<File> files) {
        gmlFiles = files;
    }

    /**
     * 座標系選択完了ボタン選択時イベント
     * 
     * @param actionEvent イベントオブジェクト
     */
    public void onSubmit(ActionEvent actionEvent) {
        var codeEnum = comboboxCoordinateCodes.getValue();
        if(codeEnum == null)
            codeEnum = CoordinateCodesEnum.EPSG2451;
        var buffer = new StringBuffer(codeEnum.toString());
        buffer.insert(4, ":");
        String code = buffer.toString();

        try {
            CityModelGroup group = new CityModelGroup();
            World.getActiveInstance().getCityModels().clear();
            for (var gmlFile : gmlFiles) {
                var cityModel = GmlImporter.loadGml(group, gmlFile.toString(), code);
                group.addCityModel(cityModel);
                World.getActiveInstance().addCityModel(cityModel);
            }
            World.getActiveInstance().setCityModelGroup(group);

            var datasetPath = Paths.get(World.getActiveInstance().getCityModels().get(0).getGmlPath()).getParent().getParent().getParent();
            var cityModelView = World.getActiveInstance().getCityModels().get(0);
            var schemaHandler = cityModelView.getSchemaHandler();
            var uroSchema = SchemaHelper.getUroSchema(schemaHandler);
            var uroSchemaLocation = uroSchema == null ? null : SchemaHelper.getSchemaLocation(uroSchema);

            PLATEAUBuilderApp.setDatasetPath(datasetPath.toString());
            PLATEAUBuilderApp.settingUroAttributeInfo(uroSchemaLocation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        root.close();
    }

    /**
     * FXMLファイルがロードされた際に呼び出される初期化メソッドです。
     * 
     * @param location FXMLファイルのURL
     * @param resources ロケール固有のリソースバンドル
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Callback<ListView<CoordinateCodesEnum>, ListCell<CoordinateCodesEnum>> cellFactory
                = (ListView<CoordinateCodesEnum> param) -> new ListCell<CoordinateCodesEnum>() {
            @Override
            protected void updateItem(CoordinateCodesEnum item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.getName());
                }
            }
        };
        ObservableList<CoordinateCodesEnum> list = FXCollections.observableArrayList(CoordinateCodesEnum.values());
        comboboxCoordinateCodes.getItems().addAll(list);
        comboboxCoordinateCodes.setButtonCell(cellFactory.call(null));
        comboboxCoordinateCodes.setCellFactory(cellFactory);
        comboboxCoordinateCodes.setValue(CoordinateCodesEnum.EPSG2451);
    }

    /**
     * 座標系選択ダイアログ表示
     * 
     * @param files GMLファイルリスト
     */
    public static void createCoorinateDialog(List<File> files) {
        try {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(CoordinateDialogController.class.getResource("coordinate-dialog.fxml")));
            stage.setScene(new Scene(loader.load()));
            var controller = (CoordinateDialogController) loader.getController();
            controller.setFiles(files);
            controller.setRoot(stage);
            stage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
