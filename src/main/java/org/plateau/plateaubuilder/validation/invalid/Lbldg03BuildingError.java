package org.plateau.plateaubuilder.validation.invalid;

import org.plateau.plateaubuilder.validation.constant.MessageError;

import java.util.List;
import java.util.stream.Collectors;

public class Lbldg03BuildingError extends BuildlingError {
    private List<String> doors;
    private List<String> windows;

    public List<String> getDoors() {
        return doors;
    }

    public void setDoors(List<String> doors) {
        this.doors = doors;
    }

    public List<String> getWindows() {
        return windows;
    }

    public void setWindows(List<String> windows) {
        this.windows = windows;
    }

    public String toString() {
        if (super.formatPolygons.isEmpty()) {
            String opening = "部が境界面の外側に存在するbldg:Window及びbldg:Doorのインスタンスが存在します：\n";
            opening = getOpeningStr(opening);
            return String.format(MessageError.ERR_LBLDG_03_001_1, super.buildingID, opening, "");
        } else {
            String polygon = "次のgml:Polygon座標の形式が不正です。\n" + super.formatPolygons.stream().map(d -> "<gml:Polygon gml:id=\"" + d + "\"").collect(Collectors.joining("\n"));
            String opening = "\n部が境界面の外側に存在するbldg:Window及びbldg:Doorのインスタンスが存在します：\n";
            opening = getOpeningStr(opening);
            return String.format(MessageError.ERR_LBLDG_03_001_1, buildingID, polygon, opening);
        }
    }

    private String getOpeningStr(String opening) {
        if (!doors.isEmpty()) {
            String windowStr = this.doors.stream().map(d -> "<bldg:Door gml:id=\"" + d + "\"").collect(Collectors.joining("\n"));
            opening += windowStr;
        }
        if (!windows.isEmpty()) {
            String doorStr = this.windows.stream().map(d -> "<bldg:Window gml:id=\"" + d + "\"").collect(Collectors.joining("\n"));
            opening += doorStr;
        }
        return opening;
    }
}
