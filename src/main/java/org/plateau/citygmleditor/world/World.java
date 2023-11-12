package org.plateau.citygmleditor.world;

import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.Group;
import javax.swing.plaf.synth.Region;
import javafx.scene.SubScene;
import javafx.scene.input.*;

import org.w3c.dom.events.MouseEvent;

import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.geometry.GeoReference;

public class World {
    private static World activeInstance;
    private static Group root3D;
    private GeoReference geoReference;
    private CityModel cityModel;
    private Material defaultMaterial;

    public static World getActiveInstance() {
        return activeInstance;
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

    public CityModel getCityModel() {
        return cityModel;
    }

    public static void setActiveInstance(World world, Group group) {
        activeInstance = world;
        root3D = group;
    }

    public void setGeoReference(GeoReference geoReference) {
        this.geoReference = geoReference;
    }

    public void setCityModel(CityModel cityModel) {
        this.cityModel = cityModel;
    }

    public World() {
        this.defaultMaterial = new PhongMaterial(Color.WHITE);
    }

}
