package org.plateau.citygmleditor.constant;

public interface MessageError {
    String ERR_C01_001_1 = "C01: 次のgml:idが重複しています：\n";
    String ERR_C01_001_2 = "gml:id=\"[({0})]\"";

    String ERR_T03_002_1 = "T03: 次のxlink:href属性により参照されたインスタンスについて、応用スキーマで定義された関連相手先の地物型又は幾何オブジェクト型と合致しません\n{0}";

    String ERR_L04_002_1 = "L04: 次の地物属性の値が、コード値と合致しない箇所";
    String ERR_L04_002_2 = "\n<gml:CodeType gml:id=\"[({0})]\">";

    String ERR_C04_BLDG_1_001 = "C04: 地物\"%s\":\n次のuro:buildingIDが重複しています：\n%s";

    String ERR_L05_001 = "L05: gml:Envelopeに記述された空間参照系のURIが、製品仕様書に示されたURIと合致しません。：[({0})]";

    String ERR_L06_001 = "L06: 地物\"({0})\":\n" +
            "次の座標が都市モデルの空間範囲外です:";
    String ERR_L06_002 = "L06: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";

    String ERR_L07_002_1 = "L07: 地物\"({0})\":\n" +
            "次のgml:LineStringとgml:LinearRing座標の形式が不正です。\n{1}" +
            "近接閾値（0.01m）未満の頂点が連続するインスタンスが存在します：\n{2}";
    String ERR_L08_001 = "L08: 地物\"({0})\":";
    String ERR_L08_002 = "L08: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";

    String ERR_L09_001 = "L09: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L09_002_1 = "L09: 地物\"({0})\":";
    String ERR_L09_SELF_INTERSECT = "\n<gml:LinearRing gml:id=\"({0})\">が自己交差しています。";
    String ERR_L09_SELF_CONTACT = "\n<gml:LinearRing gml:id=\"({0})\">が自己接触しています。";
    String ERR_L09_NON_CLOSED = "\n<gml:LinearRing gml:id=\"({0})\">が始終点の不一致しています。";
    String ERR_L09_DUPLICATE_POINT = "\n<gml:LinearRing gml:id=\"({0})\">が重複座標しています。";
    String ERR_L09_INVALID_FORMAT = "\n<gml:LinearRing gml:id=\"({0})\">無効な形式。";

    String ERR_L10_001 = "L10: Building gml:id=\"{0}\" and {1} invalid";
    String ERR_L10_002 = "L10: 地物\"({0})\":({1})";
    String ERR_L10_002_1 = "\n\"<gml:Polygon gml:id=\\\"({0})\\\">の外周または内周の座標列向きが不正です。\"";
    String ERR_L10_003 = "L10: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";

    String ERR_L11_002 = "L11: 地物\"({0})\":";
    String ERR_L11_002_1 = "\n<gml:Polygon gml:id=\"({0})\">に同一平面上にない座標値が存在します。";
    String ERR_L11_003 = "L11: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";

    String ERR_L12_001 = "L12: Building have ({0}) is invalid";
    String ERR_L12_002_1 = "L12: 地物\"({0})\":\n" +
            "次の座標値が許容誤差0.03m以外の同一平面インスタンスが存在します";
    String ERR_L12_002_2 = "\n<gml:Polygon gml:id=\"({0})\">";

    String ERR_L13_001 = "L13: 地物\"%s\": \n%s";
    String ERR_L13_0001 = "<gml:Polygon gml:id=\"%s\">内周が外周と交差している。";
    String ERR_L13_0002 = "<gml:Polygon gml:id=\"%s\">内周と外周が接し、gml:Polygonが2つ以上に分割されている。";
    String ERR_L13_0003 = "<gml:Polygon gml:id=\"%s\">内周同士が重なる、または包含関係にある。";
    String ERR_L13_0000 = "<gml:Polygon gml:id=\"%s\"> 無効な形式";

    String ERR_L14_001 = "L14: Building have ({0}) is invalid";
    String ERR_L14_002 = "地物\"DENW43AL0000OBlN\":\n" +
            "<gml:Polygon gml:id=\"polygon_12345678\">境界面が自己交差している。\n" +
            "<gml:Polygon gml:id=\"polygon_12345678\">境界面が閉じていない。\n" +
            "<gml:Polygon gml:id=\"polygon_12345678\">全ての境界面の向きが外側を向いていない。\n" +
            "<gml:Polygon gml:id=\"polygon_12345678\">境界面が立体を分断している。\n" +
            "<gml:Polygon gml:id=\"polygon_12345678\">境界面が交差している。";

    String ERR_L18_003_1 = "L18: 地物\"({0})\":";
    String ERR_L18_003_2 = "\n<gml:CompositeSurface gml:id=\"[({0})]\">が重複している。";
    String ERR_L18_003_3 = "\n<gml:CompositeSurface gml:id=\"[({0})]\">が接触していない。";
    String ERR_L18_003_4 = "L18: \nその他のエラー。";

    String ERR_LBLDG_01_PREFIX = "L_BLDG_01: ";
    String ERR_LBLDG_01_BUILDING = "地物\"%s\":";
    String ERR_LBLDG_01_SOLID = "次の重複しているgml:Solidが存在します：\n%s";
    String ERR_LBLDG_01_POLYGON = "次のgml:Polygon座標の形式が不正です。\n%s";
    String ERR_LBLDG_01_SOLID_DETAIL = "<gml:Solid gml:id=\"%s\">";
    String ERR_LBLDG_01_POLYGON_DETAIL = "<gml:Polygon gml:id=\"%s\">";

    String ERR_L_BLDG_02_002_1 = "L_BLDG 02: 地物\"({0})\":\n" +
            "次の境界面を共有していないgml:Solidが存在します：\n";
    String ERR_LBLDG_02_002_2 = "\n<gml:BuildingPart gml:id=“[({0})]”>";

    String ERR_LBLDG_03_001_1 = "L_BLDG 03: 地物\"({0})\":\n" +
            "部が境界面の外側に存在するbldg:Window及びbldg:Doorのインスタンスが存在します：";
    String ERR_LBLDG_03_001_2 = "\n<bldg:Door gml:id=\"[({0})]”>";
    String ERR_LBLDG_03_001_3 = "\n<bldg:Window gml:id=\"[({0})]”>";

    String ERR_T_Bldg_02_002_1 = "T_BLDG 02: 次のbldg:lod2Geometry又はbldg:lod3Geometryにより保持又は参照する幾何オブジェクトの型が、gml:MultiSurface又はgml:Solidではないインスタンが存在します：";
    String ERR_T_Bldg_02_002_2 = "T_BLDG 02: <bldg:BuildingInstallation gml:id=“[({0})]”>";


}
