package org.plateau.plateaubuilder.utils;

import javafx.scene.paint.Color;

public class ColorUtils {
    /**
     * 与えられた色のウェブ表現を取得します。
     * ウェブ表現は '#' で始まる16進文字列の形式です。
     *
     * @param color ウェブ表現を取得する色。
     */
    public static String getWebString(Color color) {
        // 0x...から最初の２文字削除
        var hexString = color.toString().substring(2);
        return "#" + hexString;
    }
}
