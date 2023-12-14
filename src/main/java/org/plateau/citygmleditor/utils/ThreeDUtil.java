package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.world.World;

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

    public static Vec3f convertGeoToCalculateDistance(Point3D point) {
        return World.getActiveInstance().getGeoReference()
                .Project(new GeoCoordinate(point.getX(), point.getY(), point.getZ()));
    }

    public static double distance(Point3D input1, Point3D input2) {
        Vec3f point1 = convertGeoToCalculateDistance(input1);
        Vec3f point2 = convertGeoToCalculateDistance(input2);
        point2.sub(point1);

        double x = point2.x;
        double y = point2.y;
        double z = point2.z;
        return Math.sqrt(x * x + y * y + z * z);
    }
}
