package org.plateau.citygmleditor.world;

import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.plateau.citygmleditor.citymodel.CityModel;
import org.plateau.citygmleditor.geometry.GeoReference;

public class World {
    private static World activeInstance;

    public static World getActiveInstance() {
        return activeInstance;
    }

    public static void setActiveInstance(World world) {
        activeInstance = world;
    }


    private GeoReference geoReference;
    private CityModel cityModel;

    private Material defaultMaterial;

    public GeoReference getGeoReference() {
        return geoReference;
    }

    public void setGeoReference(GeoReference geoReference) {
        this.geoReference = geoReference;
    }

    public Material getDefaultMaterial() {
        return defaultMaterial;
    }

    public World() {
        this.defaultMaterial = new PhongMaterial(Color.WHITE);
    }

    public CityModel getCityModel() {
        return cityModel;
    }

    public void setCityModel(CityModel cityModel) {
        this.cityModel = cityModel;
    }
}
