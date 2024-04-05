package org.plateaubuilder.core.citymodel;

import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.xml.schema.SchemaHandler;

import java.util.List;
import java.util.stream.Collectors;

public class CityModelView extends ManagedGMLView<CityModel> {
    private SchemaHandler schemaHandler;

    private String gmlPath;

    private AppearanceView rgbTextureAppearance;

    public SchemaHandler getSchemaHandler() { return this.schemaHandler; }

    public CityModelView(CityModel gmlObject, SchemaHandler schemaHandler) {
        super(gmlObject);

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

    public void addRGBTextureAppearances(AppearanceView appearance) {
        this.rgbTextureAppearance.addSurfaceData(appearance.getSurfaceData());
    }

    public String getGmlPath() {
        return gmlPath;
    }

    public void setGmlPath(String gmlPath) {
        this.gmlPath = gmlPath;
    }

    public List<BuildingView> getCityObjectMembersUnmodifiable() {
        return getChildrenUnmodifiable()
                .stream()
                .map((node) -> (BuildingView)node)
                .collect(Collectors.toList());
    }
}
