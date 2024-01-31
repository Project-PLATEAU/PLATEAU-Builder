package org.plateau.citygmleditor.validation;

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
}
