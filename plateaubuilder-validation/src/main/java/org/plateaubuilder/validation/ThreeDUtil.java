package org.plateaubuilder.validation;

import javafx.geometry.Point3D;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Segment;
import org.apache.commons.math3.geometry.euclidean.threed.SubLine;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.plateaubuilder.core.geospatial.GeoCoordinate;
import org.plateaubuilder.core.utils3d.geom.Vec3f;
import org.plateaubuilder.validation.constant.SegmentRelationship;
import org.plateaubuilder.validation.constant.TagName;
import org.plateaubuilder.validation.exception.InvalidPosStringException;
import org.plateaubuilder.core.world.World;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    public static List<LineSegment3D> getLineSegments(String[] posList) {
        List<Point3D> points = createListPoint(posList);
        List<LineSegment3D> lineSegments = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            Point3D start = points.get(i);
            Point3D end = points.get(i + 1);
            LineSegment3D lineSegment = new LineSegment3D(start, end);
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

    private static final double TOLERANCE = 1e-10;

    public static SegmentRelationship checkSegmentsRelationship(Vector3D segment1Start, Vector3D segment1End,
                                                                Vector3D segment2Start, Vector3D segment2End) {

        Line line1 = new Line(segment1Start, segment1End, TOLERANCE);

        SubLine segment1 = new SubLine(segment1Start, segment1End, TOLERANCE);
        SubLine segment2 = new SubLine(segment2Start, segment2End, TOLERANCE);

        if (line1.contains(segment2Start) && line1.contains(segment2End)) {
            if (isPointInLineSegment(segment2Start, segment1, false)
                || isPointInLineSegment(segment2End, segment1, false)) {
                // If end points of segment2 are in segment1 (without end points of segment1)
                // segment2 is overlap segment1
                return SegmentRelationship.OVERLAP;
            } else if (isPointInLineSegment(segment2Start, segment1, true)
                || isPointInLineSegment(segment2End, segment1, true)) {
                // end points of segment2 is one of end points of segment1
                // (and end points of segment2 is not any other point of segment1)
                return SegmentRelationship.TOUCH;
            }
            return SegmentRelationship.NONE;
        }

        // Get intersection point of 2 line segments without end points
        if (segment1.intersection(segment2, false) != null) {
            return SegmentRelationship.INTERSECT;
        }

        // Get intersection point of 2 line segments including end points
        if (segment1.intersection(segment2, true) != null) {
            return SegmentRelationship.TOUCH;
        }

        return SegmentRelationship.NONE;
    }

    private static boolean isPointInLineSegment(Vector3D point, SubLine subLine, boolean includeEndPoints) {
        Segment segment = subLine.getSegments().get(0);
        double length = segment.getStart().distance(segment.getEnd());

        var pointToStart = point.distance(segment.getStart());
        var pointToEnd = point.distance(segment.getEnd());

        if (!includeEndPoints) {
            // If not include end points, the distance from point to start or end should not be near to 0
            if (pointToStart <= (0 + TOLERANCE) || pointToEnd <= (0 + TOLERANCE)) {
                return false;
            }
        }

        var sumDistance = pointToStart + pointToEnd;

        return (length - TOLERANCE) <= sumDistance && sumDistance <= (length + TOLERANCE);
    }

    /**
     * Get list of 3d points from LinearRing node
     * @param linearRingNode LinearRing node
     * @return list of 3d points
     */
    public static List<Point3D> get3dPoints(Node linearRingNode) {
        Element linearRingElement = (Element) linearRingNode;
        // There are 2 cases: posList and pos of LinearRing
        NodeList posListNodes = linearRingElement.getElementsByTagName(TagName.GML_POSLIST);
        NodeList posNodes = linearRingElement.getElementsByTagName(TagName.GML_POS);

        if (posListNodes.getLength() > 0) {
            String[] posString = posListNodes.item(0).getTextContent().split(" ");
            return ThreeDUtil.createListPoint(posString);
        } else if (posNodes.getLength() > 0) {
            List<Point3D> point3DS = new ArrayList<>();
            for (int i = 0; i < posNodes.getLength(); i++) {
                Node posNode = posNodes.item(i);
                String[] posString = posNode.getTextContent().split(" ");
                point3DS.addAll(ThreeDUtil.createListPoint(posString));
            }
            return point3DS;
        }
        return List.of();
    }
}
