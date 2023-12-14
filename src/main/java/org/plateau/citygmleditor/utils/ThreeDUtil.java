package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ThreeDUtil {
    public static Logger logger = Logger.getLogger(ThreeDUtil.class.getName());

    public static List<Point3D> createListPoint(String[] posString) {

        int length = posString.length;
        if (length == 0 || length % 3 != 0) throw new RuntimeException("Invalid String");

        List<Point3D> point3DS = new ArrayList<>();
        for (int i = 0; i <= length - 3; ) {
            try {
                double x = Double.parseDouble(posString[i++]);
                double y = Double.parseDouble(posString[i++]);
                double z = Double.parseDouble(posString[i++]);
                Point3D point = new Point3D(x, y, z);
                point3DS.add(point);
            } catch (NumberFormatException e) {
                logger.severe("Error when parse from string to double");
                throw new RuntimeException("Invalid String");
            }
        }
        return point3DS;
    }
}
