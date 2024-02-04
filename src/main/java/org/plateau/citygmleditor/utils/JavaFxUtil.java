package org.plateau.citygmleditor.utils;

import javafx.scene.Node;
import org.plateau.citygmleditor.citymodel.geometry.MeshViewUserData;

public class JavaFxUtil {
    public static void setMeshViewUserData(Node meshView, MeshViewUserData userData) {
        meshView.setUserData(userData);
    }

  public static MeshViewUserData getMeshViewUserData(Node meshView) {
      var userData = meshView.getUserData();
    return userData instanceof MeshViewUserData ? (MeshViewUserData) userData : null;
  }
}
