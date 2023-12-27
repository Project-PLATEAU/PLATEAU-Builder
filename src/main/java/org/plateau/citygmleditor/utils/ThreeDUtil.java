package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;
import org.plateau.citygmleditor.validation.exception.InvalidPosStringException;
import org.plateau.citygmleditor.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ThreeDUtil {
    public static Logger logger = Logger.getLogger(ThreeDUtil.class.getName());

    public static List<Point3D> createListPoint(String[] posString) {
        int length = posString.length;
        if (length == 0 || length % 3 != 0) throw new InvalidPosStringException("Invalid String");

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
                throw new InvalidPosStringException("Invalid String");
            }
        }
        return point3DS;
    }

    public static Vec3f convertGeoToCalculateDistance(Point3D point) {
        return World.getActiveInstance().getGeoReference()
                .project(new GeoCoordinate(point.getX(), point.getY(), point.getZ()));
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
     * Check if 2 line segments are intersected at only 1 point
     * 2 line are intersected if they are only intersected at 1 point
     * @param first line segment
     * @param second line segment
     * @return true if 2 line segments are intersected and otherwise
     */
    public static boolean haveExactlyOneIntersection(LineSegment first, LineSegment second) {

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
     * and start of first line segment does not lie on second line segment
     * @param first line segment
     * @param second line segment
     * @return true if 2 line segments are continuous and otherwise
     */
    public static boolean isLinesContinuous(LineSegment first, LineSegment second) {
        if (first.p1.equals(second.p0)) {
            if (isPointOnLineSegment(second.p1, first.p0, first.p1)) {
                // if end of second line segment lies on first line segment
                return false;
            }
            if (isPointOnLineSegment(first.p0, second.p0, second.p1)) {
                // if start of first line segment lies on second line segment
                return false;
            }
            return true;
        }
        return false;
    }

    public static boolean isPointOnLineSegment(Coordinate pointCoordinate, Coordinate startCoordinate, Coordinate endCoordinate) {
        Point3D point = new Point3D(pointCoordinate.x, pointCoordinate.y, pointCoordinate.z);
        Point3D start = new Point3D(startCoordinate.x, startCoordinate.y, startCoordinate.z);
        Point3D end = new Point3D(endCoordinate.x, endCoordinate.y, endCoordinate.z);

        // Check if the point lies on the line defined by the start and end points
        Point3D lineDirectionVector = end.subtract(start);
        Point3D startToPointVector = point.subtract(start);

        var angle = lineDirectionVector.angle(startToPointVector);

        // Check angle between 2 vectors
        // If angle is 0 or 180, and the sum of distance from start to point and from point to end is equal to distance from start to end
        // It means that the point lies on the line segment
        return angle == 0 || angle == 180 && (point.distance(start) + point.distance(end) == start.distance(end));
    }

    public static Geometry createPolygon(List<Point3D> points) {
        Coordinate[] coordinates = new Coordinate[points.size()];
        for (int i = 0; i < points.size(); i++) {
            Point3D point = points.get(i);
            coordinates[i] = new Coordinate(point.getX(), point.getY(), point.getZ());
        }

        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coordinates);
        LinearRing linearRing = new LinearRing(coordinateSequence, new GeometryFactory());
        return new GeometryFactory().createPolygon(linearRing);
    }
}
