package org.plateau.citygmleditor.constant;

public interface MessageError {
    String ERR_L10_001 = "L10: Building gml:id=\"{0}\" and {1} invalid";

    String ERR_L12_001 = "L12: Building have ({0}) is invalid";
    String ERR_L13_001 = "L13: Building gml:id=\"{0}\" and {1} invalid";
    String ERR_L11_001 = "L11: Building have ({0}) is invalid";
    String ERR_L07_001 = "L07: Building have ({0}) is invalid";
    String ERR_L04_001 = "L04: Building have ({0}) is invalid";
    String ERR_LBLDG_02_001 = "L bldg 02: Building have ({0}) is invalid";
    String ERR_C04_001 = "C04: Building have ({0}) is invalid";
    String ERR_L18_001 = "L18: Building have ({0}) is invalid";
    String ERR_T03_001 = "T03: Xhref ({0}) is invalid";
    String ERR_BLDG03_001 = "BLDG 03: Window tag does not exist inside boundedBy";
    String ERR_BLDG03_002 = "BLDG 03: Door tag does not exist inside boundedBy";
    String ERR_T_Bldg_02_001 = "T-bldg-02: Building have ({0}) is invalid";
    String ERR_L14_001 = "L14: Building have ({0}) is invalid";

    String ERR_L05_001 = "gml:Envelopeに記述された空間参照系のURIが、製品仕様書に示されたURIと合致しません。：[({0})]";
    String ERR_L06_001 = "地物\"({0})\":\n" +
            "次の座標が都市モデルの空間範囲外です:";
    String ERR_L07_002 = "地物\"({0})\":\n" +
            "近接閾値（0.01m）未満の頂点が連続するインスタンスが存在します：";
    String ERR_L07_002_1 = "\n<gml:LinearRing gml:id=\"line_12345679\">\n";
    String ERR_L08_001 = "地物\"({0})\":";
    String ERR_L10_002 = "地物\"({0})\":({1})";
    String ERR_L10_002_1 = "\n\"<gml:Polygon gml:id=\\\"({0})\\\">の外周または内周の座標列向きが不正です。\"";
    String ERR_L11_002 = "地物\"({0})\":";
    String ERR_L11_002_1 = "\n<gml:Polygon gml:id=\"({1})\">に同一平面上にない座標値が存在します。";

    String ERR_L06_002 = "L06: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L08_002 = "L08: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L09_001 = "L09: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L10_003 = "L10: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L11_003 = "L11: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L13_002 = "L13: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
    String ERR_L18_002 = "L18: Building have (bldg:Building gml:id=({0}) LinearRing = ({1})) is valid";
}
