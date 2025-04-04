package org.plateaubuilder.validation;

import javafx.geometry.Point3D;
import javafx.util.Pair;
import org.plateaubuilder.core.utils3d.geom.Vec3f;

public class SolveEquationUtil {
  /**
   * Solve the linear equation system
   * a1 * x + b1 * y = c1
   * a2 * x + b2 * y = c2
   * @param a1
   * @param b1
   * @param c1
   * @param a2
   * @param b2
   * @param c2
   * @return Pair of (x, y) if the equation has a solution, null otherwise
   */
  public static Pair<Double, Double> solveLinearEquation(double a1, double b1, double c1, double a2, double b2, double c2) {
    double d = a1 * b2 - a2 * b1;
    if (d == 0) {
      return null;
    }
    double dx = c1 * b2 - c2 * b1;
    double dy = a1 * c2 - a2 * c1;
    return new Pair<>(dx / d, dy / d);
  }

  /**
   * The equation of the plane is 'Ax + By + Cz + D = 0'
   * Get Coefficients of the equation (A,B,C,D)
   */
  public static double[] findPlaneEquation(Point3D point1, Point3D point2, Point3D point3) {
    Point3D vector12 = point2.subtract(point1);
    Point3D vector23 = point3.subtract(point2);
    double[] planeEquation = new double[4];
    Vec3f normalVector = createNormal(vector12, vector23);
    if (normalVector.equals(new Vec3f(0, 0, 0))) return null;

    normalVector.normalize();
    // Coefficient A
    planeEquation[0] = normalVector.x;
    // Coefficient B
    planeEquation[1] = normalVector.y;
    // Coefficient C
    planeEquation[2] = normalVector.z;
    // Coefficient D
    planeEquation[3] = -(normalVector.x * point1.getX() + normalVector.y * point1.getY() + normalVector.z * point1.getZ());

    return planeEquation;
  }

  public static boolean onPlane(double[] plane1, double[] plane2){
    return plane1[0] == plane2[0] && plane1[1] == plane2[1] && plane1[2] == plane2[2];
  }

  public static Vec3f createNormal(Point3D vector12, Point3D vector23) {
    double xNormalVector = calculateDeterminant(vector12.getY(), vector12.getZ(), vector23.getY(), vector23.getZ());
    double yNormalVector = calculateDeterminant(vector12.getZ(), vector12.getX(), vector23.getZ(), vector23.getX());
    double zNormalVector = calculateDeterminant(vector12.getX(), vector12.getY(), vector23.getX(), vector23.getY());

    return new Vec3f((float) xNormalVector, (float) yNormalVector, (float) zNormalVector);
  }

  private static double calculateDeterminant(double a1, double a2, double b1, double b2) {
    return a1 * b2 - a2 * b1;
  }

  /**
   * The equation of the line is {x = x0 + at, y = y0 + bt , z = z0 + ct} with a,b,c is coordinates of plane's normal_vector
   * Find the projection of the point on the plane
   *
   * @param plane known
   * @param point need to find project
   */
  public static Point3D projectOntoPlane(double[] plane, Point3D point) {
    // find t in the equation of line
    double a = plane[0];
    double b = plane[1];
    double c = plane[2];
    double d = plane[3];
    double t = -(a * point.getX() + b * point.getY() + c * point.getZ() + d) / (a * a + b * b + c * c);

    return new Point3D(a * t + point.getX(), b * t + point.getY(), c * t + point.getZ());
  }
}
