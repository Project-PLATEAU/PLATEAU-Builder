package org.plateau.citygmleditor.citymodel;


import javafx.scene.Parent;
import org.citygml4j.xml.schema.SchemaHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CityModelView extends Parent {
    private org.citygml4j.model.citygml.core.CityModel gmlObject;
    private SchemaHandler schemaHandler;

    private String gmlPath;

    private AppearanceView rgbTextureAppearance;
    private final ArrayList<BuildingView> cityObjectMembers = new ArrayList<>();

    public org.citygml4j.model.citygml.core.CityModel getGmlObject() {
        return gmlObject;
    }
    public SchemaHandler getSchemaHandler() { return this.schemaHandler; }

    public CityModelView(org.citygml4j.model.citygml.core.CityModel gmlObject, SchemaHandler schemaHandler) {
        this.gmlObject = gmlObject;
        this.schemaHandler = schemaHandler;
    }

    public AppearanceView getRGBTextureAppearance() {
        return this.rgbTextureAppearance;
    }

    public void setRGBTextureAppearance(AppearanceView appearance) {
        this.rgbTextureAppearance = appearance;
    }

    public void setRGBTextureAppearances(AppearanceView appearance) {
        this.rgbTextureAppearance = appearance;
    }

    public String getGmlPath() {
        return gmlPath;
    }

    public void setGmlPath(String gmlPath) {
        this.gmlPath = gmlPath;
    }

    public List<BuildingView> getCityObjectMembersUnmodifiable() {
        return Collections.unmodifiableList(cityObjectMembers);
    }

    public void addCityObjectMember(BuildingView cityObjectMember) {
        this.cityObjectMembers.add(cityObjectMember);
        this.getChildren().add(cityObjectMember);
    }

    public void removeCityObjectMember(BuildingView cityObjectMember) {
        this.cityObjectMembers.remove(cityObjectMember);
        this.getChildren().remove(cityObjectMember);
    }
}
