package org.plateau.plateaubuilder.validation;

import javafx.geometry.Point3D;

public class LineSegment3D {

  private Point3D start;
  private Point3D end;

  public LineSegment3D(Point3D start, Point3D end) {
    this.start = start;
    this.end = end;
  }

  public Point3D getStart() {
    return start;
  }

  public void setStart(Point3D start) {
    this.start = start;
  }

  public Point3D getEnd() {
    return end;
  }

  public void setEnd(Point3D end) {
    this.end = end;
  }

  public boolean equal(LineSegment3D other) {
    return (this.start.equals(other.start) && this.end.equals(other.end)) || (this.start.equals(other.end) && this.end.equals(other.start));
  }
}
