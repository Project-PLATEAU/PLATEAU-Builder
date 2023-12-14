package org.plateau.citygmleditor.utils;

import javafx.util.Pair;

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
}
