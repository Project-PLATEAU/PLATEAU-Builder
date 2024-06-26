package org.plateau.plateaubuilder.world;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.plateau.plateaubuilder.control.transform.GizmoModel;
import org.plateau.plateaubuilder.citymodel.BuildingView;
import org.plateau.plateaubuilder.citymodel.CityModelGroup;
import org.plateau.plateaubuilder.citymodel.CityModelView;
import org.plateau.plateaubuilder.citymodel.GMLViewTable;
import org.plateau.plateaubuilder.geometry.GeoReference;

import java.util.ArrayList;
import java.util.List;

public class World {
    private static World activeInstance;
    private static Group root3D;
    private ObjectProperty<CityModelGroup> cityModelGroup = new SimpleObjectProperty<>();
    {
        cityModelGroup.addListener((observable, oldValue, newValue) -> {
            newValue.setViewOrder(10);
            root3D.getChildren().remove(oldValue);
            root3D.getChildren().add(newValue);
        });
    }

    private ObjectProperty<GeoReference> geoReference = new SimpleObjectProperty<>();
    private GMLViewTable cityGMLViewTable = new GMLViewTable();
    private List<CityModelView> cityModel = new ArrayList<>();
    private Material defaultMaterial;
    private Camera camera;
    private GizmoModel gizmo;

    public World() {
        defaultMaterial = new PhongMaterial(Color.WHITE);
    }

    public static World getActiveInstance() {
        return activeInstance;
    }

    public static void setActiveInstance(World world, Group group) {
        activeInstance = world;
        root3D = group;
    }

    public static Group getRoot3D() {
        return root3D;
    }

    public GeoReference getGeoReference() {
        return geoReference.get();
    }

    public ObjectProperty<GeoReference> getGeoReferenceProperty() {
        return geoReference;
    }

    public Material getDefaultMaterial() {
        return defaultMaterial;
    }
    public GMLViewTable getFeatureViewTable() {
        return cityGMLViewTable;
    }

    public void setGeoReference(GeoReference geoReference) {
        this.geoReference.set(geoReference);
    }

    public List<CityModelView> getCityModels() {
        return cityModel;
    }

    public void setCityModel(CityModelView cityModel) {
        this.cityModel = new ArrayList<CityModelView>();
        this.cityModel.add(cityModel);
    }

    public void addCityModel(CityModelView cityModel) {
        this.cityModel.add(cityModel);
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        root3D.getChildren().add(camera.getRoot());
    }

    public CityModelGroup getCityModelGroup() {
        return cityModelGroup.get();
    }

    public ObjectProperty<CityModelGroup> cityModelGroupProperty() {
        return cityModelGroup;
    }

    public void setCityModelGroup(CityModelGroup cityModelGroup) {
        this.cityModelGroup.set(cityModelGroup);
    }

    public void setGizmo(GizmoModel gizmo) {
        this.gizmo = gizmo;
        root3D.getChildren().add(gizmo);
    }

    public GizmoModel getGizmo() {
        return gizmo;
    }
    
    public void addFeatureView(BuildingView view) {
        cityGMLViewTable.register(view);
    }
}
