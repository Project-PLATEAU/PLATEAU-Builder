package org.plateau.citygmleditor.constant;

public enum L13ErrorType {
  INVALID_FORMAT,
  // Standard 1: に内周が存在し、以下の条件のいずれかに合致する場合、エラーとする
  INVALID_STANDARD_1,
  // Standard 2: 内周と外周が接し、gml:Polygonが2つ以上に分割されている
  INVALID_STANDARD_2,
  // Standard 3: 内周同士が重なる、または包含関係にある。
  INVALID_STANDARD_3
}