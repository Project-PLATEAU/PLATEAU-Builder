package org.plateau.plateaubuilder.validation.constant;

public enum SegmentRelationship {
  // if there is only one intersection
  INTERSECT,
  // if there is only one intersection and it is the end point of one segment
  TOUCH,
  // if the intersection is a segment
  OVERLAP,
  // if there is no common point
  NONE
}
