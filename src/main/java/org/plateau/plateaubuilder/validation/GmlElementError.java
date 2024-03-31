package org.plateau.plateaubuilder.validation;

import java.util.Objects;

public class GmlElementError {
  private String buildingId;
  private String solidId;
  private String polygonId;
  private String errorElementId;
  private String errorElementNodeName;
  private int error;

  public GmlElementError(String buildingId, String solidId, String polygonId, String errorElementId,
      String errorElementNodeName, int error) {
    this.buildingId = buildingId;
    this.solidId = solidId;
    this.polygonId = polygonId;
    this.errorElementId = errorElementId;
    this.errorElementNodeName = errorElementNodeName;
    this.error = error;
  }

  public String getBuildingId() {
    return buildingId;
  }

  public String getSolidId() {
    return solidId;
  }

  public String getPolygonId() {
    return polygonId;
  }

  public String getErrorElementId() {
    return errorElementId;
  }

  public String getErrorElementNodeName() {
    return errorElementNodeName;
  }

  public int getError() {
    return error;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GmlElementError that = (GmlElementError) o;
    return error == that.error && Objects.equals(buildingId, that.buildingId) && Objects.equals(
        solidId, that.solidId) && Objects.equals(polygonId, that.polygonId) && Objects.equals(
        errorElementId, that.errorElementId) && Objects.equals(errorElementNodeName,
        that.errorElementNodeName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(buildingId, solidId, polygonId, errorElementId, errorElementNodeName,
        error);
  }
}
