package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;
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

    public static List<LineSegment3D> getLineSegments(List<Point3D> points) {
        List<LineSegment3D> lineSegments = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point3D start = points.get(i);
            Point3D end = points.get(i + 1);
            LineSegment3D lineSegment = new LineSegment3D(start, end);
            lineSegments.add(lineSegment);
        }
        return lineSegments;
    }

    public static boolean isLineSegmentsIntersect(Point3D firstStart, Point3D firstEnd,
        Point3D secondStart, Point3D secondEnd, boolean lineSegmentsAreContinuous) {

        // check if 2 lines are continuous
        if (lineSegmentsAreContinuous) {
            if (firstEnd.equals(secondStart) && !firstStart.equals(secondEnd)) {
                return false;
            }
        }

        Point3D line1Vector = firstEnd.subtract(firstStart);
        Point3D line2Vector = secondEnd.subtract(secondStart);

        // check if 2 lines are parallel or coincident
        if (line1Vector.crossProduct(line2Vector).equals(Point3D.ZERO)) {
            // Check if 2 line segments are coincident if one of the points of one line is on the other line segment
            return isPointOnLineSegment(firstStart, secondStart, secondEnd) || isPointOnLineSegment(
                firstEnd, secondStart, secondEnd) || isPointOnLineSegment(secondStart, firstStart,
                firstEnd) || isPointOnLineSegment(secondEnd, firstStart, firstEnd);
        }

        var parameterT1T2 = SolveEquationUtil.solveLinearEquation(line1Vector.getX(),
            line2Vector.getX(), secondStart.getX() - firstStart.getX(), line1Vector.getY(),
            line2Vector.getY(), secondStart.getY() - firstStart.getY());

        if (parameterT1T2 == null) {
            // 2 lines are parallel or coincident
            return false;
        }

        var t1 = parameterT1T2.getKey();
        var t2 = parameterT1T2.getValue();

        Point3D intersect = firstStart.add(line1Vector.multiply(t1));

        return t1 >= 0 && t1 <= 1 && t2 >= 0 && t2 <= 1;
    }

    public static boolean isPointOnLineSegment(Point3D point, Point3D start, Point3D end) {
        // Check if the point lies on the line defined by the start and end points
        Point3D direction = end.subtract(start);
        Point3D pointToStart = point.subtract(start);

        // Check if the vectors are collinear
        double dotProduct = direction.dotProduct(pointToStart);
        if (Math.abs(dotProduct) < 0) {
            return false;
        }

        // Check if the point is within the line segment
        double lengthSquared = direction.dotProduct(direction);
        double projectionLength = dotProduct / lengthSquared;
        return projectionLength >= 0 && projectionLength <= 1;
    }
}
