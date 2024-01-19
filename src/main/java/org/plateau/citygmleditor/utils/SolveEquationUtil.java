package org.plateau.citygmleditor.utils;

import javafx.geometry.Point3D;
import javafx.util.Pair;
import org.plateau.citygmleditor.utils3d.geom.Vec3f;

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

  /**
   * create uint
   *
   * @param startPoint
   * @param endPoint
   */
  public static Vec3f createUnit(Point3D startPoint, Point3D endPoint) {
    float unitX = (float) (endPoint.getX() - startPoint.getX());
    float unitY = (float) (endPoint.getY() - startPoint.getY());
    float unitZ = (float) (endPoint.getZ() - startPoint.getZ());
    Vec3f unit = new Vec3f(unitX, unitY, unitZ);
    unit.normalize();
    return unit;
  }


  public static Vec3f createNormal(Point3D vector12, Point3D vector23) {
    double xNormalVector = calculateDeterminant(vector12.getY(), vector12.getZ(), vector23.getY(), vector23.getZ());
    double yNormalVector = calculateDeterminant(vector12.getZ(), vector12.getX(), vector23.getZ(), vector23.getX());
    double zNormalVector = calculateDeterminant(vector12.getX(), vector12.getY(), vector23.getX(), vector23.getY());

    return new Vec3f((float) xNormalVector, (float) yNormalVector, (float) zNormalVector);
  }

  public static double calculateDeterminant(double a1, double a2, double b1, double b2) {
    return a1 * b2 - a2 * b1;
  }
}
