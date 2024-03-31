package org.plateau.plateaubuilder.validation.constant;

public interface L13ErrorType {
  int INVALID_FORMAT = 1;
  // Standard 1: に内周が存在し、以下の条件のいずれかに合致する場合、エラーとする
  int INVALID_STANDARD_1 = 2;
  // Standard 2: 内周と外周が接し、gml:Polygonが2つ以上に分割されている
  int INVALID_STANDARD_2 =3;
  // Standard 3: 内周同士が重なる、または包含関係にある。
  int INVALID_STANDARD_3 = 4;
}