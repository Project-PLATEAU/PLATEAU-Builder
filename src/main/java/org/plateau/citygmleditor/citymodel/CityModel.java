package org.plateau.citygmleditor.citymodel;


import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.AppearanceMember;
import org.citygml4j.model.citygml.building.AbstractBuilding;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.core.CityObjectMember;
import org.citygml4j.xml.schema.SchemaHandler;
import org.plateau.citygmleditor.citymodel.factory.AppearanceFactory;
import org.plateau.citygmleditor.geometry.GeoCoordinate;
import org.plateau.citygmleditor.geometry.GeoReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityModel extends Parent {
    private org.citygml4j.model.citygml.core.CityModel gmlObject;
    private SchemaHandler schemaHandler;

    private String gmlPath;

    private Appearance rgbTextureAppearance;
    private final ArrayList<Building> cityObjectMembers = new ArrayList<>();

    public org.citygml4j.model.citygml.core.CityModel getGmlObject() {
        return gmlObject;
    }
    public SchemaHandler getSchemaHandler() { return this.schemaHandler; }

    public CityModel(org.citygml4j.model.citygml.core.CityModel gmlObject, SchemaHandler schemaHandler) {
        this.gmlObject = gmlObject;
        this.schemaHandler = schemaHandler;
    }

    public Appearance getRGBTextureAppearance() {
        return this.rgbTextureAppearance;
    }

    public void setRGBTextureAppearance(Appearance appearance) {
        this.rgbTextureAppearance = appearance;
    }

    public void setRGBTextureAppearances(Appearance appearance) {
        this.rgbTextureAppearance = appearance;
    }

    public String getGmlPath() {
        return gmlPath;
    }

    public void setGmlPath(String gmlPath) {
        this.gmlPath = gmlPath;
    }

    public List<Building> getCityObjectMembersUnmodifiable() {
        return Collections.unmodifiableList(cityObjectMembers);
    }

    public void addCityObjectMember(Building cityObjectMember) {
        this.cityObjectMembers.add(cityObjectMember);
        this.getChildren().add(cityObjectMember);
    }
}
