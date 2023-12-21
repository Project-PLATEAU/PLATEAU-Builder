package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.validation.LineSegment3D;
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

    public static List<LineSegment> getLineSegments(List<Point3D> points) {
        List<LineSegment> lineSegments = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point3D start = points.get(i);
            Point3D end = points.get(i + 1);
            Coordinate startCoordinate = new Coordinate(start.getX(), start.getY(), start.getZ());
            Coordinate endCoordinate = new Coordinate(end.getX(), end.getY(), end.getZ());
            LineSegment lineSegment = new LineSegment(startCoordinate, endCoordinate);
            lineSegments.add(lineSegment);
        }
        return lineSegments;
    }

    /**
     * Check if 2 line segments are intersected
     * 2 line are intersected if they are only intersected at 1 point
     * @param first line segment
     * @param second line segment
     * @return true if 2 line segments are intersected and otherwise
     */
    public static boolean isLineIntersect(LineSegment first, LineSegment second) {

        // Find one of the intersection points
        var intersection = first.intersection(second);

        if (intersection == null) {
            // No intersection
            return false;
        }

        // 2 line segments are not parallel or coincident and have intersection
        return first.angle() != second.angle();
    }

    /**
     * Check if 2 line segments are continuous
     * 2 line segments are continuous if end of first line segment is the same as start of second line segment
     * but end of second line segment does not lie on first line segment
     * @param first line segment
     * @param second line segment
     * @return true if 2 line segments are continuous and otherwise
     */
    public static boolean isLinesContinuous(LineSegment first, LineSegment second) {
        if (first.p1.equals(second.p0) && first.distance(second.p1) != 0) {
            // end of first line segment is the same as start of second line segment
            // but end of second line segment does not lie on first line segment
            return true;
        }
        return false;
    }
}
