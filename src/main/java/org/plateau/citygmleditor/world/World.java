package org.plateau.citygmleditor.world;

import java.util.List;
import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;
import javax.swing.plaf.synth.Region;
import javafx.scene.SubScene;
import javafx.scene.input.*;

import org.w3c.dom.events.MouseEvent;

import org.plateau.citygmleditor.citymodel.CityModelView;
import org.plateau.citygmleditor.geometry.GeoReference;

public class World {
    private static World activeInstance;
    private static Group root3D;
    private GeoReference geoReference;
    private List<CityModelView> cityModel;
    private Material defaultMaterial;
    private StringProperty epsgCode = new SimpleStringProperty();
    private StringProperty originProperty = new SimpleStringProperty();

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
        return geoReference;
    }

    public Material getDefaultMaterial() {
        return defaultMaterial;
    }

    public void setGeoReference(GeoReference geoReference) {
        this.geoReference = geoReference;
        originProperty.set(String.format("(%f, %f, %f)", geoReference.getOrigin().x, geoReference.getOrigin().y, geoReference.getOrigin().z));
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

    public StringProperty getEPSGCodeProperty() {
        return epsgCode;
    }

    public String getEPSGCode() {
        return epsgCode.get();
    }

    public void setEPSGCode(String epsgCode) {
        this.epsgCode.set(epsgCode);
    }

    public StringProperty getOriginProperty() {
        return originProperty;
    }
}
