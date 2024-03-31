package org.plateau.plateaubuilder.validation.invalid;

import org.plateau.plateaubuilder.validation.constant.MessageError;
import org.plateau.plateaubuilder.utils.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;

public class Lbldg02BuildingError extends BuildlingError {
    private List<String> buildingParts;

    public List<String> getBuildingPart() {
        return buildingParts;
    }

    public void setBuildingPart(List<String> buildingPart) {
        this.buildingParts = buildingPart;
    }

    public String toString() {
        if (CollectionUtil.isEmpty(super.formatPolygons) && CollectionUtil.isEmpty(buildingParts)) return "";
        if (CollectionUtil.isEmpty(buildingParts)) {
            String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n"
                    + super.formatPolygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>")
                    .collect(Collectors.joining(":\n"));
            return String.format(MessageError.ERR_L_BLDG_02_001, super.buildingID, polygonStr, "");
        }
        if (CollectionUtil.isEmpty(super.formatPolygons)) {
            String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
            return String.format(MessageError.ERR_L_BLDG_02_001, super.buildingID, bpStr, "");
        }
        String bpStr = "次の境界面を共有していないgml:Solidが存在します：\n" + this.buildingParts.stream().map(b -> "<gml:BuildingPart gml:id=“" + b + "”>").collect(Collectors.joining(":\n"));
        String polygonStr = "次のgml:Polygon座標の形式が不正です。：\n" + super.formatPolygons.stream().map(p -> "<gml:Polygon gml:id=“" + p + "”>").collect(Collectors.joining(":\n"));
        return String.format(MessageError.ERR_L_BLDG_02_001, super.buildingID, polygonStr, "\n" + bpStr);
    }
}
