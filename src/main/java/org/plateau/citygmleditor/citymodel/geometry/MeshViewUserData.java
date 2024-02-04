package org.plateau.citygmleditor.citymodel.geometry;

public class MeshViewUserData {
  private String polygonGmlId;
  private String parentGmlId;

  public MeshViewUserData(String polygonId, String parentGmlId) {
    this.polygonGmlId = polygonId;
    this.parentGmlId = parentGmlId;
  }

  public String getParentGmlId() {
    return parentGmlId;
  }

  public void setParentGmlId(String parentGmlId) {
    this.parentGmlId = parentGmlId;
  }

  public String getPolygonGmlId() {
    return polygonGmlId;
  }

  public void setPolygonGmlId(String polygonGmlId) {
    this.polygonGmlId = polygonGmlId;
  }
}
